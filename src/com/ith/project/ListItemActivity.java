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
import com.ith.project.menu.CustomMenuListAdapter;
import com.ith.project.sqlite.BulletinSQLite;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class ListItemActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private final String url = "GetBulletins";

	private static ArrayList<Bulletin> itemDetails;
	private ListView listView;
	private Button ExitBut;
	private ImageButton menuButton;
	private ImageButton BulletinButton;
	private ImageButton homeButton;
	private HttpConnection conn;
	private BulletinSQLite bulletinSQLite;
	private Bulletin bulletin;
	static ListItemArrayAdapter listItemArrAdapter;
	private static int bulletinCount;
	private LinearLayout linLayoutMenu;
	static CustomMenuListAdapter menuAdapter;
	private static ListItemActivity context;
	private Dialog dialog;
	private HashMap<String, String> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (CallMenuDialog.Exit || GridItemActivity.Exit) {
			CallMenuDialog.Exit = false;
			GridItemActivity.Exit = false;
			this.finish();
		} else {
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
		if (bulletinSQLite != null)
			bulletinSQLite.closeDB();
		if (dialog != null)
			dialog.dismiss();
		this.finish();
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	/************************************************************************************
	 * Initialize values first
	 * ***********************************************************************************/
	private void init() {

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setClickable(false);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(ListItemActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setClickable(false);
		homeButton.setClickable(true);
		homeButton.setOnClickListener(ListItemActivity.this);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(ListItemActivity.this);
		menuItems = new HashMap<String, String>();

		Thread thread = new Thread(new Runnable() {

			public void run() {

				JSONObject inputJson;
				conn = HttpConnection.getSingletonConn();

				bulletinSQLite = new BulletinSQLite(ListItemActivity.this);
				if (!bulletinSQLite.isOpen())
					bulletinSQLite.openDB();

				bulletin = new Bulletin();

				/** Make a json Object out of UserLoginId **/
				inputJson = bulletin
						.getJsonUserLoginId(LoginAuthentication.UserloginId);

				Log.v("getBulletin inquiry", "" + inputJson.toString());

				/** To establish connection to the web service **/
				String bulletinsFromWS = conn.getJSONFromUrl(inputJson, url);

				Log.e("Bulletins:", "here: " + bulletinsFromWS);

				/** Update the local file according to the web service **/
				if (bulletinsFromWS.startsWith("{")) {

					bulletinSQLite.deleteOldBulletins();
					bulletinSQLite
							.updateBulletinsListFromWebService(bulletinsFromWS);
				}
				/** Now read from the local file always **/
				itemDetails = bulletinSQLite.getBulletinsFromDB();

				/**
				 * To run the main thread after completion of the connection
				 * thread
				 **/
				if (itemDetails != null)
				/** If list is null do not run this thread :D **/
				{
					runOnUiThread(new Runnable() {

						public void run() {

							listView = (ListView) findViewById(R.id.listView1);

							Log.d("***Welcome to Bulletins ListView***",
									".......");
							listItemArrAdapter = new ListItemArrayAdapter(
									ListItemActivity.this, R.layout.list_items,
									itemDetails);
							listView.setAdapter(listItemArrAdapter);
							bulletinCount = listItemArrAdapter.getCount();
							listView.setOnItemClickListener(ListItemActivity.this);
							menuItems = new HashMap<String, String>();

						}

					});
				} else {

				}

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
	public void onClick(View v) {
		if (v.equals(ExitBut)) {
			Intent intent = new Intent(ListItemActivity.this,
					GridItemActivity.class);
			this.startActivity(intent);

		} else if (v.equals(menuButton)) {

			Intent intent = new Intent(ListItemActivity.this,
					GridItemActivity.class);
			this.startActivity(intent);

		} else if (v.equals(BulletinButton)) {

			Intent intent = new Intent(ListItemActivity.this,
					BulletinAddActivity.class);
			this.startActivity(intent);
			this.finish();

		} else if (v.equals(homeButton)) {
			/** Set up the Menu **/
			menuItems.put("Add Bulletin", "add_employee");
			new CallMenuDialog(this, dialog, menuItems);

		} else if (v.equals(linLayoutMenu)) {
			Toast.makeText(this, "Add Employee Clicked", Toast.LENGTH_SHORT)
					.show();
		} else
			Toast.makeText(this, "Menu Item Clicked", Toast.LENGTH_SHORT)
					.show();

	}

	/****************************************************************************
	 * View Bulletin
	 *************************************************************************/
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		Log.v("listItemClicked @" + (bulletinCount - position - 1),
				"HOOOrAyyy!!!!");

		Intent intent = new Intent(ListItemActivity.this,
				BulletinViewActivity.class);
		intent.putExtra("BulletinId",
				(itemDetails.get(position).getBulletinId()));
		ListItemActivity.this.startActivity(intent);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			
			Intent intent = new Intent(ListItemActivity.this,
					GridItemActivity.class);
			this.startActivity(intent);
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

}
