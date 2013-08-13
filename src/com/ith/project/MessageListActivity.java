package com.ith.project;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Employee;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.EntityClasses.Message;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.menu.CustomMenuListAdapter;
import com.ith.project.sqlite.EntryLogSQLite;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.MessageSQLite;
import com.ith.project.sqlite.MsgEntryLogSQLite;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MessageListActivity extends Activity implements OnClickListener,
		OnItemClickListener, OnDateChangedListener {

	private final String url = "GetMessages";
	private final String delUrl = "DeleteMessage";
	private ProgressDialog pdialog;
	private Dialog dialog;
	private MessageSQLite messageSQLite;
	private MsgEntryLogSQLite msgEntryLogSQLite;
	private static MessageListActivity context;
	private ImageButton menuButton;
	private ImageButton homeButton;
	private HttpConnection conn;
	static CustomMenuListAdapter menuAdapter;
	private static ArrayList<Message> itemDetails;
	private static ArrayList<Message> selectedItemDetails;
	private ListView listView;
	private static MessageItemArrayAdapter msgItemArrAdapter;
	private LinearLayout linLayoutPref;
	private static int messageCount, msgSpinner;
	private static boolean connFlag, msgStartFlag;
	private CallMenuDialog callDiag;
	private HashMap<String, String> menuItems;
	private Dialog prefDialog;
	private String msgBeginDate, msgEndDate;
	private DatePicker msgBeginDatePicker, msgEndDatePicker;
	private TextView msgStartTextView, msgEndTextView;
	private ImageView msgStartImageView, msgEndImageView;
	private Button buttonDefault, buttonSet;
	private int year1, year2, month1, month2, day1, day2;

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
		if (messageSQLite != null)
			messageSQLite.closeDB();
		if (msgEntryLogSQLite != null)
			msgEntryLogSQLite.closeDB();
		/* pdialog.dismiss(); */
		if (dialog != null)
			dialog.dismiss();

	}

	@Override
	public void onResume() {
		super.onResume();
		/* pdialog.dismiss(); */
	}

	/************************************************************************************
	 * Initialize values first
	 * ***********************************************************************************/
	private void init() {

		new Thread(new Runnable() {

			public void run() {
				selectedItemDetails = null;
				JSONObject inputJson;
				conn = HttpConnection.getSingletonConn();

				messageSQLite = new MessageSQLite(MessageListActivity.this);
				msgEntryLogSQLite = new MsgEntryLogSQLite(
						MessageListActivity.this);
				if (!messageSQLite.isOpen())
					messageSQLite.openDB();
				if (!msgEntryLogSQLite.isOpen())
					msgEntryLogSQLite.openDB();

				boolean connAvail = true;
				/**
				 * Before populating msg list try to update the pending msgs to
				 * webservice
				 **/

				ArrayList<Message> pendingMsgs = new ArrayList<Message>();
				pendingMsgs = messageSQLite.selectPendingMsgs("msgPending");
				if (pendingMsgs != null) {
					ArrayList<Message> sentMsgs = new ArrayList<Message>();// For
																			// only
																			// those
																			// msgs
																			// from
																			// logged
																			// in
																			// users

					// pendingMsgs =
					// messageSQLite.selectPendingMsgs("msgPending");

					/*
					 * int sender = -1; for (int i = 0; i < pendingMsgs.size();
					 * i++) { if (pendingMsgs.get(i).getMsgFrom() == sender)
					 * continue; else sender = pendingMsgs.get(0).getMsgFrom();
					 */
					int sender = LoginAuthentication.EmployeeId;
					String title = null, description = null;
					String[] titleArr = new String[pendingMsgs.size()];
					int[] tempReceiversId = new int[pendingMsgs.size()];
					int k = 0, l = 0;
					boolean hasTitle;
					for (int j = 0; j < pendingMsgs.size(); j++) {

						hasTitle = false;
						for (int i = 0; i < titleArr.length; i++) {
							if (pendingMsgs.get(j).getMsgTitle()
									.equals(titleArr[i])) {
								hasTitle = true;
								break;
							}
						}

						if ((sender == pendingMsgs.get(j).getMsgFrom())
								&& (!hasTitle)) {

							sentMsgs.add(pendingMsgs.get(j));
							title = pendingMsgs.get(j).getMsgTitle();
							description = pendingMsgs.get(j).getMsgDesc();
							for (Message mess : pendingMsgs) {
								if (title.equals(mess.getMsgTitle())) {

									tempReceiversId[k++] = mess.getMsgTo();
								}
							}
							int[] receiversId = new int[k];
							int m = 0;
							for (int rec : tempReceiversId) {
								if (rec != 0)
									receiversId[m++] = rec;
							}
							titleArr[l++] = title;

							/*
							 * if (title == null ||
							 * !title.equals(pendingMsgs.get(j) .getMsgTitle()))
							 * { title = pendingMsgs.get(j).getMsgTitle();
							 * description = pendingMsgs.get(j).getMsgDesc(); }
							 */
							JSONObject pendingMessageQuery = null;
							if (title != null) {
								pendingMessageQuery = makeNewMessageJSON(
										sender, receiversId, title, description);

								Log.v(" pendingMessageQuery status", ""
										+ pendingMessageQuery.toString());
								String insertStatusStr = conn.getJSONFromUrl(
										pendingMessageQuery, "SendMessage");

								if (insertStatusStr.startsWith("{")) {
									JSONObject insertStatusJson;
									try {
										insertStatusJson = new JSONObject(
												insertStatusStr);

										boolean insertStatus = (Boolean) insertStatusJson
												.get("SendMessageResult");

										if (insertStatus) {

											messageSQLite
													.deleteMessages(sentMsgs);
											Log.e("Deleted pending Msgs ",
													"Until later retrieved from web service");
										} else {

											Log.e("Problem Sending Message ",
													"InsertStatus From Web Service"
															+ insertStatus);
										}
									} catch (JSONException e) {
										connAvail = false;
										Log.e("JSONException while sending msgs",
												"" + e.getMessage());
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
					}

				}
				/* } */

				/** This is for sending the locally read messages to web service **/
				ArrayList<Message> pendingReadMsgs = new ArrayList<Message>();// If
																				// the
																				// msgs
																				// are
																				// read
																				// locally

				pendingReadMsgs = messageSQLite
						.selectPendingMsgs("readUpdatePending");
				if (pendingReadMsgs != null && connAvail) {

					for (int i = 0; i < pendingReadMsgs.size(); i++) {

						/** Don't use MsgId 'cause its different from main DB **/
						int msgTo = pendingReadMsgs.get(i).getMsgTo();
						int msgRealId = pendingReadMsgs.get(i).getMsgRealId();
						JSONObject readMsgInquiry = MessageViewActivity
								.makeMsgReadJson(msgTo, msgRealId);

						Log.e("readMsgInquiry", "" + readMsgInquiry.toString());
						String readStatusStr = conn.getJSONFromUrl(
								readMsgInquiry, "MarkMessageAsIsRead");
						Log.e("Messages Read Status:", "" + readStatusStr);

						if (readStatusStr.startsWith("{")) {
							JSONObject readStatusJson;
							try {
								readStatusJson = new JSONObject(readStatusStr);

								boolean readUpdateStatus = (Boolean) readStatusJson
										.get("MarkMessageAsIsReadResult");
								if (readUpdateStatus) {

									messageSQLite.updateMsgRead(msgTo,
											msgRealId);
									messageSQLite.updateReadPending(msgTo,
											msgRealId, "");
									Log.e("read status Updated",
											"Read Status Updated Succesfully");
								} else {
									messageSQLite.updateMsgRead(msgTo,
											msgRealId);
									messageSQLite.updateReadPending(msgTo,
											msgRealId, "msgPending");
									Log.e("server returned false for msgRead query",
											"Read Status Updated locally flag='msgPending' Succesfully");
								}
							} catch (JSONException e) {
								Log.e("Web serrvice response error",
										"ma ka garnu ta aba??");
								e.printStackTrace();
							}
						}
					}
				}

				/** Make inquiry json for message list **/
				inputJson = Message.getInquiryJson(
						LoginAuthentication.UserloginId,
						LoginAuthentication.EmployeeId, msgEntryLogSQLite
								.getLatestMsgDateModified("messageLog"));

				Log.v("getmessage inquiry", "" + inputJson.toString());

				/** To establish connection to the web service **/
				if (connAvail) {
					String messagesFromWS = conn.getJSONFromUrl(inputJson, url);
					connFlag = true;
					Log.v("Messages:", "" + messagesFromWS);
					if (!messagesFromWS.equals("")) {
						connFlag = false;
						/** Update the local file according to the web service **/
						messageSQLite.updateMessageTable(messagesFromWS,
								msgEntryLogSQLite);
					}
				}

				/** Now read from the local DB always **/
				itemDetails = messageSQLite.getMsgsFromDB();

				homeButton = (ImageButton) findViewById(R.id.home);
				homeButton.setClickable(false);

				menuButton = (ImageButton) findViewById(R.id.menu);
				menuButton.setOnClickListener(MessageListActivity.this);

				// registerForContextMenu(homeButton);
				/**
				 * To run the main thread after completion of the connection
				 * thread
				 **/
				if (itemDetails != null)
				/** If list is null donot run this thread :D **/
				{
					runOnUiThread(new Runnable() {

						public void run() {

							homeButton.setClickable(true);
							homeButton
									.setOnClickListener(MessageListActivity.this);

							listView = (ListView) findViewById(R.id.listView1);

							Log.d("***Welcome to Message ListView***",
									".......");
							msgItemArrAdapter = new MessageItemArrayAdapter(
									MessageListActivity.this,
									R.layout.messages_list, itemDetails);
							listView.setAdapter(msgItemArrAdapter);
							messageCount = msgItemArrAdapter.getCount();
							listView.setOnItemClickListener(MessageListActivity.this);

							menuItems = new HashMap<String, String>();
							/* pdialog.dismiss(); */
						}
					});
				} else {

				}
			}

		}).start();

	}

	/*************************************************************************************
	 * Make a JSONObject of new Message that user fills
	 * ***************************************************************************************/
	public JSONObject makeNewMessageJSON(int msgFrom, int[] msgTo,
			String msgTitle, String msgDesc) {
		JSONObject tempJsonFile = new JSONObject();
		JSONArray tempMsgTo = new JSONArray();
		StringBuilder msgToString = new StringBuilder();
		try {
			for (int i = 0; i < msgTo.length; i++) {
				tempMsgTo.put(msgTo[i]);
				if (i == msgTo.length - 1)
					msgToString.append(msgTo[i]);
				else
					msgToString.append(msgTo[i]).append(",");
			}

			tempJsonFile.put("fromEmployeeId", msgFrom);
			tempJsonFile.put("toEmployeeId", msgToString.toString());
			tempJsonFile.put("subject", msgTitle);
			tempJsonFile.put("description", msgDesc);
			tempJsonFile.put("userLoginId", LoginAuthentication.UserloginId);

		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return tempJsonFile;
	}

	/****************************************************************************
	 * View Message
	 *************************************************************************/
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		Log.v("MessagelistItemClicked @" + (messageCount - position - 1),
				"HOOOrAyyy!!!!");

		/* pdialog.show(); */

		Intent intent = new Intent(MessageListActivity.this,
				MessageViewActivity.class);
		intent.putExtra("PositionOfMessage", (position));
		MessageListActivity.this.startActivity(intent);

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
			Intent intent = new Intent(MessageListActivity.this,
					GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			// menuItems.put("Send SMS", "mail_sms");
			menuItems.put("Send Web Message", "mail_web");
			menuItems.put("Preferences", "preferences");
			menuItems.put("Delete Messages", "delete_user");
			// menuItems.put("Exit", "exit");
			callDiag = new CallMenuDialog(this, /* pdialog, */dialog, menuItems);
			// callMenuDialog();
		} else if (v.equals(buttonDefault)) {

			Message.isDefault = true;
			prefDialog.dismiss();

		} else if (v.equals(buttonSet)) {

			Message.isDefault = false;
			Message.setFirstCalendar(year1, month1, day1);
			Message.setSecondCalendar(year2, month2, day2);
			prefDialog.dismiss();

		} else if (v.equals(msgStartImageView)) {
			msgStartFlag = true;
			new DatePickerDialog(MessageListActivity.this, dateSet, year1,
					month1, day1).show();
		} else if (v.equals(msgEndImageView)) {
			msgStartFlag = false;
			new DatePickerDialog(this, dateSet, year2, month2, day2).show();
		} else
			Toast.makeText(this, "Menu Item Clicked", Toast.LENGTH_SHORT)
					.show();

	}

	public static ArrayList<Message> getMessageArrayList() {
		return itemDetails;
	}

	/*************************************************************************************
	 * Delete the Messages selected
	 **************************************************************************************/
	public void deleteMessages() {

		selectedItemDetails = getSelected();

		Thread delThread = new Thread(new Runnable() {

			public void run() {
				JSONObject inputDelJson;

				inputDelJson = Message.getDeleteQuery(selectedItemDetails);

				Log.e("Delete Messages ", "" + inputDelJson.toString());

				/** To establish connection to the web service **/
				String delMessagesFromWS = conn.getJSONFromUrl(inputDelJson,
						delUrl);

				Log.e("Messages Delete Status:", "" + delMessagesFromWS);

				try {
					JSONObject delReplyJson = new JSONObject(delMessagesFromWS);
					if (delReplyJson.getBoolean("DeleteMessageResult")) {
						messageSQLite.deleteMessages(selectedItemDetails);
					}
				} catch (JSONException e) {
					Log.e("JSON Parse Error @ MsgListAct", "" + e.getMessage());
					e.printStackTrace();
				}

				runOnUiThread(new Runnable() {

					public void run() {
						/* pdialog.show(); */
						// exitDialog.dismiss();
						MessageListActivity.this.finish();
						Intent intent = new Intent(MessageListActivity.this,
								MessageListActivity.class);
						MessageListActivity.this.startActivity(intent);

					}

				});
			}

		});
		delThread.start();

	}

	/****************************************************************************
	 * Get the Activity instance of ListItemActivity
	 *************************************************************************/
	public static MessageListActivity getMessageListActivityInstance() {
		return context;
	}

	/****************************************************************************
	 * Show the Preferences Dialog
	 *************************************************************************/
	public void showPreferencesDialog() {
		this.prefDialog = new Dialog(this);
		LayoutInflater prefInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		linLayoutPref = (LinearLayout) findViewById(R.id.linearLayoutPreferences);
		prefInflater.inflate(R.layout.preferences_screen, linLayoutPref, false);

		/** To bring front the Dialog box **/
		prefDialog = new Dialog(context);
		prefDialog.setTitle("Message Preferences");
		prefDialog.setCanceledOnTouchOutside(false);

		/** To set the dialog box with the List layout in the android xml **/
		prefDialog.setContentView(R.layout.preferences_screen);

		/** To get current default time **/
		// Calendar cal = Calendar.getInstance();
		Calendar cal = Message.getFirstCalendar();
		Log.e("FirstCalendar", "" + cal.getTime().toString());

		// msgBeginDatePicker = (DatePicker)
		// prefDialog.findViewById(R.id.datePickerMessageBegin);
		year1 = cal.get(Calendar.YEAR);
		month1 = cal.get(Calendar.MONTH);
		day1 = cal.get(Calendar.DAY_OF_MONTH);
		// msgBeginDatePicker.init(year1, month1, day1, this);

		/** To instantiate and initialize the MsgStartTextView **/
		msgStartTextView = (TextView) prefDialog
				.findViewById(R.id.prefMsgStartDate);
		if (msgStartTextView.getText().equals("date"))
			msgStartTextView.setText(year1 + "-" + month1 + "-" + day1);
		else
			msgStartTextView.setText(msgBeginDate);

		/** set up calendar button listener for MsgStartDate **/
		msgStartImageView = (ImageView) prefDialog
				.findViewById(R.id.imageViewMsgStartDate);
		msgStartImageView.setOnClickListener(this);

		/** To get 10 days before time **/
		// cal.add(Calendar.DAY_OF_YEAR, Message.DAY_INTERVAL_MESSAGES);
		Calendar cal2 = Message.getSecondCalendar();
		Log.e("SecondCalendar", "" + cal2.getTime().toString());

		year2 = cal2.get(Calendar.YEAR);
		month2 = cal2.get(Calendar.MONTH);
		day2 = cal2.get(Calendar.DAY_OF_MONTH);

		/** To instantiate and initialize the MsgEndTextView **/
		msgEndTextView = (TextView) prefDialog
				.findViewById(R.id.prefMsgEndDate);
		if (msgEndTextView.getText().equals("date"))
			msgEndTextView.setText(year2 + "-" + month2 + "-" + day2);
		else
			msgEndTextView.setText(msgEndDate);

		/** set up calendar button listener for MsgEndDate **/
		msgEndImageView = (ImageView) prefDialog
				.findViewById(R.id.imageViewMsgEndDate);
		msgEndImageView.setOnClickListener(this);

		/** For setting the Spinner items **/
		Spinner spinner = (Spinner) prefDialog
				.findViewById(R.id.spinnerMessage);
		ArrayList<String> spinnerItems = new ArrayList<String>();
		spinnerItems.add("Read");
		spinnerItems.add("Unread");
		spinnerItems.add("All");
		// msgSpinner = "All";
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, spinnerItems);

		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
		spinner.setSelection(msgSpinner);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				Log.e("Item at Spinner", ""
						+ parent.getItemAtPosition(pos).toString());
				// msgSpinner = parent.getItemAtPosition(pos).toString();
				msgSpinner = pos;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		buttonDefault = (Button) prefDialog.findViewById(R.id.buttonDefault);
		buttonDefault.setOnClickListener(this);

		buttonSet = (Button) prefDialog.findViewById(R.id.buttonSet);
		buttonSet.setOnClickListener(this);

		prefDialog.show();
	}

	/*****************************************************************************************************
	 * For Date Picker Dialog
	 * **************************************************************************************************/

	public void onDateChanged(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {

		// perform your required operation after date has been set
		String combinedDate = (new StringBuilder()).append(dayOfMonth)
				.append("-").append(monthOfYear).append("-").append(year)
				.toString(); // combinedDate == dd-mm-yyyy

		if (view == msgBeginDatePicker) {
			msgBeginDate = combinedDate;
			// Message.setFirstCalendar(year, monthOfYear, dayOfMonth);
			year1 = year;
			month1 = monthOfYear;
			day1 = dayOfMonth;
			Log.e("msgBeginDate	@DatePicker:", "" + msgBeginDate);
		} else if (view == msgEndDatePicker) {
			msgEndDate = combinedDate;
			// Message.setSecondCalendar(year, monthOfYear, dayOfMonth);
			year2 = year;
			month2 = monthOfYear;
			day2 = dayOfMonth;
			Log.e("msgEndDate	@DatePicker:", "" + msgEndDate);
		}
	}

	DatePickerDialog.OnDateSetListener dateSet = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			// perform your required operation after date has been set
			String combinedDate = (new StringBuilder()).append(dayOfMonth)
					.append("-").append(monthOfYear).append("-").append(year)
					.toString(); // combinedDate == dd-mm-yyyy

			Log.e("Inside the OnDateSet", "keep it on");
			if (msgStartFlag) {
				msgBeginDate = combinedDate;
				// Message.setFirstCalendar(year, monthOfYear, dayOfMonth);
				year1 = year;
				month1 = monthOfYear;
				day1 = dayOfMonth;
				msgStartTextView.setText(combinedDate);
				Log.e("msgBeginDate	@DatePicker:", "" + msgBeginDate);
			} else {
				msgEndDate = combinedDate;
				year2 = year;
				month2 = monthOfYear;
				day2 = dayOfMonth;
				msgEndTextView.setText(combinedDate);
				Log.e("msgEndDate	@DatePicker:", "" + msgEndDate);

			}
		}
	};

	/****************************************************************************
	 * To list the selected employees through CheckBoxes
	 *************************************************************************/
	public static ArrayList<Message> getSelected() {
		ArrayList<Message> selectedItemDetail = new ArrayList<Message>();
		for (int i = 0; i < messageCount; i++) {
			if (itemDetails.get(i).getChecked()) {
				selectedItemDetail.add(itemDetails.get(i));
				Log.e("Checked Items", "" + itemDetails.get(i).getMsgId());
			}
		}
		return selectedItemDetail;
	}

	/******************************************************************************************
	 * A new ArrayAdapter Class to handle the List View
	 * *****************************************************************************************/
	private class MessageItemArrayAdapter extends ArrayAdapter<Message> {

		private Context cntxt;
		private ArrayList<Message> itemDets;

		public MessageItemArrayAdapter(Context context, int textViewResourceId,
				ArrayList<Message> itemDetails) {
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

				view = inflater.inflate(R.layout.messages_list, parent, false);

				parent.setBackgroundColor(Color.rgb(221, 221, 221));

				TableLayout tableLayout = (TableLayout) view
						.findViewById(R.id.tableLayoutCheckBoxMessage);
				if (!this.itemDets.get(position).getMsgRead())
					tableLayout
							.setBackgroundResource(R.drawable.message_view_unread);

				TextView textView = (TextView) view
						.findViewById(R.id.textViewMessageTitle);
				textView.setText(this.itemDets.get(position).getMsgTitle());
				textView.setFocusable(false);

				EmployeeSQLite employeeSQLite = new EmployeeSQLite(
						MessageListActivity.this);
				employeeSQLite.openDB();
				TextView msgFrom = (TextView) view
						.findViewById(R.id.textViewMessageSender);
				msgFrom.setText("Sent By >> "
						+ employeeSQLite.getEmpName(this.itemDets.get(position)
								.getMsgFrom()));
				msgFrom.setFocusable(false);
				employeeSQLite.closeDB();

				TextView time = (TextView) view
						.findViewById(R.id.textViewMessageDate);
				time.setText("Created On: "
						+ this.itemDets.get(position).getDate());
				textView.setFocusable(false);
				// time.setVisibility(View.GONE);

				CheckBox checkBox = (CheckBox) view
						.findViewById(R.id.checkBoxMessage);
				view.setTag(checkBox);

				checkBox.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						CheckBox chkBox = (CheckBox) v;
						Message msg = (Message) chkBox.getTag();
						Log.e("CheckBox in Message checked@",
								"" + chkBox.isChecked());
						msg.setChecked(chkBox.isChecked());
					}

				});
				inflater = null;
				Message message = itemDets.get(position);
				checkBox.setChecked(message.getChecked());
				// holder.name.setChecked(employee.getChecked());
				// holder.name.setTag(employee);
				checkBox.setTag(message);
			} // else
				// view = (View) convertView;

			return view;
		}

		public int getCount() {

			return itemDets.size();
		}

		public Message getItem(int arg0) {

			return itemDets.get(arg0);
		}

		public long getItemId(int position) {

			return position;
		}
	}

}
