package com.ith.project;

import java.util.ArrayList;

import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.menu.CustomMenu;
import com.ith.project.menu.CustomMenuListAdapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BulletinViewActivity extends Activity implements OnClickListener {

	private TextView bulletinTitle;
	private TextView bulletinDesc;
	private TextView bulletinAuthor;
	private TextView bulletinDate;
	private ImageButton menuButton;
	private ImageButton BulletinButton;
	private ImageButton homeButton;
	public ProgressDialog pdialog;
	private LinearLayout linLayoutMenu;
	private ListView menuListView;
	static ListItemArrayAdapter listItemArrAdapter;
	private CustomMenuListAdapter menuAdapter;
	private Dialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.bulletin_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
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

	private void init() {

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutBulletin);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.bulletin_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		int position = bundle.getInt("PositionOfBulletin");

		Log.v("bulletinTitle", ""
				+ ListItemActivity.getBulletinArrayList().get(position)
						.getTitle());

		bulletinTitle = (TextView) findViewById(R.id.editTextBulletinTitleView);
		bulletinTitle.setText(ListItemActivity.getBulletinArrayList()
				.get(position).getTitle());

		bulletinDesc = (TextView) findViewById(R.id.editTextBulletinDescView);
		bulletinDesc.setText(ListItemActivity.getBulletinArrayList()
				.get(position).getDescription());

		// bulletinAuthor = (TextView)
		// findViewById(R.id.editTextBulletinAuthor);
		// bulletinAuthor.setText(ListItemActivity.getBulletinArrayList()
		// .get(position).getEmployeeName());

		bulletinDate = (TextView) findViewById(R.id.editTextBulletinDate);
		bulletinDate.setText(ListItemActivity.getBulletinArrayList()
				.get(position).getDate()
				+ "  "
				+ ListItemActivity.getBulletinArrayList().get(position)
						.getTime());

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(BulletinViewActivity.this);

		// BulletinButton = (ImageButton) findViewById(R.id.bulletin_add_icon);
		// BulletinButton.setOnClickListener(BulletinViewActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(BulletinViewActivity.this);

		pdialog.dismiss();
	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(BulletinButton)) {

			pdialog.show();
			// Toast.makeText(this, "Add Bulletin", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, BulletinAddActivity.class);
			this.startActivity(intent);

		} else if (v.equals(homeButton)) {
			callMenuDialog();
		}
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

		menuAdapter = new CustomMenuListAdapter(BulletinViewActivity.this,
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
					pdialog.show();
					BulletinViewActivity.this.finish();
					Intent intent = new Intent(BulletinViewActivity.this,
							BulletinAddActivity.class);
					BulletinViewActivity.this.startActivity(intent);
				}
				/** When "Exit" menu item is pressed **/
				else if (keyword.equals("Exit")) {
					pdialog.show();
					BulletinViewActivity.this.finish();
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

	/*********************************************************************************
	 * Called when the LExit Button is Clicked
	 * ******************************************************************************/
	public void modifyBulletinAdd4Admin(int userRolesId) {
		// if (userRolesId == 2)
		// findViewById(R.id.bulletin_add_icon).setVisibility(View.INVISIBLE);
	}
}
