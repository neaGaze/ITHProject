package com.ith.project;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.EntityClasses.Message;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.menu.CustomMenu;
import com.ith.project.menu.CustomMenuListAdapter;
import com.ith.project.sqlite.DateLogSQLite;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.MessageSQLite;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MessageListActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private final String url = "http://192.168.100.2/EMSWebService/Service1.svc/json/GetMessageList";

	private ProgressDialog pdialog;
	private Dialog dialog;
	private MessageSQLite messageSQLite;
	private DateLogSQLite dateLogSQLite;
	private static MessageListActivity context;
	private ImageButton menuButton;
	private ImageButton homeButton;
	private HttpConnection conn;
	static CustomMenuListAdapter menuAdapter;
	private static ArrayList<Message> itemDetails;
	private ListView listView;
	private static MessageItemArrayAdapter msgItemArrAdapter;
	private LinearLayout linLayoutMenu;
	private static int messageCount;
	private static boolean connFlag;
	private CallMenuDialog callDiag;
	private HashMap<String, String> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.list_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		context = this;
		init();

	}

	@Override
	public void onPause() {
		super.onPause();
		messageSQLite.closeDB();
		dateLogSQLite.closeDB();
		pdialog.dismiss();
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();
		pdialog.dismiss();
	}

	/************************************************************************************
	 * Initialize values first
	 * ***********************************************************************************/
	private void init() {

		new Thread(new Runnable() {

			public void run() {

				JSONObject inputJson;
				conn = HttpConnection.getSingletonConn();

				messageSQLite = new MessageSQLite(MessageListActivity.this);
				dateLogSQLite = new DateLogSQLite(MessageListActivity.this);
				messageSQLite.openDB();
				dateLogSQLite.openDB();

				inputJson = Message.getInquiryJson(LoginAuthentication
						.getUserLoginId());

				Log.v("getmessage inquiry", "" + inputJson.toString());

				/** To establish connection to the web service **/
				// String messagesFromWS = conn.getJSONFromUrl(inputJson, url);
				connFlag = true;
				// Log.v("Messages:", "" + messagesFromWS);
				// if (!messagesFromWS.equals(""))
				{
					// connFlag = false;
					/** Update the local file according to the web service **/
					// messageSQLite.updateDBMsgsTableJson(messagesFromWS,
					// dateLogSQLite);
				}
				/** Now read from the local DB always **/
				itemDetails = messageSQLite.getJSONFromDB();
				/**
				 * To run the main thread after completion of the connection
				 * thread
				 **/
				runOnUiThread(new Runnable() {

					public void run() {

						menuButton = (ImageButton) findViewById(R.id.menu);
						menuButton.setOnClickListener(MessageListActivity.this);

						homeButton = (ImageButton) findViewById(R.id.home);
						homeButton.setOnClickListener(MessageListActivity.this);

						registerForContextMenu(homeButton);

						listView = (ListView) findViewById(R.id.listView1);

						Log.d("***Welcome to Message ListView***", ".......");
						msgItemArrAdapter = new MessageItemArrayAdapter(
								MessageListActivity.this,
								R.layout.messages_list, itemDetails);
						listView.setAdapter(msgItemArrAdapter);
						messageCount = msgItemArrAdapter.getCount();
						listView.setOnItemClickListener(MessageListActivity.this);

						menuItems = new HashMap<String, String>();
						pdialog.dismiss();
					}
				});
			}

		}).start();

	}

	/****************************************************************************
	 * View Message
	 *************************************************************************/
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		Log.v("MessagelistItemClicked @" + (messageCount - position - 1),
				"HOOOrAyyy!!!!");

		pdialog.show();

		Intent intent = new Intent(MessageListActivity.this,
				MessageViewActivity.class);
		intent.putExtra("PositionOfMessage", (position));
		MessageListActivity.this.startActivity(intent);

		// ListItemActivity.this.finish();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something on back.
			pdialog.show();
			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View v) {

		if (v.equals(menuButton)) {
			pdialog.show();
			Intent intent = new Intent(MessageListActivity.this,
					GridItemActivity.class);
			this.startActivity(intent);
		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			menuItems.put("Send SMS", "mail_sms");
			menuItems.put("Send Web Message", "mail_web");
			menuItems.put("Call", "call");
			menuItems.put("Exit", "exit");
			callDiag = new CallMenuDialog(this, pdialog, dialog, menuItems);
			// callMenuDialog();
		} else if (v.equals(linLayoutMenu)) {
			Toast.makeText(this, "Add Message Clicked", Toast.LENGTH_SHORT)
					.show();
		} else
			Toast.makeText(this, "Menu Item Clicked", Toast.LENGTH_SHORT)
					.show();

	}

	/****************************************************************************
	 * Get the Activity instance of ListItemActivity
	 *************************************************************************/
	public static MessageListActivity getMessageListActivityInstance() {
		return context;
	}


	/******************************************************************************************
	 * A new ArrayAdapter Class to handle the List View
	 * *****************************************************************************************/
	private class MessageItemArrayAdapter extends ArrayAdapter<Message> {

		private Context cntxt;
		private ArrayList<Message> itemDets;

		public MessageItemArrayAdapter(Context context, int textViewResourceId,
				ArrayList<Message> itemDetails) {
			super(context, textViewResourceId);
			this.cntxt = context;
			this.itemDets = itemDetails;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) cntxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view;
			// if (convertView == null)
			{
				view = new View(this.cntxt);

				view = inflater.inflate(R.layout.messages_list, parent, false);

				parent.setBackgroundColor(Color.rgb(221, 221, 221));

				TableLayout tableLayout = (TableLayout) view
						.findViewById(R.id.tableLayoutCheckBoxMessage);
				if (!this.itemDets.get(position).getMsgRead())
					tableLayout
							.setBackgroundResource(R.drawable.message_view_unread);

				TextView textView = (TextView) view
						.findViewById(R.id.textViewMessageTitle);
				textView.setText(this.itemDets.get(position).getTitle());
				textView.setFocusable(false);

				EmployeeSQLite employeeSQLite = new EmployeeSQLite(
						MessageListActivity.this);
				employeeSQLite.openDB();
				TextView msgFrom = (TextView) view
						.findViewById(R.id.textViewMessageSender);
				msgFrom.setText(employeeSQLite.getEmpName(this.itemDets.get(
						position).getMsgFrom()));
				msgFrom.setFocusable(false);
				employeeSQLite.closeDB();

				TextView time = (TextView) view
						.findViewById(R.id.textViewMessageDate);
				time.setText(this.itemDets.get(position).getDate());
				textView.setFocusable(false);
				// time.setVisibility(View.GONE);
				inflater = null;
			} // else
				// view = (View) convertView;

			return view;
		}

		public int getCount() {

			return itemDets.size();
		}

		public Message getItem(int arg0) {

			return itemDets.get(arg0);
		}

		public long getItemId(int position) {

			return position;
		}
	}
}
