package com.ith.project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.ith.project.R;
import com.ith.project.EntityClasses.Employee;
import com.ith.project.EntityClasses.Leave;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.LeaveSQLite;

public class LeaveFormActivity extends Activity implements OnClickListener,
		TextWatcher {

	private final static String CreateForm = "SendLeaveRequest";

	private EditText leaveTo, searchBox, leaveRemark;
	private TextView leaveDate;
	private Button leaveSubmit;
	private ImageButton menuButton, empSearch, homeButton, buttonDate;
	private String leaveRemarkStr, leaveDateTimeStr, day, month, year;
	private Dialog dialog, empSelectDialog;
	private HttpConnection conn;
	private static int employeeCount;
	private JSONObject createLeave;
	private Spinner leaveTypeSpinner;
	private ArrayList<String> spinnerItems;
	private EmployeeSQLite employeeSQLite;
	private LeaveSQLite leaveSQLite;
	private static ArrayList<Employee> itemDetails;
	private static EmployeeListItemArrayAdapter listItemArrAdapter;
	private ListView empListView;
	private int applicantId, approvalId, leaveSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.leave_form);
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

	private void init() {
		leaveTypeSpinner = (Spinner) findViewById(R.id.spinnerLeaveType);
		leaveTo = (EditText) findViewById(R.id.editTextLeaveTo);
		empSearch = (ImageButton) findViewById(R.id.imageButtonLeaveSearch);
		leaveDate = (TextView) findViewById(R.id.textViewStartLeaveDateString);
		buttonDate = (ImageButton) findViewById(R.id.imageViewLeaveStartDate);
		leaveSubmit = (Button) findViewById(R.id.sendLeaveApplication);

		day = "";
		month = "";
		year = "";

		/** To set the Names of persons in the "leaveTo" field **/
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.linearLayoutLeaveFormFill);

		layoutInflater.inflate(R.layout.leave_form, linLayout, false);

		/** To set the default Names **/
		leaveTo.setText("");
		leaveTo.setFocusable(false);
		leaveTo.setClickable(true);
		leaveTo.setFocusableInTouchMode(true);

		empSearch.setOnClickListener(this);

		/** Spinner for selecting leaveType **/
		spinnerItems = new ArrayList<String>();
		spinnerItems.add("Sick");
		spinnerItems.add("Personal");
		spinnerItems.add("Family Function");
		spinnerItems.add("Others");
		leaveSpinner = 3;
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, spinnerItems);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		leaveTypeSpinner.setAdapter(dataAdapter);
		leaveTypeSpinner.setSelection(leaveSpinner);

		/** Check for spinner item change Listener **/
		leaveTypeSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						Log.e("Item at Leave Type Spinner", ""
								+ parent.getItemAtPosition(pos).toString());
						leaveSpinner = pos;

					}

					public void onNothingSelected(AdapterView<?> arg0) {
						leaveSpinner = 3;

					}
				});

		leaveRemark = (EditText) findViewById(R.id.editTextLeaveRemarks);
		leaveRemark.setFocusable(false);
		leaveRemark.setFocusableInTouchMode(true);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(LeaveFormActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setVisibility(View.GONE);
		homeButton.setOnClickListener(LeaveFormActivity.this);

		buttonDate.setOnClickListener(this);
		leaveSubmit.setOnClickListener(this);

		EmployeeListActivity.clearEmployeeChecked();

		/** To open up the Database and query for the EmployeeList **/
		employeeSQLite = new EmployeeSQLite(LeaveFormActivity.this);
		if (!employeeSQLite.isOpen())
			employeeSQLite.openDB();
		itemDetails = employeeSQLite.getEmpListFromDB();

		/**
		 * Here returnItemDetails is required to store the searched list. By
		 * default it is always equals to itemDetails
		 **/
		employeeSQLite.closeDB();

	}

	public void onClick(View v) {

		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(homeButton)) {

		} else if (v.equals(empSearch)) {

			callEmployeeSearch();

		}

		/** Launch DateSet Dialog **/
		else if (v.equals(buttonDate)) {
			String saf = null;

			if (year == "" && month == "" && day == "") {
				Calendar currDate = Calendar.getInstance();
				SimpleDateFormat dtFormat = new SimpleDateFormat(
						"yyyyMMddHHmmss", Locale.US);
				saf = dtFormat.format(currDate.getTime());

				year = saf.subSequence(0, 4).toString();
				month = saf.subSequence(4, 6).toString();
				day = saf.subSequence(6, 8).toString();

			}

			Log.e("Date are: ", "" + year + "-" + month + "-" + day);
			new DatePickerDialog(this, dateSet, Integer.parseInt(year),
					Integer.parseInt(month), Integer.parseInt(day)).show();
		}
		/**
		 * This is called for sending the leave Form to the web service and also
		 * store in sqlite if sending failed with Flag = 'leavePending'
		 **/
		else if (v.equals(leaveSubmit)) {

			applicantId = LoginAuthentication.EmployeeId;
			leaveDateTimeStr = new StringBuilder().append(year).append(month)
					.append(day).append("00").append("00").append("00")
					.toString();
			leaveRemarkStr = leaveRemark.getText().toString();

			/** Make an inquiry json object **/
			convertNamesToId();
			createLeave = Leave.makeNewLeaveJSON(applicantId, approvalId,
					(leaveSpinner + 1), leaveDateTimeStr, leaveRemarkStr);

			Log.v("createLeave status", "" + createLeave.toString());

			/** send the JSONObject to update the Leaves in the database **/
			new Thread(new Runnable() {

				public void run() {

					conn = HttpConnection.getSingletonConn();

					Log.e("insertStringJson", "" + createLeave.toString());
					String insertStatusStr = conn.getJSONFromUrl(createLeave,
							CreateForm);
					Log.e("Here comes createLeaveStr", "" + insertStatusStr);
					leaveSQLite = new LeaveSQLite(LeaveFormActivity.this);
					leaveSQLite.openDB();
					boolean insertStatus = false;
					try {

						JSONObject insertStatusJson = new JSONObject(
								insertStatusStr);

						insertStatus = (Boolean) insertStatusJson
								.get("SendLeaveRequestResult");

						if (insertStatus) {

							Log.e("Leave has been sent",
									"LEAVE sent successfully !!!");
						} else {

							leaveSQLite.saveLeaveDraft(applicantId, approvalId,
									(leaveSpinner + 1), leaveDateTimeStr,
									leaveRemarkStr, "leavePending");

							Log.e("Problem Sending Leave ",
									"Leave saved as draft: " + insertStatus);
						}
					} catch (JSONException e) {
						/**
						 * Save the typed leave application as draft in sqlite
						 * if connection return is *JPT*
						 **/
						leaveSQLite.saveLeaveDraft(applicantId, approvalId,
								(leaveSpinner + 1), leaveDateTimeStr,
								leaveRemarkStr, "leavePending");

						Log.e("JSONException while sending leave",
								"" + e.getMessage());
						Log.e("leaves Saved as Draft",
								"Saved leaves will be sent next time");
						e.printStackTrace();
					}
					leaveSQLite.closeDB();
					runOnUiThread(new Runnable() {

						public void run() {
							Intent intent = new Intent(LeaveFormActivity.this,
									LeaveListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					});
				}

			}).start();

		}

	}

	/**
	 * To read the Approval Names in the "TO" Field and convert into
	 * corresponding Id
	 **/
	private void convertNamesToId() {
		String applicantName = leaveTo.getText().toString();

		for (int i = 0; i < employeeCount; i++) {
			if (applicantName.equals(itemDetails.get(i).getEmployeeName())) {
				approvalId = itemDetails.get(i).getEmployeeId();
				break;
			}
		}

	}

	private void callEmployeeSearch() {

		LayoutInflater menuInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout linLayoutEmpListItem = null;

		/** To bring front the Dialog box **/
		empSelectDialog = new Dialog(this);
		empSelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		empSelectDialog.setCanceledOnTouchOutside(true);

		menuInflater.inflate(R.layout.employee_list_view, linLayoutEmpListItem,
				false);

		/** To set the alignment of the Dialog box in the screen **/
		WindowManager.LayoutParams WMLP = empSelectDialog.getWindow()
				.getAttributes();

		WMLP.gravity = Gravity.TOP;
		WMLP.verticalMargin = 0.08f; // To put it below header
		empSelectDialog.getWindow().setAttributes(WMLP);

		/** To set the dialog box with the List layout in the android xml **/
		empSelectDialog.setContentView(R.layout.employee_list_view);

		/** To make the dialog width small **/
		ViewGroup parent = (ViewGroup) empSelectDialog
				.findViewById(R.id.linearLayoutEmployeeListView);
		LayoutInflater hawaInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		parent.addView(hawaInflater.inflate(R.layout.employee_list_view, null),
				500, 2);

		/** for initializing EmpSelectBut and searchBox **/
		searchBox = (EditText) empSelectDialog
				.findViewById(R.id.editTextSearch);
		searchBox.setFocusable(true);
		searchBox.setFocusableInTouchMode(true);
		searchBox.addTextChangedListener(this);
		empListView = (ListView) empSelectDialog.findViewById(R.id.listView1);

		/** Add the listener to the list of Employees items **/
		listItemArrAdapter = new EmployeeListItemArrayAdapter(this,
				R.id.linearLayoutEmployeeListItems, itemDetails);
		empListView.setAdapter(listItemArrAdapter);
		employeeCount = listItemArrAdapter.getCount();

		leaveTo.setText("");
		empSelectDialog.show();

	}

	/*****************************************************************************************************
	 * For Date Picker Dialog
	 * **************************************************************************************************/
	DatePickerDialog.OnDateSetListener dateSet = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int Year, int monthOfYear,
				int dayOfMonth) {
			/** perform your required operation after date has been set **/

			if (dayOfMonth < 10) {
				day = new StringBuilder().append("0").append(dayOfMonth)
						.toString();
			} else
				day = new StringBuilder().append(dayOfMonth).toString();

			if (monthOfYear < 10) {
				month = new StringBuilder().append("0").append(monthOfYear)
						.toString();
			} else
				month = new StringBuilder().append(monthOfYear).toString();

			String combinedDate = (new StringBuilder()).append(Year)
					.append("-").append(month).append("-").append(day)
					.toString(); // combinedDate == yyyymmdd

			year = new StringBuilder().append(Year).toString();
			leaveDate.setText(combinedDate);
		}
	};

	/******************************************************************************************
	 * A new ArrayAdapter to handle the List View of Employees
	 * *****************************************************************************************/
	private class EmployeeListItemArrayAdapter extends ArrayAdapter<Employee>
			implements Filterable {

		private Context cntxt;
		private ArrayList<Employee> itemDets;

		public EmployeeListItemArrayAdapter(Context context,
				int textViewResourceId, ArrayList<Employee> itemDetails) {
			super(context, textViewResourceId);
			this.cntxt = context;
			this.itemDets = itemDetails;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) cntxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view;

			view = new View(this.cntxt);

			view = inflater
					.inflate(R.layout.employee_list_items, parent, false);

			parent.setBackgroundColor(Color.rgb(221, 221, 221));

			TextView textView = (TextView) view
					.findViewById(R.id.textViewEmployeeName);
			textView.setText(this.itemDets.get(position).getEmployeeName());
			textView.setFocusable(false);

			TextView location = (TextView) view
					.findViewById(R.id.textViewEmployeeLocation);
			location.setText(this.itemDets.get(position).getAddress());
			location.setFocusable(false);

			CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox1);
			view.setTag(checkBox);
			checkBox.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					CheckBox chkBox = (CheckBox) v;
					Employee emp = (Employee) chkBox.getTag();
					Log.e("CheckBox check@", "" + chkBox.isChecked());
					emp.setChecked(chkBox.isChecked());
					setApprovalName();
				}

			});

			inflater = null;

			Employee employee = itemDets.get(position);
			checkBox.setChecked(false);
			employee.setChecked(false);
			checkBox.setTag(employee);

			return view;
		}

		/** To set one ApprovalName in the leaveTo field **/
		private void setApprovalName() {

			empSelectDialog.dismiss();
			ArrayList<Employee> selectedItemDetail = new ArrayList<Employee>();
			for (int i = 0; i < employeeCount; i++) {
				if (itemDetails.get(i).getChecked()) {
					selectedItemDetail.add(itemDetails.get(i));
					Log.e("Checked Items", ""
							+ itemDetails.get(i).getEmployeeName());
				}
			}
			String numbers = selectedItemDetail.get(0).getEmployeeName();
			if (numbers == null)
				numbers = "";
			String numberFromMsgTo = leaveTo.getText().toString();
			if (!numberFromMsgTo.isEmpty()) {
				numbers = numberFromMsgTo;
				Log.e("Numbers from leaveTo", "" + numbers);
			}
			leaveTo.setText(numbers);

		}

		public int getCount() {

			return itemDets.size();
		}

		public Employee getItem(int arg0) {

			return itemDets.get(arg0);
		}

		public long getItemId(int position) {

			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public Filter getFilter() {

			Filter filter = new Filter() {

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {

					FilterResults results = new FilterResults();
					ArrayList<Employee> FilteredItemDetails = new ArrayList<Employee>();

					/** If search string (constraint) is null, display all **/
					if (constraint == null || constraint.length() == 0) {

						results.values = itemDetails;
						results.count = itemDetails.size();
					}
					/** If constraint is not null */
					else {

						constraint = constraint.toString().toLowerCase();
						for (int i = 0; i < itemDetails.size(); i++) {
							Employee dataNames = itemDetails.get(i);
							if (dataNames.getEmployeeName().toLowerCase()
									.contains(constraint.toString())) {
								FilteredItemDetails.add(dataNames);
							}
						}

						results.count = FilteredItemDetails.size();
						results.values = FilteredItemDetails;
					}
					return results;
				}

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {

					itemDets = (ArrayList<Employee>) results.values;
					notifyDataSetChanged();
				}
			};
			return filter;
		}
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		listItemArrAdapter.getFilter().filter(s.toString().toLowerCase());
		listItemArrAdapter.notifyDataSetChanged();

	}

	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub

	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

}
