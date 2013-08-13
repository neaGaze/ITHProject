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

		if (CallMenuDialog.Exit || GridItemActivity.Exit) {
			CallMenuDialog.Exit = false;
			GridItemActivity.Exit = false;
			this.finish();
		} else {
			/*
			 * pdialog = new ProgressDialog(this); pdialog.setCancelable(true);
			 * pdialog.setMessage("Loading ...."); pdialog.show();
			 */
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
		/* pdialog.dismiss(); */
		if (dialog != null)
			dialog.dismiss();
		this.finish();
	}

	@Override
	public void onResume() {

		super.onResume();
		/* pdialog.dismiss(); */
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
				if (!bulletinSQLite.isOpen())
					bulletinSQLite.openDB();

				bulletin = new Bulletin();

				/** Make a json Object out of UserLoginId **/
				inputJson = bulletin
						.getJsonUserLoginId(LoginAuthentication.UserloginId);

				Log.v("getBulletin inquiry", "" + inputJson.toString());

				/** To remove add Bulletin for normal users **/
			//	modifyBulletinAdd4Admin(LoginAuthentication.UserRolesId);

				/** To establish connection to the web service **/
				String bulletinsFromWS = conn.getJSONFromUrl(inputJson, url);
				connFlag = true;
				Log.e("Bulletins:", "here: " + bulletinsFromWS);

				/** Update the local file according to the web service **/
				// bulletinLocal.updateLocalFiles(inputJson, bulletinsFromWS);
				if (bulletinsFromWS.startsWith("{")
				/* || (!bulletinsFromWS.equals("")) */) {
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
				homeButton = (ImageButton) findViewById(R.id.home);
				homeButton.setClickable(false);

				menuButton = (ImageButton) findViewById(R.id.menu);
				menuButton.setOnClickListener(ListItemActivity.this);

				// registerForContextMenu(homeButton);

				homeButton = (ImageButton) findViewById(R.id.home);
				homeButton.setClickable(false);
				homeButton.setClickable(true);
				homeButton.setOnClickListener(ListItemActivity.this);

				menuButton = (ImageButton) findViewById(R.id.menu);
				menuButton.setOnClickListener(ListItemActivity.this);
				menuItems = new HashMap<String, String>();
				/**
				 * To run the main thread after completion of the connection
				 * thread
				 **/
				if (itemDetails != null)
				/** If list is null do not run this thread :D **/
				{
					runOnUiThread(new Runnable() {

						public void run() {

							// ExitBut = (Button) findViewById(R.id.exitButton);
							// ExitBut.setOnClickListener(ListItemActivity.this);

							listView = (ListView) findViewById(R.id.listView1);

							Log.d("***Welcome to ListView***", ".......");
							listItemArrAdapter = new ListItemArrayAdapter(
									ListItemActivity.this, R.layout.list_items,
									itemDetails);
							listView.setAdapter(listItemArrAdapter);
							bulletinCount = listItemArrAdapter.getCount();
							listView.setOnItemClickListener(ListItemActivity.this);
							menuItems = new HashMap<String, String>();
							/* pdialog.dismiss(); */
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
			/* pdialog.show(); */
			Intent intent = new Intent(ListItemActivity.this,
					GridItemActivity.class);
			this.startActivity(intent);
			// this.finish();
		} else if (v.equals(BulletinButton)) {
			/* pdialog.show(); */
			// Toast.makeText(this, "Add Bulletin", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(ListItemActivity.this,
					BulletinAddActivity.class);
			this.startActivity(intent);
			this.finish();

		} else if (v.equals(homeButton)) {
			/** Set up the Menu **/
			// menuItems.put("Exit", "exit");
			menuItems.put("Add Bulletin", "add_employee");
			callDiag = new CallMenuDialog(this, /* pdialog, */dialog, menuItems);
			// callMenuDialog();
		} else if (v.equals(linLayoutMenu)) {
			Toast.makeText(this, "Add Employee Clicked", Toast.LENGTH_SHORT)
					.show();
		} else
			Toast.makeText(this, "Menu Item Clicked", Toast.LENGTH_SHORT)
					.show();

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

		/* pdialog.show(); */

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
			/* pdialog.show(); */
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

	public static boolean getConnFlag() {
		return connFlag;
	}
}
