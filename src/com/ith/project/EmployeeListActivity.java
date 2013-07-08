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
import com.ith.project.sdcard.EmployeeLocal;
import com.ith.project.sqlite.DateLogSQLite;
import com.ith.project.sqlite.EmployeeSQLite;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EmployeeListActivity extends Activity implements OnClickListener,
		OnItemClickListener, TextWatcher {

	private static final int MENU_EXIT = 0;
	private final String url = "http://192.168.100.2/EMSWebService/Service1.svc/json/GetEmployeeList";
	private final String delUrl = "http://192.168.100.2/EMSWebService/Service1.svc/json/DeleteEmployee";

	private static ArrayList<Employee> itemDetails;
	private static ArrayList<Employee> returnItemDetails;
	private static ArrayList<Employee> selectedItemDetails;
	private ListView listView;
	private ImageButton menuButton;
	private ImageButton EmployeeButton;
	private ImageButton homeButton;
	private EditText searchBox;
	private ImageButton searchButton;
	private CheckBox checkBox;
	private int currPos;
	private HttpConnection conn;
	private EmployeeLocal employeeLocal;
	private EmployeeSQLite employeeSQLite;
	private DateLogSQLite dateLogSQLite;
	private Employee employee;
	private static EmployeeListItemArrayAdapter listItemArrAdapter;
	private static int employeeCount;
	private ProgressDialog pdialog;
	static CustomMenuListAdapter menuAdapter;
	private Dialog dialog;
	private static EmployeeListActivity context;
	private static boolean connFlag;
	private CallMenuDialog callDiag;
	private HashMap<String, String> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();
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
		employeeSQLite.closeDB();
		dateLogSQLite.closeDB();
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
		new Thread(new Runnable() {

			public void run() {

				JSONObject inputJson;
				conn = HttpConnection.getSingletonConn();
				// conn = new HttpConnection(url);
				// employeeLocal = new EmployeeLocal();

				employeeSQLite = new EmployeeSQLite(EmployeeListActivity.this);
				dateLogSQLite = new DateLogSQLite(EmployeeListActivity.this);
				employeeSQLite.openDB();
				dateLogSQLite.openDB();

				// employee = new Employee();

				inputJson = Employee.getJsonUserLoginIdEmployee(
						LoginAuthentication.getUserLoginId(),
						dateLogSQLite.getLatestDateModified());

				Log.v("getemployee inquiry", "" + inputJson.toString());

				/**
				 * To remove add Bulletin for normal users and change the logo
				 * as well
				 **/
				modifyEmployeeAdd4Admin(LoginAuthentication.getUserRoleId());

				/** To establish connection to the web service **/
				String employeesFromWS = conn.getJSONFromUrl(inputJson, url);
				connFlag = true;

				Log.v("Employees:", "here: " + employeesFromWS);
				if (!employeesFromWS.equals("")) {
					connFlag = false;

					/** Update the local file according to the web service **/
					// employeeLocal.updateLocalFiles(inputJson,
					// employeesFromWS);
					employeeSQLite.updateDBUsersTableJson(employeesFromWS,
							dateLogSQLite);
					// employeeLocal.updateLocalFiles();
				}
				/** Now read from the local DB always **/
				// JSONArray outputJson = employeeLocal
				// .getJSONFromLocal(inputJson);

				// Log.v("JsonArray:", "" + outputJson.toString());

				// itemDetails = setEmployee(outputJson);
				itemDetails = employeeSQLite.getJSONFromDB();
				returnItemDetails = itemDetails;

				// Log.v("Employees:",""+
				// employeeLocal.getJSONFromLocal(inputJson).toString());

				/**
				 * To run the main thread after completion of the connection
				 * thread
				 **/
				runOnUiThread(new Runnable() {

					public void run() {

						searchBox = (EditText) findViewById(R.id.editTextSearch);
						searchBox.setFocusable(true);
						searchBox.setFocusableInTouchMode(true);
						searchBox
								.addTextChangedListener(EmployeeListActivity.this);

						searchButton = (ImageButton) findViewById(R.id.buttonSearch);
						searchButton
								.setOnClickListener(EmployeeListActivity.this);

						menuButton = (ImageButton) findViewById(R.id.menu);
						menuButton
								.setOnClickListener(EmployeeListActivity.this);

						// EmployeeButton = (ImageButton)
						// findViewById(R.id.bulletin_add_icon);
						// EmployeeButton.setOnClickListener(EmployeeListActivity.this);

						homeButton = (ImageButton) findViewById(R.id.home);
						homeButton
								.setOnClickListener(EmployeeListActivity.this);

						registerForContextMenu(homeButton);

						listView = (ListView) findViewById(R.id.listView1);

						Log.d("***Welcome to ListView***", ".......");
						listItemArrAdapter = new EmployeeListItemArrayAdapter(
								EmployeeListActivity.this,
								R.layout.employee_list_items, itemDetails);
						listView.setAdapter(listItemArrAdapter);
						employeeCount = listItemArrAdapter.getCount();
						listView.setOnItemClickListener(EmployeeListActivity.this);

						menuItems = new HashMap<String, String>();

						pdialog.dismiss();
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

	/*********************************************************************************
	 * Called when the LExit Button is Clicked [DEPRECIATED]
	 * ******************************************************************************/
	public void modifyEmployeeAdd4Admin(int userRolesId) {

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutCustomTitle);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.custom_title, lin, false);
		// ImageButton addEmployeeBut = (ImageButton)
		// findViewById(R.id.bulletin_add_icon);
		int id = getResources().getIdentifier("add_employee", "drawable",
				getApplicationContext().getPackageName());
		// addEmployeeBut.setImageResource(id);
		// if (userRolesId == 2)
		// findViewById(R.id.bulletin_add_icon).setVisibility(View.INVISIBLE);
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

		pdialog.show();
		Log.v("Employee LIst Item clicked", "@ " + position);
		Intent intent = new Intent(EmployeeListActivity.this,
				EmployeeViewActivity.class);
		intent.putExtra("PositionOfEmployee", (position));
		EmployeeListActivity.this.startActivity(intent);

	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {
			pdialog.show();
			Intent intent = new Intent(EmployeeListActivity.this,
					GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			// this.finish();
		} else if (v.equals(EmployeeButton)) {
			pdialog.show();
			// Toast.makeText(this, "Add Bulletin", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(EmployeeListActivity.this,
					EmployeeAddActivity.class);
			this.startActivity(intent);
			this.finish();

		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			menuItems.put("Send SMS", "mail_sms");
			menuItems.put("Exit", "exit");
			menuItems.put("Add Employee", "add_employee");
			menuItems.put("Phone Call", "call");
			menuItems.put("Send Web Message", "mail_web");
			menuItems.put("Delete Employee", "delete_user");

			callDiag = new CallMenuDialog(this, pdialog, dialog, menuItems);
			// callMenuDialog();
		}

	}


	/****************************************************************************
	 * To delete the selected employees
	 *************************************************************************/
	public void deleteEmployee() {
		selectedItemDetails = new ArrayList<Employee>();
		for (int i = 0; i < employeeCount; i++) {
			if (itemDetails.get(i).getChecked()) {
				selectedItemDetails.add(itemDetails.get(i));
				Log.e("Deleted Items", ""
						+ itemDetails.get(i).getEmployeeName());

			}
		}

		Thread delThread = new Thread(new Runnable() {

			public void run() {
				JSONObject inputDelJson;

				inputDelJson = Employee
						.getDelJsonQueryObject(selectedItemDetails);

				Log.e("Delete Emps ", "" + inputDelJson.toString());

				/** To establish connection to the web service **/
				String delEmployeesFromWS = conn.getJSONFromUrl(
						inputDelJson, delUrl);

				Log.e("Employees Delete Status:", ""
						+ delEmployeesFromWS);

				try {
					JSONObject delReplyJson = new JSONObject(
							delEmployeesFromWS);
					if (delReplyJson.getBoolean("DeleteEmployeeResult")) {
						employeeSQLite
								.deleteEmployees(selectedItemDetails);
					}
				} catch (JSONException e) {
					Log.e("JSON Parse Error @ EmplListAct",
							"" + e.getMessage());
					e.printStackTrace();
				}

				runOnUiThread(new Runnable() {

					public void run() {
						pdialog.show();
						EmployeeListActivity.this.finish();
						Intent intent = new Intent(
								EmployeeListActivity.this,
								EmployeeListActivity.class);
						EmployeeListActivity.this.startActivity(intent);

					}

				});
			}

		});
		delThread.start();

	}

	/****************************************************************************
	 * When Home Button is Clicked
	 *************************************************************************/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);

	}

	/****************************************************************************
	 * When the custom menu item is clicked
	 *************************************************************************/
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		// String[] names = getResources().getStringArray(R.array.names);
		switch (item.getItemId()) {
		case R.id.itemAddEmployee:
			Toast.makeText(this, "Add  Employee", Toast.LENGTH_SHORT).show();
			pdialog.show();
			// Toast.makeText(this, "Add Bulletin", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(EmployeeListActivity.this,
					EmployeeAddActivity.class);
			this.startActivity(intent);
			this.finish();
			return true;

		default:
			return super.onContextItemSelected(item);
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

			currPos = position;

			View view;
			// if (convertView == null)
			{
				view = new View(this.cntxt);

				view = inflater.inflate(R.layout.employee_list_items, parent,
						false);

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

				CheckBox checkBox = (CheckBox) view
						.findViewById(R.id.checkBox1);

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
				// holder.name.setChecked(employee.getChecked());
				// holder.name.setTag(employee);
				checkBox.setTag(employee);
			} // else
				// view = (View) convertView;

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

	/****************************************************************************
	 * Get the connFlag
	 *************************************************************************/
	
	public static boolean getConnFlag() {
		return connFlag;
	}
}
