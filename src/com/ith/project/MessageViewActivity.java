package com.ith.project;

import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.EntityClasses.Message;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.MessageSQLite;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageViewActivity extends Activity implements OnClickListener {

	private final String readUrl = "MarkMessageAsIsRead";

	private TextView messageTitle;
	private TextView messageDesc;
	private TextView messageAuthor;
	private TextView messageDate;
	private Dialog dialog;
	private ImageButton menuButton;
	private ImageButton homeButton;
	private HashMap<String, String> menuItems;
	private int msgTo, msgRealId, position;
	private boolean isMsgRead;
	private String isPending;
	private JSONObject readMsgInquiry;
	private EmployeeSQLite employeeSQLite;
	private MessageSQLite messageSQLite;
	private HttpConnection conn;
	private Message ViewedMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.message_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (employeeSQLite != null)
			employeeSQLite.closeDB();
		if (messageSQLite != null)
			messageSQLite.closeDB();
		if (dialog != null)
			dialog.dismiss();
		this.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	private void init() {

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutMessage);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.message_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		position = bundle.getInt("MessageId");

		messageSQLite = new MessageSQLite(MessageViewActivity.this);
		if (!messageSQLite.isOpen())
			messageSQLite.openDB();

		ViewedMessage = messageSQLite.getViewedMessage(position);

		Log.v("messageTitle", "" + ViewedMessage.getMsgTitle());

		messageTitle = (TextView) findViewById(R.id.editTextMessageTitleView);
		messageTitle.setText(ViewedMessage.getMsgTitle());

		messageDesc = (TextView) findViewById(R.id.editTextMessageDescView);
		messageDesc.setText(ViewedMessage.getMsgDesc());

		employeeSQLite = new EmployeeSQLite(MessageViewActivity.this);
		if (!employeeSQLite.isOpen())
			employeeSQLite.openDB();

		messageAuthor = (TextView) findViewById(R.id.textViewMessageSenderName);
		messageAuthor.setText(employeeSQLite.getEmpName(ViewedMessage
				.getMsgFrom()));

		messageDate = (TextView) findViewById(R.id.editTextMessageDate);
		messageDate.setText(ViewedMessage.getDate() + "  @"
				+ ViewedMessage.getTime());

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(MessageViewActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(MessageViewActivity.this);

		menuItems = new HashMap<String, String>();

		updateReadStatus();
	}

	/************************************************************************************
	 * Update the read status in both DB and in Web
	 * ***********************************************************************************/
	private void updateReadStatus() {

		msgTo = LoginAuthentication.EmployeeId;
		msgRealId = ViewedMessage.getMsgRealId();

		/** Update the read value in sqlite locally **/
		isMsgRead = ViewedMessage.getMsgRead();
		isPending = ViewedMessage.getMsgType();
		Log.e("Let's see the isMsgRead Flag", "msgRead Flag is : " + isMsgRead);

		/**
		 * UPdate only if Msg is unread and it is also in sync with main
		 * Database
		 **/
		if ((!isMsgRead && (isPending == null || isPending.equals("")))) {
			if (!messageSQLite.isOpen())
				messageSQLite.openDB();
			messageSQLite.updateMsgRead(msgTo, msgRealId);

			readMsgInquiry = makeMsgReadJson(msgTo, msgRealId);
		}
		Thread readUpdateThread = new Thread(new Runnable() {

			public void run() {
				conn = HttpConnection.getSingletonConn();
				Log.e("readMsgInquiry", "" + readMsgInquiry.toString());
				String readStatusStr = conn.getJSONFromUrl(readMsgInquiry,
						readUrl);
				Log.e("Messages Read Status:", "" + readStatusStr);

				JSONObject readStatusJson;
				try {
					readStatusJson = new JSONObject(readStatusStr);
					boolean readUpdateStatus = (Boolean) readStatusJson
							.get("MarkMessageAsIsReadResult");
					if (readUpdateStatus) {

						Log.e("read status Updated",
								"Read Status Updated Succesfully");
					} else {
						/**
						 * update the same msg in sqlite with
						 * MessageType="readUpdatePending" if connection is
						 * refused
						 **/
						if (!messageSQLite.isOpen())
							messageSQLite.openDB();
						messageSQLite.updateReadPending(msgTo, msgRealId,
								"readUpdatePending");

						Log.e("Problem Sending Message ",
								"Pending Read Messages saved as draft"
										+ readUpdateStatus);
					}
				} catch (JSONException e) {
					/***
					 * Save the read msg in sqlite if connection return is *JPT*
					 **/
					if (!messageSQLite.isOpen())
						messageSQLite.openDB();
					messageSQLite.updateReadPending(msgTo, msgRealId,
							"readUpdatePending");

					Log.e("JSONException while Updating read Status",
							"" + e.getMessage());
					Log.e("Messges Saved for next Update",
							"Will be UPdated next time");
					e.printStackTrace();
				}

				runOnUiThread(new Runnable() {

					public void run() {

					}

				});
			}
		});
		if ((!isMsgRead && (isPending == null || isPending.equals(""))))
			readUpdateThread.start();

	}


	public void onClick(View v) {
		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			menuItems.put("Send Web Message", "mail_web");
			new CallMenuDialog(this, dialog, menuItems);
		}

		else {
		}
	}

	/**********************************************************************************
	 * To make a JSON Object for inquiry of read msg
	 * **********************************************************************************/
	public static JSONObject makeMsgReadJson(int msgTo2, int msgId2) {

		JSONObject readInquiry = new JSONObject();
		try {
			readInquiry.put("employeeId", new StringBuilder().append(msgTo2)
					.toString());
			readInquiry.put("messageId", new StringBuilder().append(msgId2)
					.toString());
			return readInquiry;
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
