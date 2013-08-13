package com.ith.project;

import java.util.HashMap;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.menu.CustomMenuListAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
	private static int position;
	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private CallMenuDialog callDiag;
	private HashMap<String, String> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
/*
		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();*/

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
		/*pdialog.dismiss();*/
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		registerReceiver(broadcastReceiver, intentFilter);
		super.onResume();
		/*pdialog.dismiss();*/
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
		modifyEmployeeAdd4Admin(LoginAuthentication.UserRolesId);

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

		menuItems = new HashMap<String, String>();
		/*pdialog.dismiss();*/
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

			/*pdialog.show()*/;
			// Toast.makeText(this, "Add Bulletin", Toast.LENGTH_SHORT).show();
			// Intent intent = new Intent(this, EmployeeAddActivity.class);
			// this.startActivity(intent);

		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			menuItems.put("Add Employee", "add_employee");
			menuItems.put("Edit Contents", "edit_user");
			menuItems.put("Send Web Message", "mail_web");
			menuItems.put("Send SMS", "mail_sms");
			menuItems.put("Phone Call", "call");
		//	menuItems.put("Exit", "exit");
			callDiag = new CallMenuDialog(this,/* pdialog,*/ dialog, menuItems);
			// callMenuDialog();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something on back.
			/*pdialog.show();*/
			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public static int getPosition(){
		return position;
	}
}
