package com.ith.project;

import java.util.ArrayList;

import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.menu.CustomMenu;
import com.ith.project.menu.CustomMenuListAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EmployeeViewActivity extends Activity implements OnClickListener {

	private TextView employeeName;
	private TextView employeeGender;
	private TextView employeeHomePhone;
	private TextView employeeMobile;
	private TextView employeeEmail;
	private TextView employeeAddress;
	private TextView employeeDesignation;
	private ImageButton menuButton;
	private ImageButton BulletinButton;
	private ImageButton homeButton;
	private ProgressDialog pdialog;
	private LinearLayout linLayoutMenu;
	private ListView menuListView;
	static CustomMenuListAdapter menuAdapter;
	private Dialog dialog;
	private int position;
	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.employee_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();
	}

	@Override
	public void onPause() {
		unregisterReceiver(broadcastReceiver);
		super.onPause();
		pdialog.dismiss();
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		registerReceiver(broadcastReceiver, intentFilter);
		super.onResume();
		pdialog.dismiss();
	}

	private void init() {

		/** Register Intent Filter with only SMS RECEIVED ACTION **/
		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");

		/** Register BroadCastReceiver **/
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle bundle = intent.getExtras();
				String sender = bundle.getString("MSG_SENDER_NUMBER");
				String msg = bundle.getString("MSG_BODY");

				Log.e("SMS RECEIVED SUCCESSFULLY", "read it from android sms");
				Log.e("SMS SENDER is", "" + sender);
				Log.e("SMS BODY is", "" + msg);
			}
		};

		/** To remove add Bulletin for normal users **/
		modifyEmployeeAdd4Admin(LoginAuthentication.getUserRoleId());

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutEmployee);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.employee_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		position = bundle.getInt("PositionOfEmployee");

		Log.v("Employee Name", ""
				+ EmployeeListActivity.getEmployeeArrayList().get(position)
						.getEmployeeName());

		employeeName = (TextView) findViewById(R.id.textViewEmployeeName);
		employeeName.setText(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getEmployeeName());

		employeeGender = (TextView) findViewById(R.id.editTextEmployeeGender);
		employeeGender.setText(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getGender());

		employeeHomePhone = (TextView) findViewById(R.id.editTextEmployeeHomePhone);
		employeeHomePhone.setText(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getHomePhone());

		employeeMobile = (TextView) findViewById(R.id.editTextEmployeeMobile);
		employeeMobile.setText(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getMobile());

		employeeEmail = (TextView) findViewById(R.id.editTextEmployeeEmail);
		employeeEmail.setText(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getEmail());

		employeeAddress = (TextView) findViewById(R.id.editTextEmployeeAddress);
		employeeAddress.setText(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getAddress());

		employeeDesignation = (TextView) findViewById(R.id.editTextEmployeeDesignation);
		employeeDesignation.setText(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getDesignation());

		/** For top menu **/
		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(this);

		// BulletinButton = (ImageButton) findViewById(R.id.bulletin_add_icon);
		// BulletinButton.setOnClickListener(this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(this);

		pdialog.dismiss();
	}

	/*********************************************************************************
	 * Called when the LExit Button is Clicked
	 * ******************************************************************************/
	public void modifyEmployeeAdd4Admin(int userRolesId) {
		// if (userRolesId == 2)
		// findViewById(R.id.bulletin_add_icon).setVisibility(View.INVISIBLE);
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
			// Intent intent = new Intent(this, EmployeeAddActivity.class);
			// this.startActivity(intent);

		} else if (v.equals(homeButton)) {
			callMenuDialog();
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
		if (LoginAuthentication.getUserRoleId() == 1) {
			tempArrList.add(setMenuItems("Add Employee", "add_employee"));
			tempArrList.add(setMenuItems("Edit Contents", "edit_user"));
		}
		tempArrList.add(setMenuItems("Send Web Message", "mail_web"));
		tempArrList.add(setMenuItems("Send SMS", "mail_sms"));
		tempArrList.add(setMenuItems("Phone Call", "call"));
		tempArrList.add(setMenuItems("Exit", "exit"));

		menuAdapter = new CustomMenuListAdapter(EmployeeViewActivity.this,
				R.layout.custom_menu_2, tempArrList);
		menuListView.setAdapter(menuAdapter);
		menuListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {

				TextView c = (TextView) view
						.findViewById(R.id.textViewCustomMenu_2);
				String keyword = c.getText().toString();

				/** When "Add Employee" menu item is pressed **/
				if (keyword.equals("Add Employee")) {
					pdialog.show();
					Intent intent = new Intent(EmployeeViewActivity.this,
							EmployeeAddActivity.class);
					EmployeeViewActivity.this.startActivity(intent);
					EmployeeViewActivity.this.finish();

				}/** When "Edit Employee" menu item is pressed **/
				else if (keyword.equals("Edit Contents")) {

					pdialog.show();
					Intent intent = new Intent(EmployeeViewActivity.this,
							EmployeeEditActivity.class);
					intent.putExtra("PositionOfEmployeeEdit",
							EmployeeViewActivity.this.position);
					EmployeeViewActivity.this.startActivity(intent);
					EmployeeViewActivity.this.finish();

				}/** When "Send Web Message" menu item is pressed **/
				else if (keyword.equals("Send Web Message")) {

					pdialog.show();
					Intent intent = new Intent(EmployeeViewActivity.this,
							MessageAddActivity.class);
					intent.putExtra(
							"FROM_EMPLOYEE_VIEW",
							EmployeeListActivity.getEmployeeArrayList()
									.get(EmployeeViewActivity.this.position)
									.getMobile());
					EmployeeViewActivity.this.startActivity(intent);
					EmployeeViewActivity.this.finish();

				}/** When "Send SMS" menu item is pressed **/
				else if (keyword.equals("Send SMS")) {

					pdialog.show();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.putExtra(
							"address",
							EmployeeListActivity.getEmployeeArrayList()
									.get(EmployeeViewActivity.this.position)
									.getMobile());
					intent.setType("vnd.android-dir/mms-sms");
					EmployeeViewActivity.this.startActivity(intent);

				}/** When "Call" menu item is pressed **/
				else if (keyword.equals("Phone Call")) {
					pdialog.show();
					Intent intent = new Intent(Intent.ACTION_CALL);
					intent.setData(Uri.parse("tel:"
							+ EmployeeListActivity.getEmployeeArrayList()
									.get(EmployeeViewActivity.this.position)
									.getMobile()));
					EmployeeViewActivity.this.startActivity(intent);

				}
				/** When "Exit" menu item is pressed **/
				else if (keyword.equals("Exit")) {
					pdialog.show();
					EmployeeViewActivity.this.finish();
					EmployeeListActivity.getEmployeeListActivityInstance()
							.finish();
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

}
