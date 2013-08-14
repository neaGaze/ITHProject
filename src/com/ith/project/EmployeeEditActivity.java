package com.ith.project;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Employee;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.menu.CustomMenuListAdapter;
import com.ith.project.sqlite.EmployeeSQLite;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class EmployeeEditActivity extends Activity implements OnClickListener {

	private final String url = "EditEmployee";
	private final String mobileStrPattern = "\\d{9}";

	private HttpConnection conn;
	private int employeeId;
	private EditText employeeName;
	private RadioGroup employeeGenderGroup;
	private RadioButton employeeGender;
	private EditText employeeHomePhone;
	private EditText employeeMobile;
	private EditText employeeEmail;
	private EditText employeeAddress;
	private EditText employeeDesignation;
	private EditText employeeRemarks;
	private Button employeeSubmit;
	private ImageButton menuButton;
	private ImageButton homeButton;
	private Dialog dialog;
	private EmployeeSQLite employeeSQLite;
	static CustomMenuListAdapter menuAdapter;
	private Matcher matcher;
	private Pattern pattern, mobilePattern;
	private JSONObject insertEmployee;
	private String empName, empGender, empHomePhone, empMobile, empEmail,
			empAddress, empDesignation, empRemarks;
	private HashMap<String, String> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.employee_edit);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		init();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/************************************************************************************
	 * Initialize values first
	 * ***********************************************************************************/
	private void init() {

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutEmployee);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.employee_view, lin, false);
		int position = EmployeeViewActivity.getPosition();

		employeeSQLite = new EmployeeSQLite(EmployeeEditActivity.this);
		if (!employeeSQLite.isOpen())
			employeeSQLite.openDB();

		Employee EditedEmployee = employeeSQLite.getViewedEmployee(position);

		employeeSQLite.closeDB();
		Log.v("Employee Name Edit", "" + EditedEmployee.getEmployeeName());

		employeeId = EditedEmployee.getEmployeeId();

		employeeName = (EditText) findViewById(R.id.UserAddeditTextUserNameEmpEdit);
		employeeName.setText(EditedEmployee.getEmployeeName());

		employeeGenderGroup = (RadioGroup) findViewById(R.id.radioSexEmpEdit);
		String genderChk = EditedEmployee.getGender();
		if (genderChk.equals("Female")) {
			employeeGender = (RadioButton) findViewById(R.id.radioButton1EmpEdit);
			employeeGender.setChecked(false);
			employeeGender = (RadioButton) findViewById(R.id.radioButton2EmpEdit);
			employeeGender.setChecked(true);
		}
		employeeGender = (RadioButton) findViewById(employeeGenderGroup
				.getCheckedRadioButtonId());

		employeeHomePhone = (EditText) findViewById(R.id.UserAddeditTextHomePhoneEmpEdit);
		employeeHomePhone.setText(EditedEmployee.getHomePhone());

		employeeMobile = (EditText) findViewById(R.id.UserAddeditTextMobileEmpEdit);
		employeeMobile.setText(EditedEmployee.getMobile());

		employeeEmail = (EditText) findViewById(R.id.UserAddeditTextEmailEmpEdit);
		employeeEmail.setText(EditedEmployee.getEmail());

		employeeAddress = (EditText) findViewById(R.id.UserAddeditTextLocationEmpEdit);
		employeeAddress.setText(EditedEmployee.getAddress());

		employeeDesignation = (EditText) findViewById(R.id.UserAddeditTextDesignationEmpEdit);
		employeeDesignation.setText(EditedEmployee.getDesignation());

		employeeRemarks = (EditText) findViewById(R.id.UserAddeditTextRemarksEmpEdit);
		employeeRemarks.setText(EditedEmployee.getRemarks());

		employeeSubmit = (Button) findViewById(R.id.UserAddbuttonEditEmpEdit);
		employeeSubmit.setOnClickListener(this);

		/** For top menu **/
		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(this);

		pattern = Pattern.compile(EmployeeAddActivity.EMAIL_PATTERN);
		mobilePattern = Pattern.compile(mobileStrPattern);

		menuItems = new HashMap<String, String>();

	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {

			Intent intent = new Intent(EmployeeEditActivity.this,
					GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
		} else if (v.equals(homeButton)) {
			/** Set up the Menu **/
			menuItems.put("Add Employee", "add_employee");
			menuItems.put("Send Web Message", "mail_web");
			menuItems.put("Send SMS", "mail_sms");
			menuItems.put("Phone Call", "call");
			new CallMenuDialog(this, dialog, menuItems);

		} else {
			boolean correctEmail = false;

			empName = employeeName.getText().toString();
			if (empName.isEmpty())
				empName = employeeName.getText().toString();

			empGender = ((RadioButton) findViewById(employeeGenderGroup
					.getCheckedRadioButtonId())).getText().toString();

			empHomePhone = employeeHomePhone.getText().toString();
			if (empHomePhone.isEmpty())
				empHomePhone = employeeHomePhone.getText().toString();

			empMobile = employeeMobile.getText().toString();
			if (empMobile.isEmpty())
				empMobile = employeeMobile.getText().toString();

			empEmail = employeeEmail.getText().toString();
			if (empEmail.isEmpty())
				empEmail = employeeEmail.getText().toString();

			empAddress = employeeAddress.getText().toString();
			if (empAddress.isEmpty())
				empAddress = employeeAddress.getText().toString();

			empDesignation = employeeDesignation.getText().toString();
			if (empDesignation.isEmpty())
				empDesignation = employeeDesignation.getText().toString();

			empRemarks = employeeRemarks.getText().toString();
			if (empRemarks.isEmpty())
				empRemarks = employeeRemarks.getText().toString();

			/** Make a json object out of form fields **/
			insertEmployee = Employee.makeNewEditEmployeeJSON(employeeId,
					empName, empGender, empHomePhone, empMobile, empEmail,
					empAddress, empDesignation, empRemarks);

			/** Verify Email address **/
			correctEmail = emailValidate(empEmail);

			/** Verify phone length **/
			boolean correctPhone = phoneValidate(empHomePhone);

			/** Verify mobie length **/
			boolean correctMobile = mobileValidate(empMobile);

			/** send the JSONObject to update the employees in the database **/
			if (correctEmail && correctPhone && correctMobile) {

				new Thread(new Runnable() {

					public void run() {

						conn = HttpConnection.getSingletonConn();

						String insertStatusStr = conn.getJSONFromUrl(
								insertEmployee, url);

						Log.e("EMPLOYEE EDIT RETURN WEB", "" + insertStatusStr);
						try {

							JSONObject insertStatusJson = new JSONObject(
									insertStatusStr);

							boolean insertStatus = (Boolean) insertStatusJson
									.get("EditEmployeeResult");

							if (insertStatus) {
								Log.e("Succesfully Edited Employee ",
										"EditStatus From Web Service is: "
												+ insertStatus);
							} else {
								Log.e("Problem Editing Employee ",
										"EditStatus From Web Service is: "
												+ insertStatus);
							}

						} catch (JSONException e) {
							Log.e("JSONException", "" + e.getMessage());
							e.printStackTrace();
						}

						runOnUiThread(new Runnable() {

							public void run() {

								Intent intent = new Intent(
										EmployeeEditActivity.this,
										EmployeeListActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);

							}
						});
					}
				}).start();
			} else if (!correctEmail) {
				Toast.makeText(EmployeeEditActivity.this, "Invalid Email",
						Toast.LENGTH_SHORT).show();
			} else if (!correctPhone) {
				Toast.makeText(EmployeeEditActivity.this,
						"Invalid Phone length", Toast.LENGTH_SHORT).show();
			} else if (!correctMobile) {
				Toast.makeText(EmployeeEditActivity.this,
						"Invalid Mobile length", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(EmployeeEditActivity.this,
						"Some Incorrect Fields", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/***********************************************************************************************************
	 * TO validate E-mail address
	 * *********************************************************************************************************/
	public boolean emailValidate(final String email) {

		matcher = pattern.matcher(email);
		return matcher.matches();

	}

	/***********************************************************************************************************
	 * To validate Phone Number length
	 * *********************************************************************************************************/
	public boolean phoneValidate(final String phone) {

		try {
			if (empHomePhone.equals(employeeHomePhone.getText().toString()))
				return true;
			int pNumber = Integer.parseInt(phone);
			if ((pNumber < Integer.MAX_VALUE) && (pNumber > 999999))
				return true;
			else
				return false;

		} catch (NumberFormatException e) {
			Log.e("Phone length is null", "Please fill the phone field");
			Toast.makeText(this, "Phone Field is Empty", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

	}

	/***********************************************************************************************************
	 * To validate Mobile length
	 * *********************************************************************************************************/
	public boolean mobileValidate(final String mobile) {

		try {
			if (empMobile.equals(employeeMobile.getText().toString()))
				return true;
			matcher = mobilePattern.matcher(mobile);
			return matcher.matches();

		} catch (NumberFormatException e) {
			Log.e("Phone length is null", "Please fill the phone field");
			Toast.makeText(this, "Phone Field is Empty", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

	}
}
