package com.ith.project;

import java.util.ArrayList;

import com.ith.project.menu.CustomMenu;
import com.ith.project.menu.CustomMenuListAdapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GridItemActivity extends Activity implements OnItemClickListener,
		OnClickListener {

	private static final int MENU_EXIT = 0;

	private ParseListItem parser;
	private ArrayList<GridItemDetails> gridItemDetails;
	private GridView gridView;
	private ProgressDialog pdialog;
	private ImageButton menuButton;
	private ImageButton BulletinButton;
	private ImageButton homeButton;
	private LinearLayout linLayoutMenu;
	private ListView menuListView;
	static ListItemArrayAdapter listItemArrAdapter;
	static CustomMenuListAdapter menuAdapter;
	private static GridItemActivity context;
	private Dialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.grid_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		context = this;
		init();
	}

	@Override
	public void onPause() {
		super.onPause();
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
		/** To remove add Bulletin for normal users **/
		// if (LoginAuthentication.getUserRoleId() == 2)
		// findViewById(R.id.bulletin_add_icon).setVisibility(View.INVISIBLE);

		parser = new ParseListItem(this, "GRID_ITEM");
		this.gridItemDetails = parser.getGridItemDetails();

		// BulletinButton = (ImageButton) findViewById(R.id.bulletin_add_icon);
		// BulletinButton.setOnClickListener(this);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(this);

		gridView = (GridView) findViewById(R.id.gridView);
		gridView.setAdapter(new GridAdapter(this, gridItemDetails));
		gridView.setOnItemClickListener(this);
		pdialog.dismiss();

	}

	/******************************************************************************************
	 * An implementation of GridAdapter
	 * *************************************************************************************/
	private class GridAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<GridItemDetails> gridItemDetails;

		public GridAdapter(Context context,
				ArrayList<GridItemDetails> gridItemDetails) {

			this.context = context;
			this.gridItemDetails = gridItemDetails;
		}

		public int getCount() {
			return gridItemDetails.size();
		}

		public GridItemDetails getItem(int arg0) {

			return gridItemDetails.get(arg0);
		}

		public long getItemId(int position) {

			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View gridView;

			if (convertView == null) {

				gridView = new View(context);

				/** get layout from mobile.xml **/
				gridView = inflater.inflate(R.layout.grid_items, null);

				/** set value into textview **/
				TextView textView = (TextView) gridView
						.findViewById(R.id.textViewMenuName);
				textView.setText(gridItemDetails.get(position).getMenuName());

				/** set image based on selected text **/
				ImageView imageView = (ImageView) gridView
						.findViewById(R.id.imageViewMenuIcon);

				int id = getResources().getIdentifier(
						gridItemDetails.get(position).getMenuIcon(),
						"drawable", getApplicationContext().getPackageName());

				// imageView.setImageResource(R.drawable.user1);
				imageView.setImageResource(id);

			} else {
				gridView = (View) convertView;
			}

			return gridView;
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

	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {

		Log.v("GridItemClicked @" + (position), "Great!!!!");
		pdialog.show();

		switch (position) {
		case 0: {
			this.startActivity(new Intent(this, EmployeeListActivity.class));
			break;
		}
		case 1: {
			this.startActivity(new Intent(this, MessageListActivity.class));
			break;
		}
		case 2: {

			Log.e("This is EVENT LIST opening " + (position), "Great!!!!");
			break;
		}
		case 3: {

			Log.e("This is LEAVE PAGE opening " + (position), "Great!!!!");
			break;
		}
		}
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
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			// this.finish();
			pdialog.dismiss();
		} else if (v.equals(BulletinButton)) {
			pdialog.show();
			Intent intent = new Intent(GridItemActivity.this,
					BulletinAddActivity.class);
			this.startActivity(intent);
			this.finish();

		} else if (v.equals(homeButton)) {
			callMenuDialog();
		}
	}

	/****************************************************************************
	 * When the Home Button is Clicked at the Menu
	 *************************************************************************/
	private void callMenuDialog() {

		LayoutInflater menuInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		linLayoutMenu = (LinearLayout) findViewById(R.id.linearLayoutCustomMenu_2);
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
		tempArrList.add(setMenuItems("Exit", "exit"));

		/** Call adapter for the list View to populate ArrayList items **/
		menuAdapter = new CustomMenuListAdapter(GridItemActivity.this,
				R.layout.custom_menu_2, tempArrList);
		menuListView.setAdapter(menuAdapter);
		menuListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {

				TextView c = (TextView) view
						.findViewById(R.id.textViewCustomMenu_2);
				String keyword = c.getText().toString();

				/** When "Exit" menu item is pressed **/
				if (keyword.equals("Exit")) {
					pdialog.show();
					GridItemActivity.this.finish();
					GridItemActivity.getGridItemActivityInstance().finish();
					ListItemActivity.getListItemActivityInstance().finish();
				}
			}
		});

		dialog.show();

	}

	public CustomMenu setMenuItems(String menuString, String menuIcon) {

		CustomMenu menu = new CustomMenu(menuString, menuIcon);
		menu.setValues(menuString, menuIcon);

		return menu;
	}

	/****************************************************************************
	 * Get the Activity instance of GridItemActivity
	 *************************************************************************/
	public static GridItemActivity getGridItemActivityInstance() {
		return context;
	}
}
