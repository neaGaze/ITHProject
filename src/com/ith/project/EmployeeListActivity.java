package com.ith.project;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.R;
import com.ith.project.EntityClasses.Employee;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.menu.CustomMenuListAdapter;
import com.ith.project.sqlite.EntryLogSQLite;
import com.ith.project.sqlite.EmployeeSQLite;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class EmployeeListActivity extends Activity implements OnClickListener,
		OnItemClickListener, TextWatcher {

	private final String url = "GetEmployeeList";
	private final String delUrl = "DeleteEmployee";

	private static ArrayList<Employee> itemDetails;
	private static ArrayList<Employee> returnItemDetails;
	private static ArrayList<Employee> selectedItemDetails;
	private ListView listView;
	private ImageButton menuButton;
	private ImageButton EmployeeButton;
	private ImageButton homeButton;
	private EditText searchBox;
	private ImageButton searchButton;
	private HttpConnection conn;
	private EmployeeSQLite employeeSQLite;
	private EntryLogSQLite entryLogSQLite;
	private static EmployeeListItemArrayAdapter listItemArrAdapter;
	private static int employeeCount;
	static CustomMenuListAdapter menuAdapter;
	private Dialog dialog;
	private static EmployeeListActivity context;
	private HashMap<String, String> menuItems;
	private Dialog exitDialog;
	private Button exitCancel;
	private Button exitConfirm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.employee_list_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		context = this;
		init();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (employeeSQLite != null)
			employeeSQLite.closeDB();
		if (entryLogSQLite != null)
			entryLogSQLite.closeDB();
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

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setClickable(false);

		searchButton = (ImageButton) findViewById(R.id.buttonSearch);
		searchButton.setClickable(false);

		homeButton.setClickable(true);
		homeButton.setOnClickListener(EmployeeListActivity.this);

		new Thread(new Runnable() {

			public void run() {

				selectedItemDetails = null;
				JSONObject inputJson;
				conn = HttpConnection.getSingletonConn();

				employeeSQLite = new EmployeeSQLite(EmployeeListActivity.this);
				entryLogSQLite = new EntryLogSQLite(EmployeeListActivity.this);
				if (!employeeSQLite.isOpen())
					employeeSQLite.openDB();
				if (!entryLogSQLite.isOpen())
					entryLogSQLite.openDB();

				inputJson = Employee.getEmploueeInquiry(
						LoginAuthentication.UserloginId,
						entryLogSQLite.getLatestDateModified());

				Log.v("getemployee inquiry", "" + inputJson.toString());

				/** To establish connection to the web service **/
				String employeesFromWS = conn.getJSONFromUrl(inputJson, url);

				Log.v("Employees:", "here: " + employeesFromWS);
				if (employeesFromWS.startsWith("{")) {

					/** Update the local file according to the web service **/
					employeeSQLite.updateDBEmployees(employeesFromWS,
							entryLogSQLite);
				}

				/** Now read from the local DB always **/
				itemDetails = employeeSQLite.getEmpListFromDB();
				returnItemDetails = itemDetails;

				/**
				 * To run the main thread after completion of the connection
				 * thread
				 **/

				runOnUiThread(new Runnable() {

					public void run() {

						if (itemDetails != null) {

							searchBox = (EditText) findViewById(R.id.editTextSearch);
							searchBox.setFocusable(true);
							searchBox.setFocusableInTouchMode(true);
							searchBox
									.addTextChangedListener(EmployeeListActivity.this);

							searchButton.setClickable(true);
							searchButton
									.setOnClickListener(EmployeeListActivity.this);

							menuButton = (ImageButton) findViewById(R.id.menu);
							menuButton
									.setOnClickListener(EmployeeListActivity.this);

							listView = (ListView) findViewById(R.id.listView1);

							Log.d("***Welcome to Employee ListView***",
									".......");

							if (itemDetails != null) {
								listItemArrAdapter = new EmployeeListItemArrayAdapter(
										EmployeeListActivity.this,
										R.layout.employee_list_items,
										itemDetails);
								listView.setAdapter(listItemArrAdapter);
								listView.setOnItemClickListener(EmployeeListActivity.this);
								employeeCount = itemDetails.size();
								menuItems = new HashMap<String, String>();
							} else {
								Log.e("Row returned null",
										"SO itemdetails is null");
							}
						}
					}
				});

			}

		}).start();

	}

	/********************************************************************************
	 * Fill Employee class and itemDetails ArrayList
	 * *********************************************************************************/
	public ArrayList<Employee> setEmployee(JSONArray tempJsonArr) {

		ArrayList<Employee> tempArrList = new ArrayList<Employee>();
		int jsonArrLength = tempJsonArr.length();
		for (int i = 0; i < jsonArrLength; i++) {
			try {
				Employee tempEmployee = new Employee();
				JSONObject tempJson = tempJsonArr.getJSONObject(i);
				tempEmployee.setValues(tempJson);
				tempArrList.add(tempEmployee);
				Log.v("json object(Employees):", "" + tempJson.toString());

			} catch (JSONException e) {
				Log.e("jSONException", "" + e.getMessage());
				e.printStackTrace();
			}
		}
		return tempArrList;
	}

	/****************************************************************************
	 * Get the correct employee ID
	 *************************************************************************/
	public static ArrayList<Employee> getEmployeeArrayList() {
		return returnItemDetails;
	}

	/****************************************************************************
	 * View Employee
	 *************************************************************************/
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		Log.v("Employee LIst Item clicked", "@ " + position);
		Intent intent = new Intent(EmployeeListActivity.this,
				EmployeeViewActivity.class);
		intent.putExtra("EmployeeId",
				(returnItemDetails.get(position).getEmployeeId()));
		EmployeeListActivity.this.startActivity(intent);

	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {

			Intent intent = new Intent(EmployeeListActivity.this,
					GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);

		} else if (v.equals(EmployeeButton)) {

			Intent intent = new Intent(EmployeeListActivity.this,
					EmployeeAddActivity.class);
			this.startActivity(intent);
			this.finish();

		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			menuItems.put("Add Employee", "add_employee");
			menuItems.put("Send Web Message", "mail_web");
			menuItems.put("Delete Employee", "delete_user");

			new CallMenuDialog(this, dialog, menuItems);

		} else if (v.equals(exitConfirm)) {
			deleteEmployee();
		} else if (v.equals(exitCancel)) {

			exitDialog.dismiss();
		}

	}

	/****************************************************************************
	 * To bring the Dialog for deleting Employees
	 *************************************************************************/
	public void deleteDialog() {
		LayoutInflater prefInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout linLayoutExit = (LinearLayout) findViewById(R.id.linearLayoutExitDialog);
		prefInflater.inflate(R.layout.preferences_screen, linLayoutExit, false);

		/** To bring front the Dialog box **/
		exitDialog = new Dialog(context);
		exitDialog.setTitle("Confirm Delete");
		exitDialog.setCanceledOnTouchOutside(false);

		/** To set the dialog box with the List layout in the android xml **/
		exitDialog.setContentView(R.layout.exit_dialog);

		TextView dialogQuest = (TextView) exitDialog
				.findViewById(R.id.textViewExitConfirm);
		dialogQuest.setText("Do you Really Want to Delete?");
		exitConfirm = (Button) exitDialog.findViewById(R.id.buttonExitConfirm);
		exitConfirm.setText("DELETE");

		exitDialog.show();

		exitCancel = (Button) exitDialog.findViewById(R.id.buttonExitCancel);
		exitCancel.setOnClickListener(context);

		exitConfirm.setOnClickListener(context);
	}

	/****************************************************************************
	 * To list the selected employees through CheckBoxes
	 *************************************************************************/
	public static ArrayList<Employee> getSelected() {
		ArrayList<Employee> selectedItemDetail = new ArrayList<Employee>();
		for (int i = 0; i < employeeCount; i++) {
			if (itemDetails.get(i).getChecked()) {
				selectedItemDetail.add(itemDetails.get(i));
				Log.e("Checked Items", ""
						+ itemDetails.get(i).getEmployeeName());
			}
		}
		return selectedItemDetail;
	}

	/** To dismiss the Checked value to false in all employees **/
	public static void clearEmployeeChecked() {

		for (int i = 0; i < employeeCount; i++) {
			if (itemDetails.get(i).getChecked()) {
				itemDetails.get(i).setChecked(false);
			}
		}
	}

	/****************************************************************************
	 * To delete the selected employees
	 *************************************************************************/
	public void deleteEmployee() {

		selectedItemDetails = getSelected();

		Thread delThread = new Thread(new Runnable() {

			public void run() {
				JSONObject inputDelJson;

				inputDelJson = Employee
						.getDelJsonQueryObject(selectedItemDetails);

				Log.e("Delete Emps ", "" + inputDelJson.toString());

				/** To establish connection to the web service **/
				String delEmployeesFromWS = conn.getJSONFromUrl(inputDelJson,
						delUrl);

				Log.e("Employees Delete Status:", "" + delEmployeesFromWS);

				try {
					JSONObject delReplyJson = new JSONObject(delEmployeesFromWS);
					if (delReplyJson.getBoolean("DeleteEmployeeResult")) {
						employeeSQLite.deleteEmployees(selectedItemDetails);
					}
				} catch (JSONException e) {
					Log.e("JSON Parse Error @ EmplListAct", "" + e.getMessage());
					e.printStackTrace();
				}

				runOnUiThread(new Runnable() {

					public void run() {

						exitDialog.dismiss();
						EmployeeListActivity.this.finish();
						Intent intent = new Intent(EmployeeListActivity.this,
								EmployeeListActivity.class);
						EmployeeListActivity.this.startActivity(intent);

					}

				});
			}

		});
		delThread.start();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/******************************************************************************************
	 * A new ArrayAdapter to handle the List View
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

			ImageView imageView = (ImageView) view
					.findViewById(R.id.imageViewEmployee);
			if (this.itemDets.get(position).getGender().equals("Female"))
				imageView.setBackgroundResource(R.drawable.female_employee);
			else
				imageView.setBackgroundResource(R.drawable.male_employee);
			imageView.setFocusable(false);

			CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox1);

			view.setTag(checkBox);

			checkBox.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					CheckBox chkBox = (CheckBox) v;
					Employee emp = (Employee) chkBox.getTag();
					Log.e("CheckBox check@", "" + chkBox.isChecked());
					emp.setChecked(chkBox.isChecked());
				}

			});
			inflater = null;

			Employee employee = itemDets.get(position);
			checkBox.setChecked(employee.getChecked());
			checkBox.setTag(employee);

			return view;
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

				@SuppressLint("DefaultLocale")
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {

					FilterResults results = new FilterResults();
					ArrayList<Employee> FilteredItemDetails = new ArrayList<Employee>();

					/** If search string (constraint) is null, display all **/
					if (constraint == null || constraint.length() == 0) {

						results.values = itemDetails;
						results.count = itemDetails.size();
						returnItemDetails = itemDetails;
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
						returnItemDetails = FilteredItemDetails;
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

	/****************************************************************************************
	 * For text change detector
	 * **************************************************************************************/
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub

	}

	public void beforeTextChanged(CharSequence s, int start, int before,
			int count) {
		// TODO Auto-generated method stub

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {

		listItemArrAdapter.getFilter().filter(s.toString().toLowerCase());
		listItemArrAdapter.notifyDataSetChanged();
	}

	/****************************************************************************
	 * Get the Activity instance of GridItemActivity
	 *************************************************************************/
	public static EmployeeListActivity getEmployeeListActivityInstance() {
		return context;
	}

}
