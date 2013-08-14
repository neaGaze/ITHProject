package com.ith.project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Leave;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.LeaveSQLite;
import com.ith.project.sqlite.MsgEntryLogSQLite;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class LeaveListActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private final static String getLeaveUrl = "GetLeaveList";
	private final static String responseUrl = "SetLeaveStatus";
	private final static String notificationSentUrl = "MarkLeaveRequestAsIsRead";
	private final static String SendLeaveRequestUrl = "SendLeaveRequest";

	private HttpConnection conn;
	private Dialog dialog, deleteDialog;
	private HashMap<String, String> menuItems;
	private LeaveSQLite leaveSQLite;
	private MsgEntryLogSQLite msgEntryLogSQLite;
	private ImageButton menuButton, homeButton;
	private static ArrayList<Leave> itemDetails;
	private ListView listView;
	private static LeaveItemArrayAdapter leaveItemArrAdapter;
	private static int leaveCount;
	private int leaveId;
	private Context context;
	private Button deleteConfirm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		if (leaveSQLite != null)
			leaveSQLite.closeDB();
		if (msgEntryLogSQLite != null)
			msgEntryLogSQLite.closeDB();
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void init() {

		Thread leaveThread = new Thread(new Runnable() {

			public void run() {

				conn = HttpConnection.getSingletonConn();
				leaveSQLite = new LeaveSQLite(getApplicationContext());
				msgEntryLogSQLite = new MsgEntryLogSQLite(
						getApplicationContext());
				if (!leaveSQLite.isOpen())
					leaveSQLite.openDB();
				if (!msgEntryLogSQLite.isOpen())
					msgEntryLogSQLite.openDB();

				/**
				 * Before populating Leave list try to update the pending Leave
				 * operations to webservice
				 **/
				ArrayList<Leave> pendingLeaveSent = new ArrayList<Leave>();
				pendingLeaveSent = leaveSQLite
						.selectPendingLeave("leavePending");
				if (pendingLeaveSent != null
						&& HttpConnection.getConnectionAvailable(context)) {

					int sender = LoginAuthentication.EmployeeId;
					String remark = null, dateTime = null;
					int approvalId, leaveType;
					for (int j = 0; j < pendingLeaveSent.size(); j++) {

						if (sender == pendingLeaveSent.get(j).getApplicantId()) {
							leaveId = pendingLeaveSent.get(j).getLeaveId();
							approvalId = pendingLeaveSent.get(j)
									.getApprovalId();
							leaveType = pendingLeaveSent.get(j)
									.getLeaveTypeId();
							remark = pendingLeaveSent.get(j).getLeaveRemarks();
							dateTime = pendingLeaveSent.get(j)
									.getLeaveStartDate();

							JSONObject pendingLeaveSentQuery = null;

							pendingLeaveSentQuery = Leave.makeNewLeaveJSON(
									sender, approvalId, leaveType, dateTime,
									remark);

							Log.v(" pendingLeaveQuery status", ""
									+ pendingLeaveSentQuery.toString());
							String insertStatusStr = conn.getJSONFromUrl(
									pendingLeaveSentQuery, SendLeaveRequestUrl);

							if (insertStatusStr.startsWith("{")) {
								JSONObject insertStatusJson;
								boolean insertStatus = false;
								try {
									insertStatusJson = new JSONObject(
											insertStatusStr);

									insertStatus = (Boolean) insertStatusJson
											.get("SendLeaveRequestResult");

								} catch (JSONException e) {

									Log.e("JSONException while posting Leave",
											"" + e.getMessage());
									e.printStackTrace();
								}
								/** If web service is successfully called **/
								if (insertStatus) {

									leaveSQLite.deleteLeave(leaveId);
									Log.e("Deleted pending Sent Leave ",
											"Until later retrieved from web service");
								} else {

									Log.e("Problem Posting Leave ",
											"InsertStatus From Web Service"
													+ insertStatus);
								}

							} else {
								Log.e("Server didn't response ",
										"InsertStatus From Web Service"
												+ insertStatusStr);
							}
						}
					}
				}

				/**
				 * This is for sending the locally read Leave application to web
				 * service
				 **/
				ArrayList<Leave> pendingReadLeave = new ArrayList<Leave>();// If
																			// the
																			// msgs
																			// are
																			// read
																			// locally

				pendingReadLeave = leaveSQLite
						.selectPendingLeave("notificationSentPending");
				if (pendingReadLeave != null
						&& HttpConnection.getConnectionAvailable(context)) {

					for (int i = 0; i < pendingReadLeave.size(); i++) {

						int leaveTo = pendingReadLeave.get(i).getApprovalId();
						int leaveId = pendingReadLeave.get(i).getLeaveId();
						JSONObject readLeaveInquiry = LeaveViewActivity
								.makeLeaveNotificationSentJson(leaveTo, leaveId);

						Log.e("readLeaveInquiry",
								"" + readLeaveInquiry.toString());
						String readStatusStr = conn.getJSONFromUrl(
								readLeaveInquiry, notificationSentUrl);
						Log.e("Read Leave Status:", "" + readStatusStr);

						if (readStatusStr.startsWith("{")) {
							JSONObject readStatusJson;
							boolean readUpdateStatus = false;
							try {
								readStatusJson = new JSONObject(readStatusStr);

								readUpdateStatus = (Boolean) readStatusJson
										.get("MarkLeaveRequestAsIsReadResult");
							} catch (JSONException e) {
								Log.e("Web service response error",
										"ma ka garnu ta aba??");
								e.printStackTrace();
							}

							if (readUpdateStatus) {

								leaveSQLite
										.updateLeaveNotificationSent(leaveId);
								leaveSQLite.updateLeaveNotificationSentPending(
										leaveId, "");
								Log.e("read status Updated for Leave",
										"Read Status Updated Succesfully");
							}

						}
					}

				}

				/**
				 * This is for sending the locally updated Approval status to
				 * web service
				 **/
				ArrayList<Leave> pendingApprovalLeave = new ArrayList<Leave>();// If
																				// the
																				// msgs
																				// are
																				// read
																				// locally

				pendingApprovalLeave = leaveSQLite
						.selectPendingLeave("approvalUpdatePending");
				if (pendingApprovalLeave != null
						&& HttpConnection.getConnectionAvailable(context)) {

					for (int i = 0; i < pendingApprovalLeave.size(); i++) {

						int leaveTo = pendingApprovalLeave.get(i)
								.getApprovalId();
						int leaveId = pendingApprovalLeave.get(i).getLeaveId();

						JSONObject approvalLeaveInquiry = LeaveViewActivity
								.makeLeaveStatusInquiryJson(leaveId,
										pendingApprovalLeave.get(i)
												.getLeaveStatus(), leaveTo);

						Log.e("approvalLeaveInquiry",
								"" + approvalLeaveInquiry.toString());
						String approvalStatusStr = conn.getJSONFromUrl(
								approvalLeaveInquiry, responseUrl);
						Log.e("Leave Approval Status:", "" + approvalStatusStr);

						if (approvalStatusStr.startsWith("{")) {
							JSONObject approvalStatusJson;
							boolean approvalUpdateStatus = false;
							try {
								approvalStatusJson = new JSONObject(
										approvalStatusStr);

								approvalUpdateStatus = (Boolean) approvalStatusJson
										.get("SetLeaveStatusResult");
							} catch (JSONException e) {
								Log.e("Web serrvice response error",
										"ma ka garnu ta aba??");
								e.printStackTrace();
							}
							if (approvalUpdateStatus) {

								leaveSQLite.updateLeaveStatusPending(leaveId,
										"");
								leaveSQLite.updateLeaveNotificationSentPending(
										leaveId, "");
								Log.e("Approval status Updated for Leave",
										"Approval Status Updated Succesfully");
							}

						}
					}

				}

				/** Make inquiry json for GetLeaveRequest List **/
				JSONObject inquiryJson = getInquiryJson(
						LoginAuthentication.UserloginId,
						LoginAuthentication.EmployeeId,
						msgEntryLogSQLite.getLatestMsgDateModified("leaveLog"));

				Log.v("GetLeave inquiry", "" + inquiryJson.toString());
				/** To establish connection to the web service **/

				String leaveFromWS = conn.getJSONFromUrl(inquiryJson,
						getLeaveUrl);
				Log.v("Leave Request:", "" + leaveFromWS);

				if (leaveFromWS.startsWith("{")) {

					/** Update the local sqlite according to the web service **/
					leaveSQLite
							.updateLeaveTable(leaveFromWS, msgEntryLogSQLite);
				}

				/** Now read from the local DB always **/
				itemDetails = leaveSQLite.getLeaveFromDB();

				homeButton = (ImageButton) findViewById(R.id.home);
				homeButton.setClickable(false);
				homeButton.setClickable(true);
				homeButton.setOnClickListener(LeaveListActivity.this);

				menuButton = (ImageButton) findViewById(R.id.menu);
				menuButton.setOnClickListener(LeaveListActivity.this);
				menuItems = new HashMap<String, String>();

				if (itemDetails != null)
				/** If list is null don't run this thread :D **/
				{

					runOnUiThread(new Runnable() {

						public void run() {

							listView = (ListView) findViewById(R.id.listView1);

							Log.d("***Welcome to Leave ListView***", ".......");
							leaveItemArrAdapter = new LeaveItemArrayAdapter(
									LeaveListActivity.this,
									R.layout.leave_list, itemDetails);
							listView.setAdapter(leaveItemArrAdapter);
							leaveCount = leaveItemArrAdapter.getCount();
							listView.setOnItemClickListener(LeaveListActivity.this);

						}
					});

				}
			}
		});
		leaveThread.start();
	}

	/***************************************************************************************
	 * To get the inquiry json object to get the LeaveRequest
	 * ***************************************************************************************/
	protected static JSONObject getInquiryJson(String userLoginId, int msgTo,
			String startDate) {
		JSONObject tempJsonFile = new JSONObject();

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmmss",
				Locale.US);
		String currDate = dtFormat.format(calendar.getTime());

		if (startDate == null)
			startDate = "isFirstTime";// startDate = previousDate;
		try {
			tempJsonFile.put("userLoginId", userLoginId);
			tempJsonFile.put("startDateTime", startDate);
			tempJsonFile.put("endDateTime", currDate);
			tempJsonFile.put("employeeId", new StringBuilder().append(msgTo));

		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}

		return tempJsonFile;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View v) {

		if (v.equals(menuButton)) {
			Intent intent = new Intent(LeaveListActivity.this,
					GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			menuItems.put("Fill Form", "mail_web");
			new CallMenuDialog(this, dialog, menuItems);

		}

	}

	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		Log.v("LeavelistItemClicked @" + (leaveCount - position - 1),
				"HOOOrAyyy!!!!");

		Intent intent = new Intent(LeaveListActivity.this,
				LeaveViewActivity.class);
		intent.putExtra("LeaveId", itemDetails.get(position).getLeaveId());
		LeaveListActivity.this.startActivity(intent);

	}

	/******************************************************************************************
	 * A new ArrayAdapter Class to handle the List View
	 * *****************************************************************************************/
	private class LeaveItemArrayAdapter extends ArrayAdapter<Leave> {

		private Context cntxt;
		private ArrayList<Leave> itemDets;

		public LeaveItemArrayAdapter(Context context, int textViewResourceId,
				ArrayList<Leave> itemDetails) {
			super(context, textViewResourceId);
			this.cntxt = context;
			this.itemDets = itemDetails;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) cntxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = new View(this.cntxt);
			view = inflater.inflate(R.layout.leave_list, parent, false);

			parent.setBackgroundColor(Color.rgb(221, 221, 221));

			TableLayout tableLayout = (TableLayout) view
					.findViewById(R.id.tableLayoutLeaveList);
			if (!this.itemDets.get(position).getNotificationSentStatus())
				tableLayout
						.setBackgroundResource(R.drawable.message_view_unread);

			/** For incoming or outgoing Application icon **/
			ImageView leaveIcon = (ImageView) view
					.findViewById(R.id.imageViewReceiver);
			String iconName;
			if (this.itemDets.get(position).getApplicantId() == LoginAuthentication.EmployeeId)
				iconName = "upload";
			else
				iconName = "download";
			int iconRes = getResources().getIdentifier(iconName, "drawable",
					getApplicationContext().getPackageName());
			leaveIcon.setImageResource(iconRes);
			leaveIcon.setFocusable(false);

			/** For Employee Name TextView **/
			EmployeeSQLite employeeSQLite = new EmployeeSQLite(
					LeaveListActivity.this);
			employeeSQLite.openDB();
			TextView textView = (TextView) view
					.findViewById(R.id.textViewRespectivePerson);
			if (this.itemDets.get(position).getApplicantId() == LoginAuthentication.EmployeeId)
				textView.setText(employeeSQLite.getEmpName(this.itemDets.get(
						position).getApprovalId()));
			else
				textView.setText(employeeSQLite.getEmpName(this.itemDets.get(
						position).getApplicantId()));
			textView.setFocusable(false);
			employeeSQLite.closeDB();

			/** For LeaveDate TextView **/
			TextView leaveDate = (TextView) view
					.findViewById(R.id.textViewLeaveDate);
			if (itemDets.get(position).getLeaveEndDate() == null
					|| itemDets.get(position).getLeaveEndDate().equals("")) {
				leaveDate.setText("Leave Date: "
						+ this.itemDets.get(position).getFormattedDate());
			} else {
				leaveDate.setText("Leave Date: "
						+ this.itemDets.get(position).getFormattedDate()
						+ " To " + itemDets.get(position).getLeaveEndDate());
			}
			textView.setFocusable(false);

			/** For Response Icon **/
			ImageView leaveStatusView = (ImageView) view
					.findViewById(R.id.imageViewLeaveApproval);
			int leaveStatus = itemDets.get(position).getLeaveStatus();

			String imageName;

			if (leaveStatus == 3)
				imageName = "thumbs_down";
			else if (leaveStatus == 2)
				imageName = "thumbs_up";
			else
				imageName = "pending";

			int id = getResources().getIdentifier(imageName, "drawable",
					getApplicationContext().getPackageName());

			leaveStatusView.setImageResource(id);
			leaveStatusView.setFocusable(false);

			/** For deleteIcon **/
			ImageButton deleteButton = (ImageButton) view
					.findViewById(R.id.imageViewLeaveDelete);
			deleteButton.setFocusable(false);
			/**
			 * Check whether leaveStatus is Approved OR Declined. If leaveStatus
			 * == 3 || 2 then you can call the deleteLeave function if the close
			 * button is Clicked
			 **/
			if (leaveStatus == 3 || leaveStatus == 2) {

				view.setTag(deleteButton);
				deleteButton.setTag((Leave) (itemDets).get(position));
				/**
				 * Set up a listener for Click in the Cross button for delete
				 * Leave if Cancelled Flag is on
				 **/
				deleteButton.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						Log.e("Leave Delete Button Clicked",
								"Leave will be deleted now");

						ImageButton deletedButton = (ImageButton) v;
						Leave deleteLeaveForDialog = (Leave) deletedButton
								.getTag();

						deleteDialog = new Dialog(getApplicationContext());
						Button cancelDelete;

						/** To bring front the Dialog box **/
						deleteDialog = new Dialog(LeaveListActivity.this);
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
						cancelDelete.setOnClickListener(new OnClickListener() {

							public void onClick(View arg0) {
								deleteDialog.dismiss();

							}
						});
						deleteConfirm.setTag(deleteLeaveForDialog);
						deleteConfirm.setOnClickListener(new OnClickListener() {

							public void onClick(View v1) {
								Button but = (Button) v1;
								Leave deleteLeave = (Leave) but.getTag();
								Log.e("Deleted Leave Item Id:", ""
										+ deleteLeave.getLeaveId());
								leaveSQLite.deleteLeave(deleteLeave
										.getLeaveId());

								deleteDialog.dismiss();
								LeaveListActivity.this.finish();
								startActivity(new Intent(
										LeaveListActivity.this,
										LeaveListActivity.class));
							}
						});

					}
				});
			} else {
				deleteButton.setVisibility(View.INVISIBLE);
			}
			return view;
		}

		public int getCount() {

			return itemDets.size();
		}

		public Leave getItem(int arg0) {

			return itemDets.get(arg0);
		}

		public long getItemId(int position) {

			return position;
		}
	}

	public static ArrayList<Leave> getLeaveArrayList() {
		return itemDetails;
	}

}
