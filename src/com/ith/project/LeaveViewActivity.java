package com.ith.project;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.LeaveSQLite;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class LeaveViewActivity extends Activity implements OnClickListener {

	private final String leaveUrl = "SetLeaveStatus";
	private final String readUrl = "MarkLeaveRequestAsIsRead";

	private Dialog dialog;
	private ImageButton menuButton, homeButton;
	private ImageView leaveView, statusImage;
	private CallMenuDialog callDiag;
	private HashMap<String, String> menuItems;
	private JSONObject readLeaveInquiry, updateStatusInquiry;
	private EmployeeSQLite employeeSQLite;
	private LeaveSQLite leaveSQLite;
	private HttpConnection conn;
	private int position, empId, leaveId, respondSpinner, responseItem,
			leaveUpdateStatus;
	private boolean isNotificationSent, readStatusUpdated;
	private Spinner leaveReponseSpinner;
	private ArrayList<String> spinnerItems;
	private String isPending, leaveImageName;
	private TextView LeaveType, LeaveDate, EmployeeName, LeaveRemarks;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * pdialog = new ProgressDialog(this); pdialog.setCancelable(true);
		 * pdialog.setLeave("Loading ...."); pdialog.show();
		 */

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.leave_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();

	}

	@Override
	public void onPause() {
		super.onPause();
		/* pdialog.dismiss(); */
		if (employeeSQLite != null)
			employeeSQLite.closeDB();
		if (leaveSQLite != null)
			leaveSQLite.closeDB();
		if (dialog != null)
			dialog.dismiss();

		this.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		/* pdialog.dismiss(); */
	}

	private void init() {

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutLeaveView);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.leave_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		position = bundle.getInt("PositionOfLeave");

		employeeSQLite = new EmployeeSQLite(LeaveViewActivity.this);
		if (!employeeSQLite.isOpen())
			employeeSQLite.openDB();
		leaveSQLite = new LeaveSQLite(LeaveViewActivity.this);
		if (!leaveSQLite.isOpen())
			leaveSQLite.openDB();

		EmployeeName = (TextView) findViewById(R.id.textViewRespectiveName);
		String empName = employeeSQLite
				.getEmpName(LeaveListActivity.getLeaveArrayList().get(position)
						.getApplicantId() == LoginAuthentication.EmployeeId ? (LeaveListActivity
						.getLeaveArrayList().get(position).getApprovalId())
						: (LeaveListActivity.getLeaveArrayList().get(position)
								.getApplicantId()));
		EmployeeName.setText(empName);

		leaveView = (ImageView) findViewById(R.id.imageViewLeaveType);
		String imageName = LeaveListActivity.getLeaveArrayList().get(position)
				.getApplicantId() == LoginAuthentication.EmployeeId ? "upload"
				: "download";
		int id = getResources().getIdentifier(imageName, "drawable",
				this.getPackageName());
		leaveView.setImageResource(id);

		int leaveStatus = LeaveListActivity.getLeaveArrayList().get(position)
				.getLeaveStatus();

		LeaveType = (TextView) findViewById(R.id.editTextLeaveType);
		String leaveCause;
		int tempId = LeaveListActivity.getLeaveArrayList().get(position)
				.getLeaveTypeId();
		if (tempId == 1)
			leaveCause = "Sick";
		else if (tempId == 2)
			leaveCause = "Personal";
		else if (tempId == 3)
			leaveCause = "Family Function";
		else
			leaveCause = "Others";
		LeaveType.setText(leaveCause);

		LeaveDate = (TextView) findViewById(R.id.textViewStartLeaveDate);
		String leaveDateStr = new StringBuilder().append(
				LeaveListActivity.getLeaveArrayList().get(position)
						.getLeaveStartDate()).toString();
		LeaveDate.setText(leaveDateStr);

		LeaveRemarks = (TextView) findViewById(R.id.editTextLeaveRemarks);
		String leaveRemark = LeaveListActivity.getLeaveArrayList()
				.get(position).getLeaveRemarks();
		LeaveRemarks.setText(leaveRemark);

		LinearLayout leaveStatusLinLayout = (LinearLayout) findViewById(R.id.linearLayoutLeaveResponseStatus);
		leaveReponseSpinner = (Spinner) findViewById(R.id.spinnerLeaveResponse);

		if (LeaveListActivity.getLeaveArrayList().get(position)
				.getLeaveStatus() == 3) {
			responseItem = 2;
			leaveImageName = "thumbs_down";
		} else if (LeaveListActivity.getLeaveArrayList().get(position)
				.getLeaveStatus() == 2) {
			responseItem = 1;
			leaveImageName = "thumbs_up";
		} else {
			responseItem = 0;
			leaveImageName = "pending";
		}
		statusImage = (ImageView) findViewById(R.id.imageViewResponse);
		statusImage.setImageResource(getResources().getIdentifier(
				leaveImageName, "drawable", this.getPackageName()));

		if (LeaveListActivity.getLeaveArrayList().get(position)
				.getApplicantId() == LoginAuthentication.EmployeeId) {
			leaveReponseSpinner.setVisibility(View.GONE);
		} else {
			spinnerItems = new ArrayList<String>();
			spinnerItems.add("Pending");
			spinnerItems.add("Approved");
			spinnerItems.add("Declined");

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, spinnerItems);

			dataAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			leaveReponseSpinner.setAdapter(dataAdapter);
			leaveReponseSpinner.setSelection(responseItem);

			updateIsNotificationSent();
		}

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(LeaveViewActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(LeaveViewActivity.this);

		menuItems = new HashMap<String, String>();

		/** Check for spinner item change Listener **/
		leaveReponseSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						Log.e("Item at Spinner",
								"" + parent.getItemAtPosition(pos).toString());
						responseItem = pos;

						String leaveStats;
						if (responseItem == 1)
							leaveImageName = "thumbs_up";
						else if (responseItem == 2)
							leaveImageName = "thumbs_down";
						else
							leaveImageName = "pending";
						int imageId = getResources().getIdentifier(
								leaveImageName, "drawable",
								getApplicationContext().getPackageName());
						statusImage.setImageResource(imageId);

						/** Now sync only when read Status in main DB is true **/
						if (readStatusUpdated
								&& (LeaveListActivity.getLeaveArrayList()
										.get(position).getApprovalId() == LoginAuthentication.EmployeeId)) {
							updateResponseStatus(responseItem);
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {
						responseItem = 2;

					}
				});
	}

	/***********************************************************************************
	 * This is used to update the NotificationStatus once the approvalId has
	 * seen the leave application
	 * ***********************************************************************************/
	private void updateIsNotificationSent() {

		empId = LoginAuthentication.EmployeeId;
		leaveId = LeaveListActivity.getLeaveArrayList().get(position)
				.getLeaveId();

		isNotificationSent = LeaveListActivity.getLeaveArrayList()
				.get(position).getNotificationSentStatus();
		isPending = LeaveListActivity.getLeaveArrayList().get(position)
				.getLeaveType();

		Log.e("Let's see the isNotificationSent Flag",
				"isNotificationSent Flag is : " + isNotificationSent);

		readStatusUpdated = false;
		/**
		 * UPdate read Value Locally only if Leave is unread and it is also in
		 * sync with main Database
		 **/
		if ((!isNotificationSent && (isPending == null || isPending.equals("")))) {

			/** make Notification status in local sqlite as true **/
			leaveSQLite.updateLeaveNotificationSent(leaveId);
			// LeaveSQLite.closeDB();
			readLeaveInquiry = makeLeaveNotificationSentJson(empId, leaveId);
			// Log.e("readLeaveINquiry", "" + readLeaveInquiry.toString());
		}
		/**
		 * ApprovalStatus can be synced if isNotificationSent = true && Leave is
		 * in sync with main DB
		 **/
		else if ((isNotificationSent && (isPending == null || isPending
				.equals(""))))
			readStatusUpdated = true;

		Thread leaveNotificationSentUpdateThread = new Thread(new Runnable() {

			public void run() {
				conn = HttpConnection.getSingletonConn();
				Log.e("readLeaveInquiry", "" + readLeaveInquiry.toString());
				String readStatusStr = conn.getJSONFromUrl(readLeaveInquiry,
						readUrl);
				Log.e("Leave NotificationSent Status:", "" + readStatusStr);

				JSONObject readStatusJson;
				try {
					readStatusJson = new JSONObject(readStatusStr);
					boolean readUpdateStatus = (Boolean) readStatusJson
							.get("MarkLeaveRequestAsIsRead");
					if (readUpdateStatus) {

						Log.e("read status Updated",
								"Read Status Updated Succesfully");
						readStatusUpdated = true;

					} else {
						/**
						 * update the same leave in sqlite with
						 * LeaveType="readUpdatePending" if connection is
						 * refused
						 **/
						// leaveSQLite.openDB();
						leaveSQLite.updateLeaveNotificationSentPending(leaveId,
								"notificationSentPending");
						// leaveSQLite.closeDB();

						Log.e("Problem Sending leave ",
								"Pending Read leaves saved as draft"
										+ readUpdateStatus);
					}
				} catch (JSONException e) {
					/***
					 * Save the read leave in sqlite if connection return is
					 * *JPT*
					 **/
					// leaveSQLite.openDB();
					leaveSQLite.updateLeaveNotificationSentPending(leaveId,
							"notificationSentPending");
					// leaveSQLite.closeDB();

					Log.e("JSONException while Updating read Status",
							"" + e.getMessage());
					Log.e("Leaves Saved for next Update",
							"Will be UPdated next time");
					e.printStackTrace();
				}

				runOnUiThread(new Runnable() {

					public void run() {
						/* pdialog.show(); */
						// exitDialog.dismiss();
						/*
						 * leaveViewActivity.this.finish(); Intent intent = new
						 * Intent(leaveViewActivity.this,
						 * leaveListActivity.class);
						 * leaveViewActivity.this.startActivity(intent);
						 */

					}

				});
			}
		});
		if ((!isNotificationSent && (isPending == null || isPending.equals(""))))
			leaveNotificationSentUpdateThread.start();

	}

	/***********************************************************************************
	 * This is used to create the json object for querying IsNotificationSent to
	 * the web service
	 * ***********************************************************************************/
	public static JSONObject makeLeaveNotificationSentJson(int empId2,
			int leaveId2) {

		JSONObject readInquiry = new JSONObject();
		try {
			readInquiry.put("approvalEmployeeId",
					new StringBuilder().append(empId2).toString());
			readInquiry.put("leaveRequestId",
					new StringBuilder().append(leaveId2).toString());
			return readInquiry;
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/***********************************************************************************
	 * This is used to update the approval Status of the leave application
	 * ***********************************************************************************/
	protected void updateResponseStatus(int leaveStatus) {

		leaveUpdateStatus = leaveStatus + 1;

		if (((isPending == null || isPending.equals("")))) {
			leaveSQLite.updateLeaveStatus(leaveId, leaveUpdateStatus);
			// leaveSQLite.closeDB();
			int approvalEmpId = LeaveListActivity.getLeaveArrayList()
					.get(position).getApprovalId();
			updateStatusInquiry = makeLeaveStatusInquiryJson(leaveId,
					leaveUpdateStatus, approvalEmpId);
		}

		/** Thread for updating Going status **/
		Thread leaveStatusThread = new Thread(new Runnable() {

			public void run() {

				conn = HttpConnection.getSingletonConn();
				Log.e("leaveStatusInquiry", "" + updateStatusInquiry.toString());
				String updateStatusStr = conn.getJSONFromUrl(
						updateStatusInquiry, leaveUrl);
				Log.e("Leave approval Status:", "" + updateStatusStr);

				JSONObject updateStatusJson;
				try {
					updateStatusJson = new JSONObject(updateStatusStr);
					boolean leaveUpdateStatusBool = (Boolean) updateStatusJson
							.get("SetLeaveStatusResult");
					if (leaveUpdateStatusBool) {

						leaveSQLite.updateLeaveStatus(leaveId,
								leaveUpdateStatus);
						Log.e("leave Status Updated",
								"leave Status Updated Succesfully");

					} else {
						/**
						 * update the same leave in sqlite with
						 * LeaveType="leaveUpdatePending" if connection is
						 * refused
						 **/
						// leaveSQLite.openDB();
						leaveSQLite.updateLeaveStatusPending(leaveId,
								"approvalUpdatePending");
						// leaveSQLite.closeDB();

						Log.e("Problem Sending Update Leave Status ",
								"Pending Leave Status saved as draft"
										+ leaveUpdateStatusBool);
					}
				} catch (JSONException e) {
					/***
					 * Save the going leave in sqlite if connection return is
					 * *JPT*
					 **/
					// leaveSQLite.openDB();
					leaveSQLite.updateLeaveStatusPending(leaveId,
							"approvalUpdatePending");
					// leaveSQLite.closeDB();

					Log.e("JSONException while Updating Leave Status",
							"" + e.getMessage());
					Log.e("Leave Saved for next Update",
							"Leave status Will be UPdated next time");
					e.printStackTrace();
				}

			}
		});
		if ((isPending == null || isPending.equals("")))
			leaveStatusThread.start();
	}

	/***********************************************************************************
	 * This is used to make the json object for approval Status query to the web
	 * service
	 * ***********************************************************************************/
	public static JSONObject makeLeaveStatusInquiryJson(int leaveId,
			int leaveStatus, int approvalEmpId) {

		JSONObject readInquiry = new JSONObject();
		try {

			readInquiry.put("leaveRequestId",
					new StringBuilder().append(leaveId).toString());
			readInquiry.put("approvalEmployeeId",
					new StringBuilder().append(approvalEmpId).toString());
			readInquiry.put("userLoginId", LoginAuthentication.UserloginId);
			readInquiry.put("leaveStatus",
					new StringBuilder().append(leaveStatus).toString());
			/*
			 * readInquiry.put("GoingStatus", new
			 * StringBuilder().append(goingStatus).toString());
			 */
			return readInquiry;
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		}

	}
}
