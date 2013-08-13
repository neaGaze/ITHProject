package com.ith.project;

import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.MessageSQLite;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

public class MessageViewActivity extends Activity implements OnClickListener {

	private final String url = "ReadMessage";
	private final String readUrl = "MarkMessageAsIsRead";

	private TextView messageTitle;
	private TextView messageDesc;
	private TextView messageAuthor;
	private TextView messageDate;
	private Dialog dialog;
	private ImageButton menuButton;
	private ImageButton homeButton;
	private ProgressDialog pdialog;
	private CallMenuDialog callDiag;
	private HashMap<String, String> menuItems;
	private int msgTo, msgRealId, position;
	private boolean isMsgRead;
	private String isPending;
	private JSONObject readMsgInquiry;
	private EmployeeSQLite employeeSQLite;
	private MessageSQLite messageSQLite;
	private HttpConnection conn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * pdialog = new ProgressDialog(this); pdialog.setCancelable(true);
		 * pdialog.setMessage("Loading ...."); pdialog.show();
		 */

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.message_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();

	}

	private void init() {

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutMessage);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.message_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		position = bundle.getInt("PositionOfMessage");

		Log.v("messageTitle", ""
				+ MessageListActivity.getMessageArrayList().get(position)
						.getMsgTitle());

		messageTitle = (TextView) findViewById(R.id.editTextMessageTitleView);
		messageTitle.setText(MessageListActivity.getMessageArrayList()
				.get(position).getMsgTitle());

		messageDesc = (TextView) findViewById(R.id.editTextMessageDescView);
		messageDesc.setText(MessageListActivity.getMessageArrayList()
				.get(position).getMsgDesc());

		employeeSQLite = new EmployeeSQLite(MessageViewActivity.this);
		employeeSQLite.openDB();
		messageAuthor = (TextView) findViewById(R.id.textViewMessageSenderName);
		messageAuthor.setText(employeeSQLite.getEmpName(MessageListActivity
				.getMessageArrayList().get(position).getMsgFrom()));

		messageDate = (TextView) findViewById(R.id.editTextMessageDate);
		messageDate.setText(MessageListActivity.getMessageArrayList()
				.get(position).getDate()
				+ "  @"
				+ MessageListActivity.getMessageArrayList().get(position)
						.getTime());

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(MessageViewActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(MessageViewActivity.this);

		menuItems = new HashMap<String, String>();

		updateReadStatus();
		/* pdialog.dismiss(); */
	}

	/************************************************************************************
	 * Update the read status in both DB and in Web
	 * ***********************************************************************************/
	private void updateReadStatus() {

		// msgTo = MessageListActivity.getMessageArrayList().get(position)
		// .getMsgTo();
		msgTo = LoginAuthentication.EmployeeId;
		msgRealId = MessageListActivity.getMessageArrayList().get(position)
				.getMsgRealId();

		/** Update the read value in sqlite locally **/
		isMsgRead = MessageListActivity.getMessageArrayList().get(position)
				.getMsgRead();
		isPending = MessageListActivity.getMessageArrayList().get(position)
				.getMsgType();
		Log.e("Let's see the isMsgRead Flag", "msgRead Flag is : " + isMsgRead);

		/**
		 * UPdate only if Msg is unread and it is also in sync with main
		 * Database
		 **/
		if ((!isMsgRead && (isPending == null || isPending.equals("")))) {
			messageSQLite = new MessageSQLite(MessageViewActivity.this);
			if (!messageSQLite.isOpen())
				messageSQLite.openDB();
			messageSQLite.updateMsgRead(msgTo, msgRealId);
			messageSQLite.closeDB();
			readMsgInquiry = makeMsgReadJson(msgTo, msgRealId);
			// Log.e("readMsgINquiry", "" + readMsgInquiry.toString());
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
						messageSQLite.openDB();
						messageSQLite.updateReadPending(msgTo, msgRealId,
								"readUpdatePending");
						messageSQLite.closeDB();

						Log.e("Problem Sending Message ",
								"Pending Read Messages saved as draft"
										+ readUpdateStatus);
					}
				} catch (JSONException e) {
					/***
					 * Save the read msg in sqlite if connection return is *JPT*
					 **/
					messageSQLite.openDB();
					messageSQLite.updateReadPending(msgTo, msgRealId,
							"readUpdatePending");
					messageSQLite.closeDB();

					Log.e("JSONException while Updating read Status",
							"" + e.getMessage());
					Log.e("Messges Saved for next Update",
							"Will be UPdated next time");
					e.printStackTrace();
				}

				runOnUiThread(new Runnable() {

					public void run() {
						/* pdialog.show(); */
						// exitDialog.dismiss();
						/*
						 * MessageViewActivity.this.finish(); Intent intent =
						 * new Intent(MessageViewActivity.this,
						 * MessageListActivity.class);
						 * MessageViewActivity.this.startActivity(intent);
						 */

					}

				});
			}
		});
		if ((!isMsgRead && (isPending == null || isPending.equals(""))))
			readUpdateThread.start();

	}

	@Override
	public void onPause() {
		super.onPause();
		/* pdialog.dismiss(); */
		employeeSQLite.closeDB();
		// messageSQLite.closeDB();
		if (dialog != null)
			dialog.dismiss();
		this.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		/* pdialog.dismiss(); */
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
			// menuItems.put("Exit", "exit");
			callDiag = new CallMenuDialog(this, /* pdialog, */dialog, menuItems);
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
			// do something on back.
			/* pdialog.show(); */
			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
