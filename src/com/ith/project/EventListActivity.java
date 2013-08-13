package com.ith.project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Event;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.EntityClasses.Message;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.sqlite.*;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ImageView;

public class EventListActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private final static String getEventsUrl = "ListOfEvents";
	private final static String sendEvents = "CreateEvent";
	private final static String cancelEvents = "SetEventStatus";
	private final static String postponeEvents = "PostponeEvent";

	private Context context;
	private HttpConnection conn;
	private Dialog dialog, cancelDialog, deleteDialog;
	private EventSQLite eventSQLite;
	private MsgEntryLogSQLite msgEntryLogSQLite;
	private ImageButton menuButton, homeButton;
	private static ArrayList<Event> itemDetails;
	private ListView listView;
	private static EventItemArrayAdapter eventItemArrAdapter;
	private CallMenuDialog callDiag;
	private HashMap<String, String> menuItems;
	private static int eventCount;
	private Button buttonCancel, cancelConfirm, deleteConfirm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * pdialog = new ProgressDialog(this); pdialog.setCancelable(true);
		 * pdialog.setMessage("Loading ...."); pdialog.show();
		 */
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.list_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		context = this;
		init();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (eventSQLite != null)
			eventSQLite.closeDB();
		if (msgEntryLogSQLite != null)
			msgEntryLogSQLite.closeDB();
		/* pdialog.dismiss(); */
		// exitDialog.dismiss();
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	private void init() {

		Thread eventList = new Thread(new Runnable() {

			public void run() {

				conn = HttpConnection.getSingletonConn();
				eventSQLite = new EventSQLite(EventListActivity.this);
				msgEntryLogSQLite = new MsgEntryLogSQLite(
						EventListActivity.this);
				if (!eventSQLite.isOpen())
					eventSQLite.openDB();
				if (!msgEntryLogSQLite.isOpen())
					msgEntryLogSQLite.openDB();

				boolean connAvail = true;
				/**
				 * Before populating Event list try to update the pending Events
				 * operations to webservice
				 **/
				ArrayList<Event> pendingEventsSent = new ArrayList<Event>();
				pendingEventsSent = eventSQLite
						.selectPendingEvents("eventPending");
				if (pendingEventsSent != null) {

					ArrayList<Event> sentEvents = new ArrayList<Event>();
					int sender = LoginAuthentication.EmployeeId;
					String title = null, description = null, place = null, dateTime = null;
					String longitude = "", latitude = "";
					int[] receiversId = new int[pendingEventsSent.size()];
					int k = 0;
					for (int j = 0; j < pendingEventsSent.size(); j++) {

						if (sender == pendingEventsSent.get(j)
								.getEventCreator()) {

							sentEvents.add(pendingEventsSent.get(j));
							receiversId[k++] = pendingEventsSent.get(j)
									.getEventReceiver();
							title = pendingEventsSent.get(j).getEventName();
							description = pendingEventsSent.get(j)
									.getEventDesc();
							place = pendingEventsSent.get(j).getEventPlace();
							dateTime = pendingEventsSent.get(j)
									.getEventDateTime();
							longitude = pendingEventsSent.get(j).getLongitude();
							latitude = pendingEventsSent.get(j).getLatitude();
						}
					}

					JSONObject pendingEventsSentQuery = null;
					if (title != null && description != null) {

						pendingEventsSentQuery = makeNewMessageJSON(sender,
								receiversId, title, description, place,
								dateTime, longitude, latitude);

						Log.v(" pendingMessageQuery status", ""
								+ pendingEventsSentQuery.toString());
						String insertStatusStr = conn.getJSONFromUrl(
								pendingEventsSentQuery, sendEvents);

						if (insertStatusStr.startsWith("{")) {
							JSONObject insertStatusJson;
							try {
								insertStatusJson = new JSONObject(
										insertStatusStr);

								boolean insertStatus = (Boolean) insertStatusJson
										.get("CreateEventResult");

								/** If web service is successfully called **/
								if (insertStatus) {

									eventSQLite.deleteEvents(pendingEventsSent);
									Log.e("Deleted pending Sent Events ",
											"Until later retrieved from web service");
								} else {

									Log.e("Problem Posting Events ",
											"InsertStatus From Web Service"
													+ insertStatus);
								}
							} catch (JSONException e) {
								connAvail = false;
								Log.e("JSONException while posting Events", ""
										+ e.getMessage());
								e.printStackTrace();
							}
						} else {
							connAvail = false;
							Log.e("Server didn't response ",
									"InsertStatus From Web Service"
											+ insertStatusStr);
						}

					}
				}

				/** This is for sending the locally read messages to web service **/
				ArrayList<Event> pendingReadEvents = new ArrayList<Event>();// If
																			// the
																			// msgs
																			// are
																			// read
																			// locally

				pendingReadEvents = eventSQLite
						.selectPendingEvents("readUpdatePending");
				if (pendingReadEvents != null && connAvail) {

					for (int i = 0; i < pendingReadEvents.size(); i++) {
						/**
						 * Don't use EventId because msgId differs from that of
						 * the main DB. Same goes to MsgId also. However eventId
						 * can also be null so user with care
						 * **/
						int msgTo = pendingReadEvents.get(i).getEventReceiver();
						int msgRealId = pendingReadEvents.get(i)
								.getEventRealId();
						JSONObject readEventInquiry = EventViewActivity
								.makeEventReadJson(msgTo, msgRealId);

						Log.e("readEventsInquiry",
								"" + readEventInquiry.toString());
						String readStatusStr = conn.getJSONFromUrl(
								readEventInquiry, "MarkEventAsIsRead");
						Log.e("Event Read Status:", "" + readStatusStr);

						if (readStatusStr.startsWith("{")) {
							JSONObject readStatusJson;
							try {
								readStatusJson = new JSONObject(readStatusStr);

								boolean readUpdateStatus = (Boolean) readStatusJson
										.get("MarkEventAsIsReadResult");
								if (readUpdateStatus) {

									eventSQLite.updateEventRead(msgTo,
											msgRealId);
									eventSQLite.updateEventReadPending(msgTo,
											msgRealId, "");
									Log.e("read status Updated for Events",
											"Read Status Updated Succesfully");
								} else {

								}
							} catch (JSONException e) {
								Log.e("Web serrvice response error",
										"ma ka garnu ta aba??");
								e.printStackTrace();
							}
						}
					}

				}

				/**
				 * This is for sending the locally updated Going status to web
				 * service
				 **/
				ArrayList<Event> pendingGoingEvents = new ArrayList<Event>();// If
																				// the
																				// msgs
																				// are
																				// read
																				// locally

				pendingGoingEvents = eventSQLite
						.selectPendingEvents("goingUpdatePending");
				if (pendingGoingEvents != null && connAvail) {

					for (int i = 0; i < pendingGoingEvents.size(); i++) {

						int msgTo = pendingGoingEvents.get(i)
								.getEventReceiver();
						int msgRealId = pendingGoingEvents.get(i)
								.getEventRealId();
						JSONObject goingEventInquiry = EventViewActivity
								.makeEventGoingJson(msgTo, msgRealId,
										pendingGoingEvents.get(i)
												.getParticipationStatus());

						Log.e("GoingEventsInquiry",
								"" + goingEventInquiry.toString());
						String goingStatusStr = conn.getJSONFromUrl(
								goingEventInquiry, "SetUserEventStatus");
						Log.e("Event Going Status:", "" + goingStatusStr);

						if (goingStatusStr.startsWith("{")) {
							JSONObject goingStatusJson;
							try {
								goingStatusJson = new JSONObject(goingStatusStr);

								boolean goingUpdateStatus = (Boolean) goingStatusJson
										.get("SetUserEventStatusResult");
								if (goingUpdateStatus) {

									eventSQLite.updateEventGoing(msgTo,
											msgRealId, "");
									eventSQLite.updateEventReadPending(msgTo,
											msgRealId, "");
									Log.e("Going status Updated for Events",
											"Going Status Updated Succesfully");
								} else {

								}
							} catch (JSONException e) {
								Log.e("Web serrvice response error",
										"ma ka garnu ta aba??");
								e.printStackTrace();
							}
						}
					}

				}

				/** For sending locally cancelled Events to web service **/
				ArrayList<Event> pendingCancelEvents = new ArrayList<Event>();

				pendingCancelEvents = eventSQLite
						.selectPendingEvents("cancelEventPending");
				if (pendingCancelEvents != null && connAvail) {

					for (int i = 0; i < pendingCancelEvents.size(); i++) {
						/**
						 * Don't use EventId because msgId differs from that of
						 * the main DB. Same goes to MsgId also. However eventId
						 * can also be null so user with care
						 * **/
						int msgRealId = pendingCancelEvents.get(i)
								.getEventRealId();
						JSONObject cancelEventInquiry = getDeleteQuery(msgRealId);

						Log.e("cancelEventInquiry",
								"" + cancelEventInquiry.toString());

						String cancelStatusStr = conn.getJSONFromUrl(
								cancelEventInquiry, cancelEvents);

						Log.e("Event Cancel Status:", "" + cancelStatusStr);

						if (cancelStatusStr.startsWith("{")) {
							JSONObject cancelStatusJson;
							try {
								cancelStatusJson = new JSONObject(
										cancelStatusStr);

								boolean cancelUpdateStatus = (Boolean) cancelStatusJson
										.get("SetEventStatusResult");
								if (cancelUpdateStatus) {

									eventSQLite.cancelEvent(msgRealId, "");
									Log.e("Delete Successful !! :D",
											"Pending cancel Event Deleted Succesfully");
								} else {

								}
							} catch (JSONException e) {
								Log.e("Web serrvice response error",
										"gives jpt response o.O");
								e.printStackTrace();
							}
						}
					}

				}

				/** For sending locally postponed events to web service **/
				ArrayList<Event> pendingPostponedEvents = new ArrayList<Event>();

				pendingPostponedEvents = eventSQLite
						.selectPendingEvents("eventPostponed");
				if (pendingPostponedEvents != null && connAvail) {

					for (int i = 0; i < pendingPostponedEvents.size(); i++) {

						int eventRealId = pendingPostponedEvents.get(i)
								.getEventRealId();
						JSONObject postponedEventInquiry = getPostponeQuery(
								eventRealId, pendingPostponedEvents.get(i)
										.getEventDateTime());

						Log.e("postponedEventInquiry", ""
								+ postponedEventInquiry.toString());

						String postponedStatusStr = conn.getJSONFromUrl(
								postponedEventInquiry, postponeEvents);

						Log.e("Event Postpone Status:", "" + postponedStatusStr);

						if (postponedStatusStr.startsWith("{")) {
							JSONObject postponedStatusJson;
							try {
								postponedStatusJson = new JSONObject(
										postponedStatusStr);

								boolean postponedUpdateStatus = (Boolean) postponedStatusJson
										.get("PostponeEventResult");
								if (postponedUpdateStatus) {

									eventSQLite.updateEventPostponed(
											eventRealId, pendingPostponedEvents
													.get(i).getEventDateTime(),
											"");
									Log.e("PostPoned Successful !! :D",
											"Pending postponed Event updated Succesfully");
								} else {

								}
							} catch (JSONException e) {
								Log.e("Web serrvice response error",
										"gives jpt response o.O");
								e.printStackTrace();
							}
						}
					}

				}

				/** Make inquiry json for GetEvents List **/
				JSONObject inquiryJson = getInquiryJson(
						LoginAuthentication.UserloginId,
						LoginAuthentication.EmployeeId,
						msgEntryLogSQLite.getLatestMsgDateModified("eventLog"));

				Log.v("GetEvents inquiry", "" + inquiryJson.toString());
				/** To establish connection to the web service **/
				/* if (!connAvail) */{
					String eventsFromWS = conn.getJSONFromUrl(inquiryJson,
							getEventsUrl);
					boolean connFlag = true;
					/*
					 * JSONObject mainEvents = new JSONObject(); JSONArray
					 * ListOfEvents = new JSONArray(); JSONObject Desc = new
					 * JSONObject(); JSONObject EventId = new JSONObject();
					 * JSONObject a = new JSONObject(); JSONObject b = new
					 * JSONObject(); try { Desc.put("Description", "Test");
					 * Desc.put("EventId", 3); Desc.put("EventStatusId", 1);
					 * Desc.put("IsEventRead", 1); Desc.put("Latitude", "0.0");
					 * Desc.put("Longitude", "0.0");
					 * Desc.put("OrganizerEmployeeId", 4);
					 * Desc.put("StringEventDate", "20130728_110000");
					 * Desc.put("Title",
					 * "Meeting To Appreciate Employee Through Bonus");
					 * Desc.put("Venue", "Hotel Yak and Yeti");
					 * 
					 * JSONArray GoingValues = new JSONArray();
					 * GoingValues.put(0); GoingValues.put(2);
					 * GoingValues.put(2); GoingValues.put(1);
					 * GoingValues.put(1); Desc.put("IsGoing", GoingValues);
					 * 
					 * JSONArray EmployeeIds = new JSONArray();
					 * EmployeeIds.put(3); EmployeeIds.put(4);
					 * EmployeeIds.put(7); EmployeeIds.put(10);
					 * EmployeeIds.put(40); Desc.put("ToEmployeeId",
					 * EmployeeIds);
					 * 
					 * EventId.put("Description",
					 * "Training didai chan hamro Bill Gates ");
					 * EventId.put("EventId", 24); EventId.put("EventStatusId",
					 * 2); EventId.put("IsEventRead", 0);
					 * EventId.put("Latitude", "0.0"); EventId.put("Longitude",
					 * "0.0"); EventId.put("OrganizerEmployeeId", 3);
					 * EventId.put("StringEventDate", "20121129_102000");
					 * EventId.put("Title", "Seminar On Linux Talks");
					 * EventId.put("Venue", "Hotel Annapurna");
					 * 
					 * JSONArray GoingValues1 = new JSONArray();
					 * GoingValues1.put(1); GoingValues1.put(2);
					 * GoingValues1.put(0); EventId.put("IsGoing",
					 * GoingValues1);
					 * 
					 * JSONArray EmployeeIds1 = new JSONArray();
					 * EmployeeIds1.put(3); EmployeeIds1.put(4);
					 * EmployeeIds1.put(7); EventId.put("ToEmployeeId",
					 * EmployeeIds1);
					 * 
					 * a.put("Description", "Football khelna au hai keta hau");
					 * a.put("EventId", 20); a.put("EventStatusId", 3);
					 * a.put("IsEventRead", 0); a.put("Latitude", "0.0");
					 * a.put("Longitude", "0.0"); a.put("OrganizerEmployeeId",
					 * 4); a.put("StringEventDate", "20121129_102000");
					 * a.put("Title", "Football Match on Sunday");
					 * a.put("Venue", "Pepsi Cola");
					 * 
					 * JSONArray GoingValues2 = new JSONArray();
					 * GoingValues2.put(2); GoingValues2.put(1);
					 * GoingValues2.put(0); a.put("IsGoing", GoingValues2);
					 * 
					 * JSONArray EmployeeIds2 = new JSONArray();
					 * EmployeeIds2.put(3); EmployeeIds2.put(4);
					 * EmployeeIds2.put(7); a.put("ToEmployeeId", EmployeeIds2);
					 * 
					 * b.put("Description", "Hospital janu paryo");
					 * b.put("EventId", 10); b.put("EventStatusId", 4);
					 * b.put("IsEventRead", 1); b.put("Latitude", "0.0");
					 * b.put("Longitude", "0.0"); b.put("OrganizerEmployeeId",
					 * 3); b.put("StringEventDate", "20121129_102000");
					 * b.put("Title", "Injury Time"); b.put("Venue",
					 * "Jaishidewal");
					 * 
					 * JSONArray GoingValues3 = new JSONArray();
					 * GoingValues3.put(2); GoingValues3.put(1);
					 * GoingValues3.put(0); b.put("IsGoing", GoingValues3);
					 * 
					 * JSONArray EmployeeIds3 = new JSONArray();
					 * EmployeeIds3.put(4); EmployeeIds3.put(7);
					 * EmployeeIds3.put(3); b.put("ToEmployeeId", EmployeeIds3);
					 * 
					 * ListOfEvents.put(Desc); ListOfEvents.put(EventId);
					 * ListOfEvents.put(a); ListOfEvents.put(b);
					 * mainEvents.put("ListOfEventsResult", ListOfEvents); }
					 * catch (JSONException e) { // TODO Auto-generated catch
					 * block e.printStackTrace(); }
					 */
					// eventsFromWS = mainEvents.toString();
					// eventsFromWS = test.getText().toString();

					Log.v("Events:", "" + eventsFromWS);
					if (eventsFromWS.startsWith("{") /*
													 * ||
													 * !eventsFromWS.equals("")
													 */) {
						connFlag = false;
						/** Update the local sqlite according to the web service **/
						eventSQLite.updateEventTable(eventsFromWS,
								msgEntryLogSQLite);
					}
				}

				/** Now read from the local DB always **/
				itemDetails = eventSQLite.getEventsFromDB();

				homeButton = (ImageButton) findViewById(R.id.home);
				homeButton.setClickable(false);
				homeButton.setClickable(true);
				homeButton.setOnClickListener(EventListActivity.this);

				menuButton = (ImageButton) findViewById(R.id.menu);
				menuButton.setOnClickListener(EventListActivity.this);
				menuItems = new HashMap<String, String>();

				if (itemDetails != null)
				/** If list is null don't run this thread :D **/
				{

					runOnUiThread(new Runnable() {

						public void run() {

							listView = (ListView) findViewById(R.id.listView1);

							Log.d("***Welcome to Event ListView***", ".......");
							eventItemArrAdapter = new EventItemArrayAdapter(
									EventListActivity.this,
									R.layout.messages_list, itemDetails);
							listView.setAdapter(eventItemArrAdapter);
							eventCount = eventItemArrAdapter.getCount();
							listView.setOnItemClickListener(EventListActivity.this);

							/* pdialog.dismiss(); */
						}
					});

				} else {

				}
			}
		});
		eventList.start();

	}

	protected JSONObject getPostponeQuery(int eventRealId, String eventDateTime) {

		// JSONObject deleteEmp = new JSONObject();
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("eventId", new StringBuilder().append(eventRealId));
			jsonObject.put("eventPostponedDate", eventDateTime);
			jsonObject.put("employeeId",
					new StringBuilder().append(LoginAuthentication.EmployeeId));
			jsonObject.put("userLoginId", LoginAuthentication.UserloginId);
			// deleteEmp.put("SetEventStatus", jsonObject);
		} catch (JSONException e) {
			Log.e("JSONEXception @ getCancelEvent", "" + e.getMessage());
			e.printStackTrace();
		}
		return jsonObject;
	}

	/****************************************************************************
	 * View Event
	 *************************************************************************/
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		Log.v("EventlistItemClicked @" + (eventCount - position - 1),
				"HOOOrAyyy!!!!");

		/* pdialog.show(); */

		Intent intent = new Intent(EventListActivity.this,
				EventViewActivity.class);
		intent.putExtra("PositionOfEvent", (position));
		EventListActivity.this.startActivity(intent);

		// ListItemActivity.this.finish();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something on back.
			/* pdialog.show(); */
			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View v) {

		if (v.equals(menuButton)) {
			/* pdialog.show(); */
			Intent intent = new Intent(EventListActivity.this,
					GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			// menuItems.put("Send SMS", "mail_sms");
			menuItems.put("Add Events", "mail_web");
			// menuItems.put("Exit", "exit");
			callDiag = new CallMenuDialog(this, /* pdialog, */dialog, menuItems);
			// callMenuDialog();
		}
	}

	/*************************************************************************************
	 * Make a JSONObject of new Event that user fills
	 * ***************************************************************************************/
	protected JSONObject makeNewMessageJSON(int sender, int[] receiversId,
			String title, String description, String place, String dateTime,
			String longitude, String latitude) {

		JSONObject tempJsonFile = new JSONObject();
		JSONArray tempMsgTo = new JSONArray();
		StringBuilder msgToString = new StringBuilder();
		try {
			for (int i = 0; i < receiversId.length; i++) {
				tempMsgTo.put(receiversId[i]);
				if (i == receiversId.length - 1)
					msgToString.append(receiversId[i]);
				else
					msgToString.append(receiversId[i]).append(",");
			}

			tempJsonFile.put("organizerEmployeeId",
					new StringBuilder().append(sender));
			tempJsonFile.put("toEmployeeId", msgToString.toString());
			tempJsonFile.put("title", title);
			tempJsonFile.put("description", description);
			tempJsonFile.put("venue", place);
			tempJsonFile.put("eventDate", dateTime);
			tempJsonFile
					.put("longitude", new StringBuilder().append(longitude));
			tempJsonFile.put("latitude", new StringBuilder().append(latitude));
			tempJsonFile.put("userLoginId", LoginAuthentication.UserloginId);

		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return tempJsonFile;

	}

	/***************************************************************************************
	 * To get the inquiry json object to get the List of events
	 * ***************************************************************************************/
	protected static JSONObject getInquiryJson(String userLoginId, int msgTo,
			String startDate) {
		JSONObject tempJsonFile = new JSONObject();

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String currDate = dtFormat.format(calendar.getTime());

		if (startDate == null)
			startDate = "isFirstTime";// startDate = previousDate;
		try {
			tempJsonFile.put("userLoginId",
			/* "4A85CDB0-8822-43D1-B634-00039D9578C0" */userLoginId);
			tempJsonFile.put("startDateTime",/* "20130720151055" */startDate);
			tempJsonFile.put("endDateTime", /* "20130801191055" */currDate);
			tempJsonFile.put("employeeId",
					new StringBuilder().append(msgTo/* msgTo */));
			/*
			 * tempJsonFile.put("eventId", "2"); tempJsonFile.put("employeeId",
			 * "7");
			 */
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}

		return tempJsonFile;
	}

	/******************************************************************************************
	 * To get the delete Query for cancel Event
	 * *****************************************************************************************/
	protected JSONObject getDeleteQuery(int eventId) {

		// JSONObject deleteEmp = new JSONObject();
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("eventId", new StringBuilder().append(eventId));
			jsonObject.put("eventStatus", "cancel");
			jsonObject.put("employeeId",
					new StringBuilder().append(LoginAuthentication.EmployeeId));
			jsonObject.put("userLoginId", LoginAuthentication.UserloginId);
			// deleteEmp.put("SetEventStatus", jsonObject);
		} catch (JSONException e) {
			Log.e("JSONEXception @ getCancelEvent", "" + e.getMessage());
			e.printStackTrace();
		}
		return jsonObject;

	}

	/******************************************************************************************
	 * A new ArrayAdapter Class to handle the List View
	 * *****************************************************************************************/
	private class EventItemArrayAdapter extends ArrayAdapter<Event> {

		private Context cntxt;
		private ArrayList<Event> itemDets;
		private int IdOfCancelEvent, IdOfDeleteEvent;
		private String isPending;

		public EventItemArrayAdapter(Context context, int textViewResourceId,
				ArrayList<Event> itemDetails) {
			super(context, textViewResourceId);
			this.cntxt = context;
			this.itemDets = itemDetails;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) cntxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view;
			// if (convertView == null)
			{
				view = new View(this.cntxt);

				view = inflater.inflate(R.layout.event_list, parent, false);

				parent.setBackgroundColor(Color.rgb(221, 221, 221));

				TableLayout tableLayout = (TableLayout) view
						.findViewById(R.id.tableLayoutEventList);
				if (!this.itemDets.get(position).getEventReadStatus())
					tableLayout
							.setBackgroundResource(R.drawable.message_view_unread);

				TextView textView = (TextView) view
						.findViewById(R.id.textViewEventTitle);
				textView.setText(this.itemDets.get(position).getEventName());
				textView.setFocusable(false);

				EmployeeSQLite employeeSQLite = new EmployeeSQLite(
						EventListActivity.this);
				employeeSQLite.openDB();
				TextView msgFrom = (TextView) view
						.findViewById(R.id.textViewEventCreator);
				msgFrom.setText("Created By >> "
						+ employeeSQLite.getEmpName(this.itemDets.get(position)
								.getEventCreator()));
				msgFrom.setFocusable(false);
				employeeSQLite.closeDB();

				TextView time = (TextView) view
						.findViewById(R.id.textViewEventDate);
				time.setText("Happening On: "
						+ this.itemDets.get(position).getDate() + " @"
						+ itemDets.get(position).getTime());
				textView.setFocusable(false);
				// time.setVisibility(View.GONE);

				ImageView goingView = (ImageView) view
						.findViewById(R.id.imageViewEventParticipation);
				String goingStats = this.itemDets.get(position)
						.getParticipationStatus();
				int eventStatus = itemDets.get(position).getEventStatus();

				String imageName;

				if (eventStatus == 3 || goingStats.equals("Cancelled"))
					imageName = "cancel";
				else if (eventStatus == 2) {
					imageName = "complete";
				} else if (goingStats.equals("Going"))
					imageName = "green_dot";
				else if (goingStats.equals("NotGoing"))
					imageName = "red_dot";
				else
					imageName = "pending";

				int id = getResources().getIdentifier(imageName, "drawable",
						getApplicationContext().getPackageName());

				// imageView.setImageResource(R.drawable.user1);
				goingView.setImageResource(id);
				goingView.setFocusable(false);

				ImageButton deleteButton = (ImageButton) view
						.findViewById(R.id.imageViewDelete);
				deleteButton.setFocusable(false);
				// deleteButton.setFocusableInTouchMode(true);

				/**
				 * Check whether eventStatus is cancelled and Completed. If
				 * eventStatus == 3 || 2 then you can call the deleteEvent
				 * function if the close button is Clicked
				 **/
				if (eventStatus == 3 || eventStatus == 2
						|| goingStats.equals("Cancelled")) {

					IdOfDeleteEvent = itemDets.get(position).getEventId();
					view.setTag(deleteButton);
					deleteButton.setTag((Event) (itemDets).get(position));
					/**
					 * Set up a listener for Click in the Cross button for
					 * delete Event if Cancelled Flag is on
					 **/
					deleteButton.setOnClickListener(new OnClickListener() {

						public void onClick(View v) {

							ImageButton deletedButton = (ImageButton) v;
							Event deleteEventForDialog = (Event) deletedButton
									.getTag();

							deleteDialog = new Dialog(getApplicationContext());
							Button cancelDelete;
							
							/** To bring front the Dialog box **/
							deleteDialog = new Dialog(context);
							deleteDialog.setTitle("Confirm Delete");
							deleteDialog.setCanceledOnTouchOutside(false);

							/**
							 * To set the dialog box with the List layout in the
							 * android xml
							 **/
							deleteDialog.setContentView(R.layout.exit_dialog);

							deleteConfirm = (Button) deleteDialog
									.findViewById(R.id.buttonExitConfirm);
							deleteConfirm.setText("Delete");
							/* pdialog.dismiss(); */
							deleteDialog.show();

							cancelDelete = (Button) deleteDialog
									.findViewById(R.id.buttonExitCancel);
							cancelDelete
									.setOnClickListener(new OnClickListener() {

										public void onClick(View v) {
											deleteDialog.dismiss();

										}
									});

							deleteConfirm.setTag(deleteEventForDialog);
							deleteConfirm
									.setOnClickListener(new OnClickListener() {

										public void onClick(View v1) {
											/**
											 * To delete the Cancelled or
											 * confirmed Events when confirm
											 * button is clicked
											 * 
											 **/
											// ImageButton deletedButton =
											// (ImageButton) v;
											Button but = (Button) v1;
											Event deleteEvent = (Event) but
													.getTag();

											Log.e("Click chai vayo cross Button ma",
													"Event will be deleted now");

											Log.e("Deleted Item Id:", ""
													+ deleteEvent.getEventId());

											eventSQLite.deleteEvent(deleteEvent
													.getEventId());

											deleteDialog.dismiss();
											EventListActivity.this.finish();
											startActivity(new Intent(
													EventListActivity.this,
													EventListActivity.class));

										}
									});

						}
					});
				}
				/**
				 * Use the cross button for cancel Events (provided only to the
				 * creator of Events)
				 **/
				else if (this.itemDets.get(position).getEventCreator() == LoginAuthentication.EmployeeId) {

					IdOfCancelEvent = this.itemDets.get(position)
							.getEventRealId();
					isPending = this.itemDets.get(position).getEventType();

					/**
					 * Set up a listener for Click in the Cross button for
					 * cancel Event
					 **/
					deleteButton.setOnClickListener(new OnClickListener() {

						public void onClick(View v) {
							Log.e("La tyo cross click chai vayo",
									"tara herum k hunxa");
							cancelEvent();
						}
					});
				}
				/**
				 * If EventStatus is Pending OR Postponed, hide the deleteButton
				 **/
				else
					deleteButton.setVisibility(View.INVISIBLE);

			} // else
				// view = (View) convertView;

			return view;
		}

		/******************************************************************************************
		 * To cancel the Events i.e. notify that event will not happen
		 * *****************************************************************************************/
		private void cancelEvent() {

			/**
			 * Can cancel everytime overriding existing eventType; except when
			 * eventType='eventPending' because that event is not present in the
			 * main DB
			 **/
			if (isPending == null)
				isPending = "";
			Log.e("isPending is ", ": " + isPending);
			if (!isPending.equals("eventPending")) {

				/** To bring front the Dialog box **/
				cancelDialog = new Dialog(context);
				cancelDialog.setTitle("Confirm Exit");
				cancelDialog.setCanceledOnTouchOutside(false);

				/**
				 * To set the dialog box with the List layout in the android xml
				 **/
				cancelDialog.setContentView(R.layout.exit_dialog);

				cancelConfirm = (Button) cancelDialog
						.findViewById(R.id.buttonExitConfirm);
				cancelConfirm.setText("Cancel");
				// pdialog.dismiss();
				cancelDialog.show();

				buttonCancel = (Button) cancelDialog
						.findViewById(R.id.buttonExitCancel);
				buttonCancel.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						cancelDialog.dismiss();

					}
				});

				cancelConfirm.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						/** If confirm button is clicked event is cancelled **/
						eventSQLite.cancelEvent(IdOfCancelEvent,
								"cancelEventPending");

						/** Thread for cancelling the events to the web service **/
						Thread delThread = new Thread(new Runnable() {

							public void run() {
								JSONObject inputDelJson;

								inputDelJson = getDeleteQuery(IdOfCancelEvent);

								Log.e("Cancel Event ",
										"" + inputDelJson.toString());

								/**
								 * To establish connection to the web service
								 **/
								String delEventFromWS = conn.getJSONFromUrl(
										inputDelJson, cancelEvents);

								Log.e("Event Cancel Status:", ""
										+ delEventFromWS);

								try {
									JSONObject delReplyJson = new JSONObject(
											delEventFromWS);
									if (delReplyJson
											.getBoolean("SetEventStatusResult")) {
										eventSQLite.cancelEvent(
												IdOfCancelEvent, "");
									}
								} catch (JSONException e) {
									Log.e("JSON Parse Error @ MsgListAct", ""
											+ e.getMessage());
									e.printStackTrace();
								}

								runOnUiThread(new Runnable() {

									public void run() {
										/* pdialog.show(); */
										// exitDialog.dismiss();
										cancelDialog.dismiss();
										EventListActivity.this.finish();
										Intent intent = new Intent(
												EventListActivity.this,
												EventListActivity.class);
										EventListActivity.this
												.startActivity(intent);

									}

								});
							}

						});

						delThread.start();
					}
				});

			}

		}

		public int getCount() {

			return itemDets.size();
		}

		public Event getItem(int arg0) {

			return itemDets.get(arg0);
		}

		public long getItemId(int position) {

			return position;
		}

	}

	public static ArrayList<Event> getEventArrayList() {
		return itemDetails;
	}

}
