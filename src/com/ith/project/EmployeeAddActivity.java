package com.ith.project;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EmployeeAddActivity extends Activity implements OnClickListener {

	private final String url = "http://192.168.100.2/EMSWebService/Service1.svc/json/InsertEmployees";
	public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private EditText employeeName;
	private RadioGroup employeeGenderGroup;
	private RadioButton employeeGender;
	private EditText employeeHomePhone;
	private EditText employeeMobile;
	private EditText employeeEmail;
	private EditText employeeAddress;
	private EditText employeeUserName;
	private EditText employeePassword1;
	private EditText employeePassword2;
	private EditText employeeDesignation;
	private EditText employeeRemarks;
	private ImageButton employeeSubmit;
	private Employee employeeAdd;
	private String empName, empGender, empHomePhone, empMobile, empEmail,
			empAddress, empDesignation, empRemarks, empUserName, empPwd1,
			empPwd2;
	private ProgressDialog pdialog;
	private JSONObject insertEmployee;
	private HttpConnection conn;
	private ImageButton menuButton;
	private ImageButton homeButton;
	private LinearLayout linLayoutMenu;
	private ListView menuListView;
	static CustomMenuListAdapter menuAdapter;
	private Dialog dialog;
	private Matcher matcher;
	private Pattern pattern;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.employee_add);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();
	}

	private void init() {
		employeeAdd = new Employee();
		employeeGenderGroup = (RadioGroup) findViewById(R.id.radioSex);
		employeeGender = (RadioButton) findViewById(employeeGenderGroup
				.getCheckedRadioButtonId());
		employeeName = (EditText) findViewById(R.id.UserAddeditTextUserName);
		employeeHomePhone = (EditText) findViewById(R.id.UserAddeditTextHomePhone);
		employeeMobile = (EditText) findViewById(R.id.UserAddeditTextMobile);
		employeeEmail = (EditText) findViewById(R.id.UserAddeditTextEmail);
		employeeAddress = (EditText) findViewById(R.id.UserAddeditTextLocation);
		employeeUserName = (EditText) findViewById(R.id.UserAddeditTextUsername);
		employeePassword1 = (EditText) findViewById(R.id.UserAddeditTextPassword1);
		employeePassword2 = (EditText) findViewById(R.id.UserAddeditTextPassword2);
		employeeDesignation = (EditText) findViewById(R.id.UserAddeditTextDesignation);
		employeeRemarks = (EditText) findViewById(R.id.UserAddeditTextRemarks);
		employeeSubmit = (ImageButton) findViewById(R.id.UserAddbuttonEdit);
		employeeSubmit.setOnClickListener(this);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(EmployeeAddActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(EmployeeAddActivity.this);

		pattern = Pattern.compile(EMAIL_PATTERN);

		pdialog.dismiss();

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

	public void onClick(View v) {

		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(homeButton)) {
			callMenuDialog();
		} else {
			boolean correctEmail = false;
			// pdialog.show();
			empName = employeeName.getText().toString();
			empGender = employeeGender.getText().toString();
			empHomePhone = employeeHomePhone.getText().toString();
			empMobile = employeeMobile.getText().toString();
			empEmail = employeeEmail.getText().toString();
			empAddress = employeeAddress.getText().toString();
			empUserName = employeeUserName.getText().toString();
			empPwd1 = employeePassword1.getText().toString();
			empPwd2 = employeePassword2.getText().toString();
			empDesignation = employeeDesignation.getText().toString();
			empRemarks = employeeRemarks.getText().toString();

			/** Make a json object out of form fields **/
			insertEmployee = employeeAdd.makeNewEmployeeJSON(empName,
					empGender, empHomePhone, empMobile, empEmail, empAddress,
					empDesignation, empRemarks);

			/** Verify Email address **/
			correctEmail = emailValidate(empEmail);

			/** Verify Correct Passwords **/
			boolean correctPwd = pwdValidate(empPwd1, empPwd2);

			/** Verify Empty fields **/
			boolean fieldsFilled = FieldsValidate(empName, empGender,
					empMobile, empEmail, empAddress, empDesignation,
					empUserName, empPwd1, empPwd2);

			/** Verify phone length **/
			boolean correctPhone = phoneValidate(empMobile);

			/** send the JSONObject to update the employees in the database **/
			if (correctEmail && correctPhone && correctPwd) {

				new Thread(new Runnable() {

					public void run() {

						conn = HttpConnection.getSingletonConn();

						String insertStatusStr = conn.getJSONFromUrl(
								insertEmployee, url);

						try {

							JSONObject insertStatusJson = new JSONObject(
									insertStatusStr);

							boolean insertStatus = (Boolean) insertStatusJson
									.get("InsertEmployee");

							if (insertStatus) {
								Toast.makeText(EmployeeAddActivity.this,
										"Successfully Employee Added",
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(EmployeeAddActivity.this,
										"Problem Adding Employee",
										Toast.LENGTH_SHORT).show();

								Log.e("Problem Adding Employee ",
										"InsertStatus From Web Service"
												+ insertStatus);
							}

						} catch (JSONException e) {
							Log.e("JSONException", "" + e.getMessage());
							e.printStackTrace();
						}

						runOnUiThread(new Runnable() {

							public void run() {

								Intent intent = new Intent(
										EmployeeAddActivity.this,
										EmployeeListActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);

								// EmployeeAddActivity.this.finish();
							}
						});
					}
				}).start();
			} else if (!fieldsFilled) {
				Toast.makeText(EmployeeAddActivity.this,
						"One Or More Fields Empty", Toast.LENGTH_SHORT).show();
			} else if (!correctEmail)
				Toast.makeText(EmployeeAddActivity.this, "Invalid Email",
						Toast.LENGTH_SHORT).show();
			else if (!correctPhone)
				Toast.makeText(EmployeeAddActivity.this,
						"Invalid Phone length", Toast.LENGTH_SHORT).show();
			else if (!correctPwd)
				Toast.makeText(EmployeeAddActivity.this,
						"Password Don't Match", Toast.LENGTH_SHORT).show();
			else {
				Toast.makeText(EmployeeAddActivity.this,
						"Some Incorrect Fields", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/*******************************************************************************
	 * To validate 2 password field for correct password
	 ********************************************************************************/
	private boolean pwdValidate(String pwd1, String pwd2) {

		if (pwd1.equals(pwd2))
			return true;
		else
			return false;
	}

	/*****************************************************************************************
	 * TO validate If Fields are empty or not
	 * ******************************************************************************************/
	private boolean FieldsValidate(String empName2, String empGender2,
			String empMobile2, String empEmail2, String empAddress2,
			String empDesignation2, String empUsername2, String empPwd1,
			String empPwd2) {

		boolean tmpFlag = true;
		if (empName2.isEmpty() || empGender2.isEmpty()
				|| empUsername2.isEmpty() || empMobile2.isEmpty()
				|| empEmail2.isEmpty() || empAddress2.isEmpty()
				|| empDesignation2.isEmpty() || empPwd1.isEmpty()
				|| empPwd2.isEmpty())
			tmpFlag = false;

		return tmpFlag;
	}

	/***********************************************************************************************************
	 * TO validate E-mail address
	 * *********************************************************************************************************/
	public boolean emailValidate(final String email) {

		matcher = pattern.matcher(email);
		return matcher.matches();

	}

	/***********************************************************************************************************
	 * TO validate Phone Number length
	 * *********************************************************************************************************/
	public boolean phoneValidate(final String phone) {

		try {

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

		tempArrList.add(setMenuItems("Exit", "exit"));

		menuAdapter = new CustomMenuListAdapter(EmployeeAddActivity.this,
				R.layout.custom_menu_2, tempArrList);
		menuListView.setAdapter(menuAdapter);
		menuListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {

				TextView c = (TextView) view
						.findViewById(R.id.textViewCustomMenu_2);
				String keyword = c.getText().toString();

				/** When "Add Employee" menu item is pressed **/
				if (keyword.equals("Exit")) {
					pdialog.show();
					EmployeeAddActivity.this.finish();
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

}
