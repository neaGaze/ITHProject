package com.ith.project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Event;
import com.ith.project.EntityClasses.EventParticipants;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.googlemap.PinGMapActivity;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.EventGoingSQLite;
import com.ith.project.sqlite.EventSQLite;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;

public class EventViewActivity extends Activity implements OnClickListener {

	private final String readUrl = "MarkEventAsIsRead";
	private final String goingUrl = "SetUserEventStatus";
	private final String postponeUrl = "PostponeEvent";

	private Dialog dialog;
	private ImageButton menuButton, homeButton;
	private HashMap<String, String> menuItems;
	private JSONObject readEventInquiry, goingEventInquiry, postponeInquiry;
	private EmployeeSQLite employeeSQLite;
	private Event viewedEvent;
	private EventSQLite eventSQLite;
	private HttpConnection conn;
	private int position, eventTo, eventRealId, msgSpinner;
	private boolean isEventRead, readStatusUpdated;
	private ArrayList<String> spinnerItems;
	private String isPending, GoingStatus, day, month, year, hour, minute,
			latitudeStr, longitudeStr, eventDateTimeStr;
	private double latitude, longitude;
	private TextView EventName, EventDesc, EventDate, EventCreator, EventVenue,
			postponeDate, postponeTime;
	private Spinner GoingStatusSpinner;
	private Button submitPostpone;
	private ImageButton dateButton, timeButton, gMapButton, showInvited,
			hideInvited;
	private LinearLayout postponeLayout;
	private ArrayList<EventParticipants> eventsParticipants;
	private EventGoingSQLite eventGoingSQLite;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.event_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (employeeSQLite != null)
			employeeSQLite.closeDB();
		if (eventSQLite != null)
			eventSQLite.closeDB();
		if (dialog != null)
			dialog.dismiss();

		this.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void init() {
		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutEvent);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.event_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		position = bundle.getInt("EventId");

		eventSQLite = new EventSQLite(EventViewActivity.this);
		if (!eventSQLite.isOpen())
			eventSQLite.openDB();

		viewedEvent = eventSQLite.getViewedEvent(position);
		Log.v("EventName", "" + viewedEvent.getEventName());

		EventName = (TextView) findViewById(R.id.textViewEventName);
		EventName.setText(viewedEvent.getEventName());

		EventDesc = (TextView) findViewById(R.id.editTextEventDesc);
		EventDesc.setText(viewedEvent.getEventDesc());

		int eventStatus = viewedEvent.getEventStatus();

		employeeSQLite = new EmployeeSQLite(EventViewActivity.this);
		if (!employeeSQLite.isOpen())
			employeeSQLite.openDB();

		EventCreator = (TextView) findViewById(R.id.editTextEventCreator);
		EventCreator.setText(employeeSQLite.getEmpName(viewedEvent
				.getEventCreator()));

		EventDate = (TextView) findViewById(R.id.editTextEventDate);
		String eventDateStr = new StringBuilder().append(viewedEvent.getDate())
				.append(" @").append(viewedEvent.getTime()).toString();

		if (eventStatus == 2)
			EventDate.setText("COMPLETED");
		else if (eventStatus == 3)
			EventDate.setText("CANCELLED");
		else if (eventStatus == 4) {
			EventDate.setText(eventDateStr + " [POSTPONED]");
		} else
			EventDate.setText(eventDateStr);

		postponeLayout = (LinearLayout) findViewById(R.id.datePostponeEvent);

		if (LoginAuthentication.EmployeeId != viewedEvent.getEventCreator()
				|| (eventStatus == 2) || (eventStatus == 3)) {

			postponeLayout.setVisibility(View.GONE);
		} else {

			dateButton = (ImageButton) findViewById(R.id.imageViewEventPostponeDate);
			timeButton = (ImageButton) findViewById(R.id.imageViewEventPostponeTime);

			dateButton.setOnClickListener(this);
			timeButton.setOnClickListener(this);

			postponeDate = (TextView) findViewById(R.id.textViewEventPostponeDate);
			postponeTime = (TextView) findViewById(R.id.textViewEventPostponeTime);

			day = "";
			month = "";
			year = "";
			hour = "";
			minute = "";

			submitPostpone = (Button) findViewById(R.id.buttonPostpone);
			submitPostpone.setOnClickListener(this);
		}

