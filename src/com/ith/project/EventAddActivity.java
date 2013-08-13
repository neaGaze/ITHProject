package com.ith.project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Employee;
import com.ith.project.EntityClasses.Event;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.googlemap.GoogleMapActivity;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.EventSQLite;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TimePicker;

public class EventAddActivity extends Activity implements OnClickListener,
		TextWatcher, OnItemClickListener {

	private final static String AddUrl = "CreateEvent";

	private EditText eventTo, searchBox, eventTitle, eventDesc, eventVenue;
	private TextView eventDate, eventTime;
	private Button eventSubmit, empSelectBut, googleMapButton;
	private ImageButton menuButton, empSearch, homeButton, buttonDate,
			buttonTime;
	String eventIdStr, eventTitleStr, eventDescStr, eventDateStr, eventTimeStr,
			eventVenueStr, eventDateTimeStr;
	public static String longitude, latitude;
	private Dialog dialog, empSelectDialog;
	private HashMap<String, String> menuItems;
	private HttpConnection conn;
	private static int employeeCount;
	private JSONObject insertEvent;
	// private static EmployeeListItemArrayAdapter listItemArrAdapter;
	private EmployeeSQLite employeeSQLite;
	private EventSQLite eventSQLite;
	private static ArrayList<Employee> itemDetails, returnItemDetails;
	private static EmployeeListItemArrayAdapter listItemArrAdapter;
	private Event eventAdd;
	private Integer[] receiversId;
	private ListView empListView;
	private int currPos, msgFrom, msgTo;
	private String day, month, year, hour, minute;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.event_add);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();
	}

	private void init() {

		eventAdd = new Event();

		eventTitle = (EditText) findViewById(R.id.editTextEventAddTitle);
		eventTo = (EditText) findViewById(R.id.editTextEventAddReceivers);
		empSearch = (ImageButton) findViewById(R.id.imageButtonEventAddSearch);
		eventDesc = (EditText) findViewById(R.id.editTextEventAddDesc);
		eventDate = (TextView) findViewById(R.id.textViewEventAddDateString);
		eventTime = (TextView) findViewById(R.id.textViewEventAddTimeString);
		buttonDate = (ImageButton) findViewById(R.id.imageViewEventHappeningDate);
		buttonTime = (ImageButton) findViewById(R.id.imageViewEventHappeningTime);
		eventVenue = (EditText) findViewById(R.id.editTextEventAddVenue);
		eventSubmit = (Button) findViewById(R.id.buttonAddEvent);
		googleMapButton = (Button) findViewById(R.id.buttonPinGoogleMap);

		day = "";
		month = "";
		year = "";
		hour = "";
		minute = "";
		/** To set the Email of persons in the "eventTo" field **/
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.linearLayoutAddEvent);
		// LinearLayout linLayoutMenu = new LinearLayout(this);
		layoutInflater.inflate(R.layout.event_add, linLayout, false);

		ArrayList<Employee> selectedEmployees = EmployeeListActivity
				.getSelected();
		String numbers = getEmployeeName(selectedEmployees);

		/** To set the default Names and make it UnEditable **/
		eventTo.setText(numbers);
		eventTo.setFocusable(false);
		eventTo.setClickable(true);
		eventTo.setFocusableInTouchMode(true);
		// eventTo.setOnClickListener(this);

		empSearch.setOnClickListener(this);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(EventAddActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setVisibility(View.GONE);
		homeButton.setOnClickListener(EventAddActivity.this);

		buttonDate.setOnClickListener(this);
		buttonTime.setOnClickListener(this);
		googleMapButton.setOnClickListener(this);
		eventSubmit.setOnClickListener(this);

		menuItems = new HashMap<String, String>();
		/* pdialog.dismiss(); */

		EmployeeListActivity.clearEmployeeChecked();

		/** To open up the Database and query for the EmployeeList **/
		employeeSQLite = new EmployeeSQLite(EventAddActivity.this);
		if (!employeeSQLite.isOpen())
			employeeSQLite.openDB();
		itemDetails = employeeSQLite.getEmpListFromDB();

		/**
		 * Here returnItemDetails is required to store the searched list. By
		 * default it is always equals to itemDetails
		 **/
		returnItemDetails = itemDetails;
		employeeSQLite.closeDB();

	}

	@Override
	public void onPause() {
		super.onPause();
		/* pdialog.dismiss(); */
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();
		/* pdialog.dismiss(); */
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	/** TO set the selectedEmployee list which are checked **/
	public String getEmployeeName(ArrayList<Employee> selectedEmployees) {

		String[] receiversName = new String[selectedEmployees.size()];
		receiversId = new Integer[selectedEmployees.size()];
		StringBuilder str = new StringBuilder();
		String numbers = null;

		for (int i = 0; i < selectedEmployees.size(); i++) {
			receiversName[i] = selectedEmployees.get(i).getEmployeeName();
			receiversId[i] = selectedEmployees.get(i).getEmployeeId();
			if (i == (selectedEmployees.size() - 1))
				numbers = str.append(receiversName[i]).toString();
			else
				numbers = str.append(receiversName[i]).append("; ").toString();
		}
		return numbers;
	}

	/*********************************************************************
	 * Handle OnClickListeners for Buttons
	 ***************************************************************************/
	public void onClick(View v) {

		if (v.equals(menuButton)) {
			/* pdialog.show(); */
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			// menuItems.put("Exit", "exit");
			// callDiag = new CallMenuDialog(this, pdialog, dialog, menuItems);
			// callMenuDialog();
		} else if (v.equals(empSearch)) {

			callEmployeeSearch();

		}

		/** Launch DateSet Dialog **/
		else if (v.equals(buttonDate)) {
			String saf = null;

			if (year == "" && month == "" && day == "") {
				Calendar currDate = Calendar.getInstance();
				SimpleDateFormat dtFormat = new SimpleDateFormat(
						"yyyyMMddHHmmss");
				saf = dtFormat.format(currDate.getTime());

				year = saf.subSequence(0, 4).toString();
				month = saf.subSequence(4, 6).toString();
				day = saf.subSequence(6, 8).toString();

			}

			Log.e("Date are: ", "" + year + "-" + month + "-" + day);
			new DatePickerDialog(this, dateSet, Integer.parseInt(year),
					Integer.parseInt(month), Integer.parseInt(day)).show();
		}

		/** Launch TimeSet Dialog **/
		else if (v.equals(buttonTime)) {
			if (hour == "" && minute == "") {
				hour = new StringBuilder().append(
						Calendar.getInstance().getTime().getHours()).toString();
				minute = new StringBuilder().append(
						Calendar.getInstance().getTime().getMinutes())
						.toString();
			}
			Log.e("Time Set are: ", "" + hour + ":" + minute);
			new TimePickerDialog(this, timeSet, Integer.parseInt(hour),
					Integer.parseInt(minute), true).show();
		}
		/** Launch googleMap Activity to pin the Google Map Location **/
		else if (v.equals(googleMapButton)) {
			Intent intent = new Intent(getApplicationContext(),
					GoogleMapActivity.class);
			startActivity(intent);
		}

		/**
		 * This is called for sending the event to the web service and also
		 * store in sqlite if sending failed with Flag = 'eventPending'
		 **/
		else if (v.equals(eventSubmit)) {

			// pdialog.show();
			msgFrom = LoginAuthentication.EmployeeId;
			// msgTo = Integer.parseInt(eventTo.getText().toString());
			eventTitleStr = eventTitle.getText().toString();
			eventDescStr = eventDesc.getText().toString();
			eventVenueStr = eventVenue.getText().toString();
			eventDateTimeStr = new StringBuilder().append(year).append(month)
					.append(day).append(hour).append(minute).append("00")
					.toString();
			if (latitude == null || longitude == null || latitude.equals("")
					|| longitude.equals(""))
				latitude = longitude = "";
			else
				Log.e("lat/Long ", "is :" + latitude + " & " + longitude);
			// msgDate = getCurrentDate();
			// Log.d("Event Send Date", "" + msgDate);

			/** Make an inquiry json object **/
			convertNamesToId();
			insertEvent = eventAdd.makeNewEventJSON(msgFrom, receiversId,
					eventTitleStr, eventDescStr, eventVenueStr,
					eventDateTimeStr, longitude, latitude);

			Log.v("insertEvent status", "" + insertEvent.toString());

			/** send the JSONObject to update the Events in the database **/
			new Thread(new Runnable() {

				public void run() {

					conn = HttpConnection.getSingletonConn();

					Log.e("insertStringJson", "" + insertEvent.toString());
					String insertStatusStr = conn.getJSONFromUrl(insertEvent,
							AddUrl);
					Log.e("Here comes insertStatusStr", "" + insertStatusStr);
					eventSQLite = new EventSQLite(EventAddActivity.this);
					eventSQLite.openDB();
					try {

						JSONObject insertStatusJson = new JSONObject(
								insertStatusStr);

						boolean insertStatus = (Boolean) insertStatusJson
								.get("CreateEventResult");

						if (insertStatus) {

							Log.e("Event has been sent",
									"EVENT sent successfully !!!");
						} else {

							eventSQLite.saveEventDraft(msgFrom, receiversId,
									eventTitleStr, eventDescStr,
									eventDateTimeStr, eventVenueStr, longitude,
									latitude, "pending", 0, 0, "eventPending");

							Log.e("Problem Sending Event ",
									"Event saved as draft" + insertStatus);
						}

					} catch (JSONException e) {
						/**
						 * Save the typed msgs as draft in sqlite if connection
						 * return is *JPT*
						 **/
						eventSQLite.saveEventDraft(msgFrom, receiversId,
								eventTitleStr, eventDescStr, eventDateTimeStr,
								eventVenueStr, longitude, latitude, "pending",
								0, 1, "eventPending");

						// Toast.makeText(EventAddActivity.this,"Event Saved as draft",
						// Toast.LENGTH_SHORT)
						// .show();

						Log.e("JSONException while sending event",
								"" + e.getMessage());
						Log.e("Events Saved as Draft",
								"Saved events will be sent next time");
						e.printStackTrace();
					}
					eventSQLite.closeDB();
					runOnUiThread(new Runnable() {

						public void run() {
							Intent intent = new Intent(EventAddActivity.this,
									EventListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);

							// BulletinAddActivity.this.finish();
						}
					});
				}

			}).start();

		}
	}

	/**
	 * To read the names of Employees in the "TO" Field and convert into
	 * corresponding Id and prevent repeat of names
	 **/
	private void convertNamesToId() {
		String namesInMsgTo = eventTo.getText().toString();
		String[] employeeNames = namesInMsgTo.split("; ");
		HashMap<String, Integer> hMap = new HashMap<String, Integer>();
		for (String oneName : employeeNames) {
			for (int i = 0; i < employeeCount; i++) {
				if (oneName.equals(itemDetails.get(i).getEmployeeName())) {
					hMap.put(oneName, itemDetails.get(i).getEmployeeId());
					break;
				}
			}
		}
		int i = 0;
		Log.e("Size of hMap", "Number or Receivers: " + hMap.size());
		receiversId = new Integer[hMap.size()];
		Iterator<Entry<String, Integer>> hMapIterator = hMap.entrySet()
				.iterator();
		while (hMapIterator.hasNext()) {

			Map.Entry<String, Integer> value = (Map.Entry) hMapIterator.next();
			receiversId[i++] = value.getValue();
			Log.e("Msg Send Names",
					"" + value.getKey() + " : " + value.getValue());
		}

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
			eventDate.setText(combinedDate);
		}
	};

	/*****************************************************************************************************
	 * For Time Picker Dialog
	 * **************************************************************************************************/
	TimePickerDialog.OnTimeSetListener timeSet = new TimePickerDialog.OnTimeSetListener() {

		public void onTimeSet(TimePicker view, int Hour, int min) {
			/** perform your required operation after time has been set **/

			if (Hour < 10)
				hour = new StringBuilder().append("0").append(Hour).toString();
			else
				hour = new StringBuilder().append(Hour).toString();

			if (min < 10)
				minute = new StringBuilder().append("0").append(min).toString();
			else {
				minute = new StringBuilder().append(min).toString();
			}
			String combinedTime = (new StringBuilder()).append(hour)
					.append(":").append(minute).toString(); // combinedTime ==
															// HH:mm
			eventTime.setText(combinedTime);

		}
	};

	/******************************************************************************************
	 * To search the required Employees
	 * *****************************************************************************************/
	private void callEmployeeSearch() {
		LayoutInflater menuInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout linLayoutEmpListItem = null;
		// linLayoutEmpListItem = (LinearLayout)
		// findViewById(R.id.linearLayoutEmployeeListItems);
		// linLayoutEmpListItem = (LinearLayout)
		// findViewById(android.R.layout.simple_spinner_dropdown_item);

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

		/**
		 * For intializing the linearLayout for selected Employees \w cross sign
		 **/
		// inflateEmployees("name");

		/** for initializing EmpSelectBut and searchBox **/
		empSelectBut = (Button) empSelectDialog
				.findViewById(R.id.selectedEmployees);
		empSelectBut.setVisibility(View.VISIBLE);
		empSelectBut.setFocusable(false);
		searchBox = (EditText) empSelectDialog
				.findViewById(R.id.editTextSearch);
		searchBox.setFocusable(true);
		searchBox.setFocusableInTouchMode(true);
		searchBox.addTextChangedListener(this);

		empListView = (ListView) empSelectDialog.findViewById(R.id.listView1);

		/** To open up the Database and query for the EmployeeList **/
		/*
		 * employeeSQLite = new EmployeeSQLite(this); employeeSQLite.openDB();
		 * itemDetails = employeeSQLite.getJSONFromDB(); returnItemDetails =
		 * itemDetails; employeeSQLite.closeDB();
		 */

		/** Add the listener to the list of Employees items **/
		listItemArrAdapter = new EmployeeListItemArrayAdapter(this,
				R.id.linearLayoutEmployeeListItems, itemDetails);
		empListView.setAdapter(listItemArrAdapter);
		employeeCount = listItemArrAdapter.getCount();

		empListView.setOnItemClickListener(this);

		empSelectDialog.show();
		/* pdialog.dismiss(); */

	}

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

				/** When Finished ?? button is clicked **/
				empSelectBut.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						empSelectDialog.dismiss();
						ArrayList<Employee> selectedItemDetail = new ArrayList<Employee>();
						for (int i = 0; i < employeeCount; i++) {
							if (itemDetails.get(i).getChecked()) {
								selectedItemDetail.add(itemDetails.get(i));
								Log.e("Checked Items", ""
										+ itemDetails.get(i).getEmployeeName());
							}
						}
						String numbers = getEmployeeName(selectedItemDetail);
						if (numbers == null)
							numbers = "";
						String numberFromMsgTo = eventTo.getText().toString();
						if (!numberFromMsgTo.isEmpty()) {
							if (numbers.equals(""))
								numbers = numberFromMsgTo;
							else
								numbers = numberFromMsgTo + "; " + numbers;
							Log.e("Numbers from eventTo", "" + numbers);
						}
						eventTo.setText(numbers);
					}
				});
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

	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub

	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		listItemArrAdapter.getFilter().filter(s.toString().toLowerCase());
		listItemArrAdapter.notifyDataSetChanged();

	}

}
