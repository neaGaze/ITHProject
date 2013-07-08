package com.ith.project;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ith.project.EntityClasses.Bulletin;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.menu.CustomMenu;
import com.ith.project.menu.CustomMenuListAdapter;
import com.ith.project.sdcard.BulletinLocal;
import com.ith.project.sqlite.BulletinSQLite;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListItemActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private static final int MENU_EXIT = 0;
	private final String url = "http://192.168.100.2/EMSWebService/Service1.svc/json/GetBulletins";

	private static ArrayList<Bulletin> itemDetails;
	private ListView listView;
	private Button ExitBut;
	private ImageButton menuButton;
	private ImageButton BulletinButton;
	private ImageButton homeButton;
	private HttpConnection conn;
	private BulletinLocal bulletinLocal;
	private BulletinSQLite bulletinSQLite;
	private Bulletin bulletin;
	static ListItemArrayAdapter listItemArrAdapter;
	private static int bulletinCount;
	private ProgressDialog pdialog;
	private LinearLayout linLayoutMenu;
	private ListView menuListView;
	static CustomMenuListAdapter menuAdapter;
	private static ListItemActivity context;
	private Dialog dialog;
	private static boolean connFlag;
	private CallMenuDialog callDiag;
	private HashMap<String, String> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (CallMenuDialog.Exit) {
			CallMenuDialog.Exit = false;
			this.finish();
		} else {
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
	}

	@Override
	public void onPause() {
		super.onPause();
		bulletinSQLite.closeDB();
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

		Thread thread = new Thread(new Runnable() {

			public void run() {

				JSONObject inputJson;
				conn = HttpConnection.getSingletonConn();
				// conn = new HttpConnection(url);
				// bulletinLocal = new BulletinLocal();

				bulletinSQLite = new BulletinSQLite(ListItemActivity.this);
				bulletinSQLite.openDB();

				bulletin = new Bulletin();

				/** Make a json Object out of UserLoginId **/
				inputJson = bulletin.getJsonUserLoginId(LoginAuthentication
						.getUserLoginId());

				Log.v("getBulletin inquiry", "" + inputJson.toString());

				/** To remove add Bulletin for normal users **/
				modifyBulletinAdd4Admin(LoginAuthentication.getUserRoleId());

				/** To establish connection to the web service **/
				String bulletinsFromWS = conn.getJSONFromUrl(inputJson, url);
				connFlag = true;
				Log.e("Bulletins:", "here: " + bulletinsFromWS);

				/** Update the local file according to the web service **/
				// bulletinLocal.updateLocalFiles(inputJson, bulletinsFromWS);
				if (!bulletinsFromWS.equals("")) {
					connFlag = false;
					Log.e("Empty Bulletins Response",
							"So don't delete Bulletins");
					bulletinSQLite.deleteAllRows();
					bulletinSQLite.updateDBUsersTableJson(bulletinsFromWS);
				}
				/** Now read from the local file always **/
				// JSONArray outputJson = bulletinLocal
				// .getJSONFromLocal(inputJson);

				// Log.v("JsonArray:", "" + outputJson.toString());

				// itemDetails = setBulletin(outputJson);
				itemDetails = bulletinSQLite.getJSONFromDB();

				// Log.v("Bulletins:",""+
				// bulletinLocal.getJSONFromLocal(inputJson).toString());

				/**
				 * To run the main thread after completion of the connection
				 * thread
				 **/
				runOnUiThread(new Runnable() {

					public void run() {

						// ExitBut = (Button) findViewById(R.id.exitButton);
						// ExitBut.setOnClickListener(ListItemActivity.this);

						menuButton = (ImageButton) findViewById(R.id.menu);
						menuButton.setOnClickListener(ListItemActivity.this);

						homeButton = (ImageButton) findViewById(R.id.home);
						homeButton.setOnClickListener(ListItemActivity.this);
						registerForContextMenu(homeButton);
						listView = (ListView) findViewById(R.id.listView1);

						Log.d("***Welcome to ListView***", ".......");
						listItemArrAdapter = new ListItemArrayAdapter(
								ListItemActivity.this, R.layout.list_items,
								itemDetails);
						listView.setAdapter(listItemArrAdapter);
						bulletinCount = listItemArrAdapter.getCount();
						listView.setOnItemClickListener(ListItemActivity.this);
						menuItems = new HashMap<String, String>();
						pdialog.dismiss();
					}

				});

			}

		});
		thread.start();
	}

	/********************************************************************************
	 * Fill bulletin class and itemDetails ArrayList
	 * *********************************************************************************/
	public ArrayList<Bulletin> setBulletin(JSONArray tempJsonArr) {

		ArrayList<Bulletin> tempArrList = new ArrayList<Bulletin>();
		int jsonArrLength = tempJsonArr.length();
		for (int i = 0; i < jsonArrLength; i++) {
			try {
				Bulletin tempBulletin = new Bulletin();
				JSONObject tempJson = tempJsonArr.getJSONObject(i);
				tempBulletin.setValues(tempJson);
				tempArrList.add(tempBulletin);
				Log.v("json object:", "" + tempJson.toString());

			} catch (JSONException e) {
				Log.e("jSONException", "" + e.getMessage());
				e.printStackTrace();
			}

		}
		return tempArrList;
	}

	/*********************************************************************************
	 * Called when the LExit Button is Clicked
	 * ******************************************************************************/
	public void modifyBulletinAdd4Admin(int userRolesId) {
		// if (userRolesId == 2)
		// findViewById(R.id.bulletin_add_icon).setVisibility(View.INVISIBLE);
	}

	/*********************************************************************************
	 * Called when the LExit Button is Clicked
	 * ******************************************************************************/
	public void onClick(View v) {
		if (v.equals(ExitBut)) {
			Intent intent = new Intent(ListItemActivity.this,
					GridItemActivity.class);
			this.startActivity(intent);
			// this.finish();
		} else if (v.equals(menuButton)) {
			pdialog.show();
			Intent intent = new Intent(ListItemActivity.this,
					GridItemActivity.class);
			this.startActivity(intent);
			// this.finish();
		} else if (v.equals(BulletinButton)) {
			pdialog.show();
			// Toast.makeText(this, "Add Bulletin", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(ListItemActivity.this,
					BulletinAddActivity.class);
			this.startActivity(intent);
			this.finish();

		} else if (v.equals(homeButton)) {
			/** Set up the Menu **/
			menuItems.put("Exit", "exit");
			menuItems.put("Add Bulletin", "add_employee");
			callDiag = new CallMenuDialog(this, pdialog, dialog, menuItems);
			// callMenuDialog();
		} else if (v.equals(linLayoutMenu)) {
			Toast.makeText(this, "Add Employee Clicked", Toast.LENGTH_SHORT)
					.show();
		} else
			Toast.makeText(this, "Menu Item Clicked", Toast.LENGTH_SHORT)
					.show();

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
		if (LoginAuthentication.getUserRoleId() == 1)
			tempArrList.add(setMenuItems("Add Bulletin", "add_employee"));
		tempArrList.add(setMenuItems("Exit", "exit"));

		menuAdapter = new CustomMenuListAdapter(ListItemActivity.this,
				R.layout.custom_menu_2, tempArrList);
		menuListView.setAdapter(menuAdapter);
		menuListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {

				TextView c = (TextView) view
						.findViewById(R.id.textViewCustomMenu_2);
				String keyword = c.getText().toString();

				/** When "Add Bulletin" menu item is pressed **/
				if (keyword.equals("Add Bulletin")) {
					if (!connFlag) {
						pdialog.show();
						Intent intent = new Intent(ListItemActivity.this,
								BulletinAddActivity.class);
						ListItemActivity.this.startActivity(intent);
					} else {
						Toast.makeText(ListItemActivity.this,
								"Can't add while you're Offline",
								Toast.LENGTH_SHORT).show();
						Log.e("Attempted Bulletin Add While Offline",
								"Go Online and then try");
					}
				}
				/** When "Exit" menu item is pressed **/
				else if (keyword.equals("Exit")) {
					pdialog.show();
					ListItemActivity.this.finish();
				}
			}
		});
		/*
		 * TextView text1 = (TextView) dialog
		 * .findViewById(R.id.textViewCustomMenu1);
		 * text1.setText("Add Employee"); text1.setOnClickListener(this);
		 * ImageView image1 = (ImageView) dialog
		 * .findViewById(R.id.imageViewCustomMenu1);
		 * image1.setImageResource(R.drawable.add_employee);
		 * image1.setOnClickListener(this);
		 * 
		 * linLayoutMenu = (LinearLayout) dialog
		 * .findViewById(R.id.linearLayoutCustomMenu2);
		 * menuInflater.inflate(R.layout.custom_menu, linLayoutMenu, false);
		 * linLayoutMenu.setOnClickListener(this); TextView text2 = (TextView)
		 * dialog .findViewById(R.id.textViewCustomMenu2);
		 * text2.setText("Send Messages"); text2.setOnClickListener(this);
		 * ImageView image2 = (ImageView) dialog
		 * .findViewById(R.id.imageViewCustomMenu2);
		 * image2.setImageResource(R.drawable.send_mail);
		 * image2.setOnClickListener(this);
		 * 
		 * linLayoutMenu = (LinearLayout) dialog
		 * .findViewById(R.id.linearLayoutCustomMenu3);
		 * menuInflater.inflate(R.layout.custom_menu, linLayoutMenu, false);
		 * linLayoutMenu.setOnClickListener(this); TextView text3 = (TextView)
		 * dialog .findViewById(R.id.textViewCustomMenu3);
		 * text3.setText("Send SMS"); text3.setOnClickListener(this); ImageView
		 * image3 = (ImageView) dialog .findViewById(R.id.imageViewCustomMenu2);
		 * image3.setImageResource(R.drawable.send_mail);
		 * image3.setOnClickListener(this);
		 * 
		 * linLayoutMenu = (LinearLayout) dialog
		 * .findViewById(R.id.linearLayoutCustomMenu4);
		 * menuInflater.inflate(R.layout.custom_menu, linLayoutMenu, false);
		 * linLayoutMenu.removeAllViewsInLayout();
		 */
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

	/****************************************************************************
	 * When Home Button is Clicked
	 *************************************************************************/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);

	}

	/****************************************************************************
	 * Menu Item Creation
	 *************************************************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, MENU_EXIT, 0, "Exit").setIcon(R.drawable.exit);
		return super.onCreateOptionsMenu(menu);
	}

	/****************************************************************************
	 * Menu Item Clicked
	 *************************************************************************/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_EXIT: {
			// When Exit Button is clicked
			this.finish();
		}

		default:
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	/****************************************************************************
	 * View Bulletin
	 *************************************************************************/
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		Log.v("listItemClicked @" + (bulletinCount - position - 1),
				"HOOOrAyyy!!!!");

		pdialog.show();

		Intent intent = new Intent(ListItemActivity.this,
				BulletinViewActivity.class);
		intent.putExtra("PositionOfBulletin", (position));
		ListItemActivity.this.startActivity(intent);

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

	public static ArrayList<Bulletin> getBulletinArrayList() {
		return itemDetails;
	}

	/****************************************************************************
	 * Get the Activity instance of ListItemActivity
	 *************************************************************************/
	public static ListItemActivity getListItemActivityInstance() {
		return context;
	}

	public static boolean getConnFlag() {
		return connFlag;
	}
}
