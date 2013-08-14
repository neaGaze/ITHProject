package com.ith.project;

import java.util.HashMap;
import com.ith.project.EntityClasses.Employee;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.menu.CustomMenuListAdapter;
import com.ith.project.sqlite.EmployeeSQLite;
import android.app.Activity;
import android.app.Dialog;
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
	private EmployeeSQLite employeeSQLite;
	static CustomMenuListAdapter menuAdapter;
	private Dialog dialog;
	private static int position;
	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private HashMap<String, String> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		if (employeeSQLite != null)
			employeeSQLite.closeDB();
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		registerReceiver(broadcastReceiver, intentFilter);
		super.onResume();

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

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutEmployee);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.employee_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		position = bundle.getInt("EmployeeId");

		employeeSQLite = new EmployeeSQLite(EmployeeViewActivity.this);
		if (!employeeSQLite.isOpen())
			employeeSQLite.openDB();

		Employee ViewedEmployee = employeeSQLite.getViewedEmployee(position);

		Log.v("Employee Name", "" + ViewedEmployee.getEmployeeName());

		employeeName = (TextView) findViewById(R.id.textViewEmployeeName);
		employeeName.setText(ViewedEmployee.getEmployeeName());

		employeeGender = (TextView) findViewById(R.id.editTextEmployeeGender);
		employeeGender.setText(ViewedEmployee.getGender());

		employeeHomePhone = (TextView) findViewById(R.id.editTextEmployeeHomePhone);
		employeeHomePhone.setText(ViewedEmployee.getHomePhone());

		employeeMobile = (TextView) findViewById(R.id.editTextEmployeeMobile);
		employeeMobile.setText(ViewedEmployee.getMobile());

		employeeEmail = (TextView) findViewById(R.id.editTextEmployeeEmail);
		employeeEmail.setText(ViewedEmployee.getEmail());

		employeeAddress = (TextView) findViewById(R.id.editTextEmployeeAddress);
		employeeAddress.setText(ViewedEmployee.getAddress());

		employeeDesignation = (TextView) findViewById(R.id.editTextEmployeeDesignation);
		employeeDesignation.setText(ViewedEmployee.getDesignation());

		/** For top menu **/
		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(this);

		menuItems = new HashMap<String, String>();

	}

	public void onClick(View v) {

		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(BulletinButton)) {

		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			menuItems.put("Add Employee", "add_employee");
			menuItems.put("Edit Contents", "edit_user");
			menuItems.put("Send Web Message", "mail_web");
			menuItems.put("Send SMS", "mail_sms");
			menuItems.put("Phone Call", "call");
			new CallMenuDialog(this, dialog, menuItems);

		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public static int getPosition() {
		return position;
	}
}
