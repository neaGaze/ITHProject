package com.ith.project;

import java.util.ArrayList;
import org.json.JSONObject;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.EntityClasses.Message;
import com.ith.project.connection.HttpConnection;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
	private ListView menuListView;
	private static ArrayList<Message> itemDetails;
	private ListView listView;
	private static MessageItemArrayAdapter msgItemArrAdapter;
	private LinearLayout linLayoutMenu;
	private static int messageCount;

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

				// Log.v("Messages:", "" + messagesFromWS);

				/** Update the local file according to the web service **/
				// messageSQLite.updateDBMsgsTableJson(messagesFromWS,
				// dateLogSQLite);

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
								MessageListActivity.this, R.layout.list_items,
								itemDetails);
						listView.setAdapter(msgItemArrAdapter);
						messageCount = msgItemArrAdapter.getCount();
						listView.setOnItemClickListener(MessageListActivity.this);

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
				BulletinViewActivity.class);
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
			callMenuDialog();
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

	private void callMenuDialog() {

		LayoutInflater menuInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		linLayoutMenu = (LinearLayout) findViewById(R.id.linearLayoutCustomMenu_2);
		// LinearLayout linLayoutMenu = new LinearLayout(this);
		menuInflater.inflate(R.layout.menu_list_view, linLayoutMenu, false);

		/** To bring front the Dialog box **/
		dialog = new Dialog(this, R.style.mydialogstyle);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);

		/** To set the alignment of the Dialog box in the screen **/
		WindowManager.LayoutParams WMLP = dialog.getWindow().getAttributes();
		WMLP.x = getWindowManager().getDefaultDisplay().getWidth();
		WMLP.gravity = Gravity.TOP;
		WMLP.verticalMargin = 0.08f; // To put it below header
		dialog.getWindow().setAttributes(WMLP);

		/** To set the dialog box with the List layout in the android xml **/
		dialog.setContentView(R.layout.menu_list_view);

		menuListView = (ListView) dialog.findViewById(R.id.listView2);

		/** make an arrayList of items to display at the CustomMenu **/
		ArrayList<CustomMenu> tempArrList = new ArrayList<CustomMenu>();

		/** To remove add Bulletin for normal users **/
		// if (LoginAuthentication.getUserRoleId() == 1)
		tempArrList.add(setMenuItems("Send SMS", "mail_sms"));
		tempArrList.add(setMenuItems("Send Web Message", "mail_web"));
		tempArrList.add(setMenuItems("Call", "call"));
		tempArrList.add(setMenuItems("Exit", "exit"));

		menuAdapter = new CustomMenuListAdapter(MessageListActivity.this,
				R.layout.custom_menu_2, tempArrList);
		menuListView.setAdapter(menuAdapter);
		menuListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {

				TextView c = (TextView) view
						.findViewById(R.id.textViewCustomMenu_2);
				String keyword = c.getText().toString();

				/** When "Send SMS" menu item is pressed **/
				if (keyword.equals("Send SMS")) {
					pdialog.show();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.putExtra("address", "9803472561");
					intent.setType("vnd.android-dir/mms-sms");
					MessageListActivity.this.startActivity(intent);

				}/** When "Send Web Message" menu item is pressed **/
				else if (keyword.equals("Send Web Message")) {
					pdialog.show();

				}/** When "Call" menu item is pressed **/
				else if (keyword.equals("Call")) {
					pdialog.show();

				}
				/** When "Exit" menu item is pressed **/
				else if (keyword.equals("Exit")) {
					pdialog.show();
					MessageListActivity.this.finish();
					GridItemActivity.getGridItemActivityInstance().finish();
					ListItemActivity.getListItemActivityInstance().finish();
				}
			}
		});
		dialog.show();

	}

	/****************************************************************************
	 * When we have to set Menu Items in the ArrayList
	 *************************************************************************/
	public CustomMenu setMenuItems(String menuString, String menuIcon) {

		CustomMenu menu = new CustomMenu(menuString, menuIcon);
		menu.setValues(menuString, menuIcon);

		return menu;
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

				view = inflater.inflate(R.layout.list_items, parent, false);

				parent.setBackgroundColor(Color.rgb(221, 221, 221));

				TextView textView = (TextView) view
						.findViewById(R.id.textViewListDesc);
				textView.setText(this.itemDets.get(position).getTitle());
				textView.setFocusable(false);

				EmployeeSQLite employeeSQLite = new EmployeeSQLite(
						MessageListActivity.this);
				employeeSQLite.openDB();
				TextView msgFrom = (TextView) view
						.findViewById(R.id.textViewListDate);
				msgFrom.setText(employeeSQLite.getEmpName(this.itemDets.get(
						position).getMsgFrom()));
				msgFrom.setFocusable(false);
				employeeSQLite.closeDB();

				TextView time = (TextView) view
						.findViewById(R.id.textViewListTime);
				time.setText(this.itemDets.get(position).getTime());
				textView.setFocusable(false);
				time.setVisibility(View.GONE);
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
