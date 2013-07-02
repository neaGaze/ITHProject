package com.ith.project;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Employee;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EmployeeEditActivity extends Activity implements OnClickListener {

	private final String url = "http://192.168.100.2/EMSWebService/Service1.svc/json/EditEmployee";
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
	private ProgressDialog pdialog;
	private Dialog dialog;
	private LinearLayout linLayoutMenu;
	private ListView menuListView;
	static CustomMenuListAdapter menuAdapter;
	private Context context;
	private Matcher matcher;
	private Pattern pattern, mobilePattern;
	private JSONObject insertEmployee;
	private String empName, empGender, empHomePhone, empMobile, empEmail,
			empAddress, empDesignation, empRemarks;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.employee_edit);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		context = this;
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

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutEmployee);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.employee_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		int position = bundle.getInt("PositionOfEmployeeEdit");

		Log.v("Employee Name Edit", ""
				+ EmployeeListActivity.getEmployeeArrayList().get(position)
						.getEmployeeName());

		employeeId = EmployeeListActivity.getEmployeeArrayList().get(position)
				.getEmployeeId();

		employeeName = (EditText) findViewById(R.id.UserAddeditTextUserNameEmpEdit);
		employeeName.setHint(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getEmployeeName());

		employeeGenderGroup = (RadioGroup) findViewById(R.id.radioSexEmpEdit);
		String genderChk = EmployeeListActivity.getEmployeeArrayList()
				.get(position).getGender();
		if (genderChk.equals("female")) {
			employeeGender = (RadioButton) findViewById(R.id.radioButton1EmpEdit);
			employeeGender.setChecked(false);
			employeeGender = (RadioButton) findViewById(R.id.radioButton2EmpEdit);
			employeeGender.setChecked(true);
		}
		employeeGender = (RadioButton) findViewById(employeeGenderGroup
				.getCheckedRadioButtonId());

		employeeHomePhone = (EditText) findViewById(R.id.UserAddeditTextHomePhoneEmpEdit);
		employeeHomePhone.setHint(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getHomePhone());

		employeeMobile = (EditText) findViewById(R.id.UserAddeditTextMobileEmpEdit);
		employeeMobile.setHint(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getMobile());

		employeeEmail = (EditText) findViewById(R.id.UserAddeditTextEmailEmpEdit);
		employeeEmail.setHint(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getEmail());

		employeeAddress = (EditText) findViewById(R.id.UserAddeditTextLocationEmpEdit);
		employeeAddress.setHint(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getAddress());

		employeeDesignation = (EditText) findViewById(R.id.UserAddeditTextDesignationEmpEdit);
		employeeDesignation.setHint(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getDesignation());

		employeeRemarks = (EditText) findViewById(R.id.UserAddeditTextRemarksEmpEdit);
		employeeRemarks.setHint(EmployeeListActivity.getEmployeeArrayList()
				.get(position).getRemarks());

		employeeSubmit = (Button) findViewById(R.id.UserAddbuttonEditEmpEdit);
		employeeSubmit.setOnClickListener(this);

		/** For top menu **/
		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(this);

		pattern = Pattern.compile(EmployeeAddActivity.EMAIL_PATTERN);
		mobilePattern = Pattern.compile(mobileStrPattern);
		pdialog.dismiss();

	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {
			pdialog.show();
			Intent intent = new Intent(EmployeeEditActivity.this,
					GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
		} else if (v.equals(homeButton))
			callMenuDialog();
		else {
			boolean correctEmail = false;
			// pdialog.show();
			empName = employeeName.getText().toString();
			if (empName.isEmpty())
				empName = employeeName.getHint().toString();

			empGender = ((RadioButton) findViewById(employeeGenderGroup
					.getCheckedRadioButtonId())).getText().toString();

			empHomePhone = employeeHomePhone.getText().toString();
			if (empHomePhone.isEmpty())
				empHomePhone = employeeHomePhone.getHint().toString();

			empMobile = employeeMobile.getText().toString();
			if (empMobile.isEmpty())
				empMobile = employeeMobile.getHint().toString();

			empEmail = employeeEmail.getText().toString();
			if (empEmail.isEmpty())
				empEmail = employeeEmail.getHint().toString();

			empAddress = employeeAddress.getText().toString();
			if (empAddress.isEmpty())
				empAddress = employeeAddress.getHint().toString();

			empDesignation = employeeDesignation.getText().toString();
			if (empDesignation.isEmpty())
				empDesignation = employeeDesignation.getHint().toString();

			empRemarks = employeeRemarks.getText().toString();
			if (empRemarks.isEmpty())
				empRemarks = employeeRemarks.getHint().toString();

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

								// EmployeeAddActivity.this.finish();
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
			if (empHomePhone.equals(employeeHomePhone.getHint().toString()))
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
			if (empMobile.equals(employeeMobile.getHint().toString()))
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

	/***********************************************************************************************************
	 * For Handling Menu Items
	 * *********************************************************************************************************/
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
		}
		tempArrList.add(setMenuItems("Send Web Message", "mail_web"));
		tempArrList.add(setMenuItems("Send SMS", "mail_sms"));
		tempArrList.add(setMenuItems("Phone Call", "call"));
		tempArrList.add(setMenuItems("Exit", "exit"));

		menuAdapter = new CustomMenuListAdapter(EmployeeEditActivity.this,
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
					Intent intent = new Intent(EmployeeEditActivity.this,
							EmployeeAddActivity.class);
					EmployeeEditActivity.this.startActivity(intent);
				} else if (keyword.equals("Send Web Message")) {

				} else if (keyword.equals("Send SMS")) {

				} else if (keyword.equals("Phone Call")) {

				}
				/** When "Exit" menu item is pressed **/
				else if (keyword.equals("Exit")) {
					pdialog.show();
					EmployeeEditActivity.this.finish();
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
