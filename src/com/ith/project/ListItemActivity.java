package com.ith.project;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.sdcard.BulletinLocal;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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
	private Bulletin bulletin;
	static ListItemArrayAdapter listItemArrAdapter;
	private static int bulletinCount;
	private ProgressDialog pdialog;

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
		init();

	}

	@Override
	public void onPause() {
		super.onPause();
		pdialog.dismiss();
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
				bulletinLocal = new BulletinLocal();
				bulletin = new Bulletin();

				inputJson = bulletin.getJsonUserLoginId(LoginAuthentication
						.getUserLoginId());

				Log.v("getBulletin inquiry", "" + inputJson.toString());

				/** To remove add Bulletin for normal users **/
				modifyBulletinAdd4Admin(LoginAuthentication.getUserRoleId());

				/** To establish connection to the web service **/
				String bulletinsFromWS = conn.getJSONFromUrl(inputJson, url);

				Log.v("Bulletins:", "" + bulletinsFromWS);

				/** Update the local file according to the web service **/
				bulletinLocal.updateLocalFiles(inputJson, bulletinsFromWS);

				/** Now read from the local file always **/
				JSONArray outputJson = bulletinLocal
						.getJSONFromLocal(inputJson);

				Log.v("JsonArray:", "" + outputJson.toString());

				itemDetails = setBulletin(outputJson);

				Log.v("Bulletins:",
						""
								+ bulletinLocal.getJSONFromLocal(inputJson)
										.toString());

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

						BulletinButton = (ImageButton) findViewById(R.id.bulletin_add_icon);
						BulletinButton
								.setOnClickListener(ListItemActivity.this);

						homeButton = (ImageButton) findViewById(R.id.home);
						homeButton.setOnClickListener(ListItemActivity.this);

						listView = (ListView) findViewById(R.id.listView1);

						Log.d("***Welcome to ListView***", ".......");
						listItemArrAdapter = new ListItemArrayAdapter(
								ListItemActivity.this, R.layout.list_items,
								itemDetails);
						listView.setAdapter(listItemArrAdapter);
						bulletinCount = listItemArrAdapter.getCount();
						listView.setOnItemClickListener(ListItemActivity.this);
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
		if (userRolesId == 2)
			findViewById(R.id.bulletin_add_icon).setVisibility(View.INVISIBLE);
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
			Toast.makeText(this, "Home Button", Toast.LENGTH_SHORT).show();
		}

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

	public static ArrayList<Bulletin> getBulletinArrayList() {
		return itemDetails;
	}
}