		EventVenue = (TextView) findViewById(R.id.editTextEventVenue);
		EventVenue.setText(viewedEvent.getEventPlace());

		latitudeStr = viewedEvent.getLatitude();
		longitudeStr = viewedEvent.getLongitude();
		Log.e("Latitude ra longitude", "" + latitudeStr + ", " + longitudeStr);

		if (latitudeStr.equals("") || latitudeStr == null)
			latitude = 15.15;
		else
			latitude = Double.valueOf(latitudeStr).doubleValue();

		if (longitudeStr.equals("") || longitudeStr == null)
			longitude = 15.15;
		else
			longitude = Double.valueOf(longitudeStr).doubleValue();

		LinearLayout eventStatusLinLayout = (LinearLayout) findViewById(R.id.linearLayoutIsGoingStatus);
		GoingStatusSpinner = (Spinner) findViewById(R.id.spinnerGoingStatus);

		if (eventStatus == 2 || eventStatus == 3)
			eventStatusLinLayout.setVisibility(View.GONE);
		else
		/** Use Spinner for selecting whether to go or not to go in the Event **/
		{
			spinnerItems = new ArrayList<String>();
			spinnerItems.add("Going");
			spinnerItems.add("Not Going");
			spinnerItems.add("Pending");

			if (viewedEvent.getParticipationStatus().equals("Going"))
				msgSpinner = 0;
			else if (viewedEvent.getParticipationStatus().equals("NotGoing"))
				msgSpinner = 1;
			else
				msgSpinner = 2;

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, spinnerItems);

			dataAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			GoingStatusSpinner.setAdapter(dataAdapter);
			GoingStatusSpinner.setSelection(msgSpinner);
		}

		gMapButton = (ImageButton) findViewById(R.id.buttonGoogleMap);
		gMapButton.setOnClickListener(this);

		showInvited = (ImageButton) findViewById(R.id.buttonShowParticipants);
		showInvited.setOnClickListener(this);

		hideInvited = (ImageButton) findViewById(R.id.buttonHideParticipants);
		hideInvited.setOnClickListener(this);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(EventViewActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(EventViewActivity.this);

		menuItems = new HashMap<String, String>();

		/** Now Goto the updateReadStatus() method **/
		updateReadStatus();

		/** Check for spinner item change Listener **/
		GoingStatusSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						Log.e("Item at Spinner",
								"" + parent.getItemAtPosition(pos).toString());

						msgSpinner = pos;
						/** Now sync only when read Status in main DB is true **/
						if (readStatusUpdated) {
							updateGoingStatus(msgSpinner);
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {
						msgSpinner = 2;

					}
				});
	}

	/************************************************************************************
	 * Update the read status in both DB and in Web
	 * ***********************************************************************************/
	private void updateReadStatus() {

		eventTo = LoginAuthentication.EmployeeId;
		eventRealId = viewedEvent.getEventRealId();

		isEventRead = viewedEvent.getEventReadStatus();
		isPending = viewedEvent.getEventType();

		Log.e("Let's see the isEventRead Flag", "eventRead Flag is : "
				+ isEventRead);

		readStatusUpdated = false;
		eventSQLite = new EventSQLite(EventViewActivity.this);
		if (!eventSQLite.isOpen())
			eventSQLite.openDB();
		/**
		 * UPdate read Value Locally only if Event is unread and it is also in
		 * sync with main Database
		 **/
		if ((!isEventRead && (isPending == null || isPending.equals("")))) {

			eventSQLite.updateEventRead(eventTo, eventRealId);

			readEventInquiry = makeEventReadJson(eventTo, eventRealId);
		}
		/**
		 * GoingStatus can be synced if isEventRead = true && event is in sync
		 * with main DB
		 **/
		else if ((isEventRead && (isPending == null || isPending.equals(""))))
			readStatusUpdated = true;

		Thread eventReadUpdateThread = new Thread(new Runnable() {

			public void run() {
				conn = HttpConnection.getSingletonConn();
				Log.e("readeventInquiry", "" + readEventInquiry.toString());
				String readStatusStr = conn.getJSONFromUrl(readEventInquiry,
						readUrl);
				Log.e("Events Read Status:", "" + readStatusStr);

				JSONObject readStatusJson;
				try {
					readStatusJson = new JSONObject(readStatusStr);
					boolean readUpdateStatus = (Boolean) readStatusJson
							.get("MarkEventAsIsReadResult");
					if (readUpdateStatus) {

						Log.e("read status Updated",
								"Read Status Updated Succesfully");
						readStatusUpdated = true;

					} else {
						/**
						 * update the same event in sqlite with
						 * EventType="readUpdatePending" if connection is
						 * refused
						 **/
						eventSQLite.updateEventReadPending(eventTo,
								eventRealId, "readUpdatePending");

						Log.e("Problem Sending Event ",
								"Pending Read Events saved as draft"
										+ readUpdateStatus);
					}
				} catch (JSONException e) {
					/***
					 * Save the read event in sqlite if connection return is
					 * *JPT*
					 **/
					eventSQLite.updateEventReadPending(eventTo, eventRealId,
							"readUpdatePending");

					Log.e("JSONException while Updating read Status",
							"" + e.getMessage());
					Log.e("Events Saved for next Update",
							"Will be UPdated next time");
					e.printStackTrace();
				}

				runOnUiThread(new Runnable() {

					public void run() {

					}

				});
			}
		});
		if ((!isEventRead && (isPending == null || isPending.equals(""))))
			eventReadUpdateThread.start();

	}

	/************************************************************************************
	 * Notify the web service that the Employee is Attending the Event or not
	 * ***********************************************************************************/
	protected void updateGoingStatus(int goingStatus) {

		GoingStatus = viewedEvent.getParticipationStatus();

		if (goingStatus == 0)
			GoingStatus = "Going";
		else if (goingStatus == 1)
			GoingStatus = "NotGoing";
		else
			GoingStatus = "Pending";

		if (((isPending == null || isPending.equals("")))) {
			Log.e("La hai going Status ko ho",
					"sqlite will be updated below this");
			eventSQLite.updateEventGoing(eventTo, eventRealId, GoingStatus);

			goingEventInquiry = makeEventGoingJson(eventTo, eventRealId,
					GoingStatus);

		}

		/** Thread for updating Going status **/
		Thread goingStatusThread = new Thread(new Runnable() {

			public void run() {

				conn = HttpConnection.getSingletonConn();
				Log.e("goingEventInquiry", "" + goingEventInquiry.toString());
				String goingStatusStr = conn.getJSONFromUrl(goingEventInquiry,
						goingUrl);
				Log.e("Events going Status:", "" + goingStatusStr);

				JSONObject goingStatusJson;
				try {
					goingStatusJson = new JSONObject(goingStatusStr);
					boolean goingUpdateStatus = (Boolean) goingStatusJson
							.get("SetUserEventStatusResult");
					if (goingUpdateStatus) {

						eventSQLite.updateEventGoing(eventTo, eventRealId,
								GoingStatus);
						Log.e("going status Updated",
								"going Status Updated Succesfully");

					} else {
						/**
						 * update the same event in sqlite with
						 * EventType="goingUpdatePending" if connection is
						 * refused
						 **/
						eventSQLite.updateGoingPending(eventTo, eventRealId,
								"goingUpdatePending");

						Log.e("Problem Sending Going Event ",
								"Pending Going Events saved as draft"
										+ goingUpdateStatus);
					}
				} catch (JSONException e) {
					/***
					 * Save the going event in sqlite if connection return is
					 * *JPT*
					 **/
					eventSQLite.updateGoingPending(eventTo, eventRealId,
							"goingUpdatePending");

					Log.e("JSONException while Updating going Status",
							"" + e.getMessage());
					Log.e("Events Saved for next Update",
							"Going status Will be UPdated next time");
					e.printStackTrace();
				}

			}
		});
		if ((isPending == null || isPending.equals("")))
			goingStatusThread.start();
	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			menuItems.put("Send Events", "mail_web");
			new CallMenuDialog(this, dialog, menuItems);
		} else if (v.equals(showInvited)) {
			showInvitedList();
		} else if (v.equals(hideInvited)) {
			ListView listView = (ListView) findViewById(R.id.listViewToReceivers);
			listView.setVisibility(View.INVISIBLE);
		}
		/** Launch DateSet Dialog **/
		else if (v.equals(dateButton)) {
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
		/** Launch TimeSet Dialog **/
		else if (v.equals(timeButton)) {
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
		} else if (v.equals(submitPostpone)) {

			if (year != "" && month != "" && day != "" && hour != ""
					&& minute != "") {
				eventDateTimeStr = new StringBuilder().append(year)
						.append(month).append(day).append("_").append(hour)
						.append(minute).append("00").toString();

				postponeInquiry = postponeEventJson(eventDateTimeStr,
						LoginAuthentication.EmployeeId,
						viewedEvent.getEventRealId());

				Log.v("postponeEvent status", "" + postponeInquiry.toString());
			}
			/** send the JSONObject to postpone the Events in the database **/
			Thread postponeThread = new Thread(new Runnable() {

				public void run() {

					conn = HttpConnection.getSingletonConn();

					Log.e("insertStringJson", "" + postponeInquiry.toString());
					String postponeStatusStr = conn.getJSONFromUrl(
							postponeInquiry, postponeUrl);
					Log.e("Here comes postponeStatusStr", ""
							+ postponeStatusStr);
					eventSQLite = new EventSQLite(EventViewActivity.this);
					eventSQLite.openDB();
					try {

						JSONObject postponeStatusJson = new JSONObject(
								postponeStatusStr);

						boolean postponeStatus = (Boolean) postponeStatusJson
								.get("PostponeEventResult");

						if (postponeStatus) {

							Log.e("Event has been postponed",
									"EVENT postponed successfully !!!");
						} else {

							eventSQLite.updateEventPostponed(eventRealId,
									eventDateTimeStr, "eventPostponed");

							Log.e("Problem Postponding Event ",
									"Postponed Event written in sqlite"
											+ postponeStatus);
						}

					} catch (JSONException e) {
						/**
						 * Save the postponed msgs as draft in sqlite if
						 * connection return is *JPT*
						 **/
						eventSQLite.updateEventPostponed(eventRealId,
								eventDateTimeStr, "eventPostponed");

						Log.e("JSONException while postponding event",
								"" + e.getMessage());
						Log.e("Postponed Events Saved as Draft",
								"Postponed events will be sent next time");
						e.printStackTrace();
					}
					eventSQLite.closeDB();
					runOnUiThread(new Runnable() {

						public void run() {
							Intent intent = new Intent(EventViewActivity.this,
									EventListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);

						}
					});
				}

			});
			if (year != "" && month != "" && day != "" && hour != ""
					&& minute != "")
				postponeThread.start();
		} else if (v.equals(gMapButton)) {
			Intent intent = new Intent(EventViewActivity.this,
					PinGMapActivity.class);
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
			startActivity(intent);

		} else {

		}

	}

	/**
	 * make a json out of postponeEventInquiry
	 * **/
	private JSONObject postponeEventJson(String eventDateTime, int empId,
			int eventRealId) {

		JSONObject postponeInquiry = new JSONObject();
		try {

			postponeInquiry.put("eventId",
					new StringBuilder().append(eventRealId).toString());
			postponeInquiry.put("employeeId", new StringBuilder().append(empId)
					.toString());
			postponeInquiry.put("userLoginId", LoginAuthentication.UserloginId);
			postponeInquiry.put("eventPostponedDate", eventDateTime);
			return postponeInquiry;
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return null;
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
			postponeDate.setText(combinedDate);
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
			postponeTime.setText(combinedTime);

		}
	};

	/***********************************************************************************
	 * Show the Other Invited People
	 ************************************************************************************/
	private void showInvitedList() {

		eventGoingSQLite = new EventGoingSQLite(getApplicationContext());
		if (!eventGoingSQLite.isOpen())
			eventGoingSQLite.openDB();

		eventsParticipants = eventGoingSQLite.getEventGoing(viewedEvent
				.getEventRealId());
		eventGoingSQLite.closeDB();

		if (eventsParticipants != null) {
			ListView listView = (ListView) findViewById(R.id.listViewToReceivers);
			listView.setVisibility(View.VISIBLE);
			Log.d("***Welcome to Event ListView***", ".......");
			EventParticipantsArrayAdapter eventItemArrAdapter = new EventParticipantsArrayAdapter(
					EventViewActivity.this, R.layout.event_participants,
					eventsParticipants);
			listView.setAdapter(eventItemArrAdapter);
		}

	}

	/**********************************************************************************
	 * To make a JSON Object for inquiry of read event
	 * **********************************************************************************/
	public static JSONObject makeEventReadJson(int eventTo2, int eventRealId2) {

		JSONObject readInquiry = new JSONObject();
		try {
			readInquiry.put("employeeId", new StringBuilder().append(eventTo2)
					.toString());
			readInquiry.put("eventId", new StringBuilder().append(eventRealId2)
					.toString());
			return readInquiry;
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**********************************************************************************
	 * To make a JSON Object for inquiry of GoingUpdate Status event
	 * **********************************************************************************/
	public static JSONObject makeEventGoingJson(int eventTo2, int eventRealId2,
			String goingStatus) {

		JSONObject readInquiry = new JSONObject();
		try {

			readInquiry.put("eventId", new StringBuilder().append(eventRealId2)
					.toString());
			readInquiry.put("employeeId", new StringBuilder().append(eventTo2)
					.toString());
			readInquiry.put("userLoginId", LoginAuthentication.UserloginId);
			readInquiry.put("eventStatus", goingStatus);

			return readInquiry;
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return null;

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
	 * A new ArrayAdapter Class to handle the List View of Participating
	 * Employees
	 * *****************************************************************************************/
	private class EventParticipantsArrayAdapter extends
			ArrayAdapter<EventParticipants> {

		private Context cntxt;
		private ArrayList<EventParticipants> itemDets;

		public EventParticipantsArrayAdapter(Context context,
				int textViewResourceId, ArrayList<EventParticipants> itemDetails) {
			super(context, textViewResourceId);
			this.cntxt = context;
			this.itemDets = itemDetails;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) cntxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = new View(getApplicationContext());
			view = inflater.inflate(R.layout.event_participants, parent, false);

			parent.setBackgroundColor(Color.rgb(221, 221, 221));

			EmployeeSQLite employeeSQLite = new EmployeeSQLite(
					EventViewActivity.this);
			employeeSQLite.openDB();
			TextView textView = (TextView) view
					.findViewById(R.id.textViewParticipants);
			textView.setText(employeeSQLite.getEmpName(this.itemDets.get(
					position).getToEmployeeId()));
			textView.setFocusable(false);
			employeeSQLite.closeDB();

			ImageView imageView = (ImageView) view
					.findViewById(R.id.imageViewParticipantGoing);
			String viewImage;
			if (this.itemDets.get(position).getGoingStatus().equals("NotGoing"))
				viewImage = "red_dot";
			else if (this.itemDets.get(position).getGoingStatus()
					.equals("Going"))
				viewImage = "green_dot";
			else
				viewImage = "pending";

			int id = getResources().getIdentifier(viewImage, "drawable",
					getApplicationContext().getPackageName());

			imageView.setImageResource(id);

			return view;
		}

		public int getCount() {

			return itemDets.size();
		}

		public EventParticipants getItem(int arg0) {

			return itemDets.get(arg0);
		}

		public long getItemId(int position) {

			return position;
		}
	}
}
