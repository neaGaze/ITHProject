package com.ith.project.sqlite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Leave;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.sdcard.SQLQueryStore;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LeaveSQLite {

	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;
	private MsgEntryLogSQLite msgEntryLogSQLite;
	private SQLQueryStore sqlSave;

	public LeaveSQLite(Context context) {
		usersDBHelper = new UsersDBHelper(context);
		sqlSave = new SQLQueryStore();
	}

	/***********************************************************************************
	 * To instantiate SQLiteDatabase with writable mode
	 * ************************************************************************************/
	public void openDB() {
		db = usersDBHelper.getWritableDatabase();
	}

	/***********************************************************************************
	 * To close the DBHelper class
	 * ************************************************************************************/
	public void closeDB() {
		usersDBHelper.close();
	}

	/***********************************************************************************
	 * To check if the db is open or not
	 * ************************************************************************************/
	public boolean isOpen() {
		if (db == null)
			return false;
		else
			return db.isOpen();
	}

	/***********************************************************************************
	 * Update the Leave table from the json object through web service
	 * ************************************************************************************/
	public void updateLeaveTable(String leaveFromWS,
			MsgEntryLogSQLite msgEntryLogSQLite) {

		try {
			this.msgEntryLogSQLite = msgEntryLogSQLite;
			JSONObject leaveObj;
			leaveObj = new JSONObject(leaveFromWS);
			JSONArray leaveArr = leaveObj.getJSONArray("GetLeaveListResult");

			/** Calculate the current Date and Time **/
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmss", Locale.US);
			String currDate = dateFormat.format(cal.getTime());

			for (int i = 0; i < leaveArr.length(); i++) {

				/** To check for occurence of quotes ' in the String **/
				String unNormalizedRemark = leaveArr.getJSONObject(i)
						.getString("Remarks");
				StringBuilder normalizedRemark = new StringBuilder();
				String[] remarkParts = unNormalizedRemark.split("'");
				for (int j = 0; j < remarkParts.length; j++) {
					if (j == remarkParts.length - 1)
						normalizedRemark.append(remarkParts[j]);
					else
						normalizedRemark.append(remarkParts[j] + "''");
				}

				/** Query to check if the row already exists or not **/
				String checkQuery = "SELECT * FROM "
						+ UsersDBHelper.TABLE_LEAVE
						+ " WHERE "
						+ UsersDBHelper.LeaveRqId
						+ "="
						+ Integer.parseInt(leaveArr.getJSONObject(i).getString(
								"LeaveRequestId"));
				Cursor cursor = db.rawQuery(checkQuery, null);

				if (cursor.moveToFirst()) {
					/**
					 * i.e. there are rows with given LeaveRqId; so do update.
					 **/
					String updateQuery = "UPDATE "
							+ UsersDBHelper.TABLE_LEAVE
							+ " SET "
							+ UsersDBHelper.LeaveStatusId
							+ "="
							+ leaveArr.getJSONObject(i).getInt("LeaveStatusId")
							+ ", "
							+ UsersDBHelper.IsNotificationSent
							+ "="
							+ leaveArr.getJSONObject(i).getInt(
									"IsNotificationSent")
							+ ", "
							+ UsersDBHelper.LeaveType
							+ "='' "
							+ " WHERE "
							+ UsersDBHelper.LeaveRqId
							+ "="
							+ leaveArr.getJSONObject(i).getString(
									"LeaveRequestId");

					db.execSQL(updateQuery);

				} else {
					String insertQuery = "INSERT INTO "
							+ UsersDBHelper.TABLE_LEAVE
							+ " ( "
							+ UsersDBHelper.LeaveRqId
							+ ", "
							+ UsersDBHelper.ApplicantId
							+ ", "
							+ UsersDBHelper.ApprovalId
							+ ", "
							+ UsersDBHelper.LeaveTypeId
							+ ", "
							+ UsersDBHelper.LeaveStatusId
							+ ", "
							+ UsersDBHelper.Remark
							+ ", "
							+ UsersDBHelper.IsNotificationSent
							+ ", "
							+ UsersDBHelper.LeaveStartDate
							+ ", "
							+ UsersDBHelper.LeaveEndDate
							+ ") VALUES ("
							+ leaveArr.getJSONObject(i)
									.getInt("LeaveRequestId")
							+ ", "
							+ (leaveArr.getJSONObject(i).getInt(
									"ApplicantEmployeeId") == 0 ? LoginAuthentication.EmployeeId
									: (leaveArr.getJSONObject(i)
											.getInt("ApplicantEmployeeId")))
							+ ", "
							+ (leaveArr.getJSONObject(i).getInt(
									"ApprovalEmployeeId") == 0 ? LoginAuthentication.EmployeeId
									: (leaveArr.getJSONObject(i)
											.getInt("ApprovalEmployeeId")))
							+ ", "
							+ leaveArr.getJSONObject(i).getInt("LeaveTypeId")
							+ ", "
							+ leaveArr.getJSONObject(i).getInt("LeaveStatusId")
							+ ", '"
							+ normalizedRemark.toString()
							+ "', "
							+ leaveArr.getJSONObject(i).getString(
									"IsNotificationSent") + ", '"
							// +
							// leavesArr.getJSONObject(i).getInt("LeaveTo")
							+ leaveArr.getJSONObject(i).getString(
									"StringLeaveDateTime") + "', ''" + ")";

					// Log.v("UPDATE QUERY LEAVE", "" + updateQuery);
					db.execSQL(insertQuery);
				}
			}
			this.msgEntryLogSQLite.updateMsgEntryLog(currDate, "leaveLog");
		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}

	}

	/*****************************************************************************************
	 * read all Leave rows and populate Arraylist of Leave
	 * ************************************************************************************/
	public ArrayList<Leave> getLeaveFromDB() {

		ArrayList<Leave> tempArrList = new ArrayList<Leave>();

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_LEAVE
				+ " WHERE " + UsersDBHelper.ApplicantId + "="
				+ LoginAuthentication.EmployeeId + " OR "
				+ UsersDBHelper.ApprovalId + "="
				+ LoginAuthentication.EmployeeId;

		Log.v("SELECT QUERY LEAVE", "" + readQuery);
		Cursor cursor = db.rawQuery(readQuery, null);

		sqlSave.writeFile2Sdcard(readQuery);
		Log.v("CURSOR LEAVE SIZE:", "" + cursor.getCount());

		/**
		 * If the cursor is able to move to First, at least 1 value is present
		 **/
		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				Leave tempLeave = new Leave();

				tempLeave.setLeaveId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.LeaveRqId)));
				tempLeave.setApplicantId((cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.ApplicantId))));
				tempLeave.setApprovalId((cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.ApprovalId))));
				tempLeave.setLeaveTypeId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.LeaveTypeId)));
				tempLeave.setLeaveStatus(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.LeaveStatusId)));
				tempLeave.setLeaveStartDate(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.LeaveStartDate)));
				tempLeave.setRemarks(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Remark)));
				tempLeave.setIsNotificationSent(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.IsNotificationSent)));
				tempLeave.setLeaveType(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.LeaveType)));

				tempLeave.parseDateTime(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.LeaveStartDate)));
				// }
				tempArrList.add(tempLeave);

				cursor.moveToNext();
			}

			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND at Leave@getLeaveFromDB", "Cursor Size is 0");
		}
		cursor.close();
		return null;

	}

	/***********************************************************************************
	 * Delete the Leave because it's already updated to the main DB
	 * ************************************************************************************/
	public void deleteLeave(int leaveId) {

		String deleteQuery = "DELETE FROM " + UsersDBHelper.TABLE_LEAVE
				+ " WHERE " + UsersDBHelper.LeaveRqId + "=" + leaveId;

		Log.e("DELETE Query is", "" + deleteQuery);
		db.execSQL(deleteQuery);

	}

	/***********************************************************************************
	 * Save the leave draft in the sqlite
	 * ************************************************************************************/
	public void saveLeaveDraft(int applicantId, int approvalId,
			int leaveSpinner, String leaveDateTimeStr, String leaveRemarkStr,
			String pending) {

		String updateQuery = "INSERT OR REPLACE INTO "
				+ UsersDBHelper.TABLE_LEAVE + " ( " + UsersDBHelper.ApplicantId
				+ ", " + UsersDBHelper.ApprovalId + ", "
				+ UsersDBHelper.LeaveTypeId + ", "
				+ UsersDBHelper.LeaveStatusId + ", " + UsersDBHelper.Remark
				+ ", " + UsersDBHelper.IsNotificationSent + ", "
				+ UsersDBHelper.LeaveStartDate + ", "
				+ UsersDBHelper.LeaveEndDate + ", " + UsersDBHelper.LeaveType
				+ ") VALUES (" + applicantId + ", " + approvalId + ", "
				+ leaveSpinner + ", " + 3 + ", '" + leaveRemarkStr + "', " + 0
				+ ", '" + leaveDateTimeStr + "', '', '" + pending + "')";

		db.execSQL(updateQuery);

		sqlSave.writeFile2Sdcard(updateQuery);

	}

	/***********************************************************************************
	 * Update the pending status in the given leaveId as 'Read'
	 * ************************************************************************************/
	public void updateLeaveNotificationSent(int leaveId) {

		String updateReadQuery = "UPDATE " + UsersDBHelper.TABLE_LEAVE
				+ " SET " + UsersDBHelper.IsNotificationSent + "=" + 1
				+ " WHERE " + UsersDBHelper.LeaveRqId + "=" + leaveId;
		// UPDATE LEAVE SET IsNotificationSent=1 WHERE LeaveRqId=2;
		db.execSQL(updateReadQuery);

		sqlSave.writeFile2Sdcard(updateReadQuery);

	}

	/***********************************************************************************
	 * Update the Leave Notification of the given leaveId for pending
	 * ************************************************************************************/
	public void updateLeaveNotificationSentPending(int leaveId,
			String pendingStatus) {

		String updateReadQuery = "UPDATE " + UsersDBHelper.TABLE_LEAVE
				+ " SET " + UsersDBHelper.LeaveType + "='" + pendingStatus
				+ "' WHERE " + UsersDBHelper.LeaveRqId + "=" + leaveId;
		// UPDATE LEAVE SET LeaveType='notificationSentPending' WHERE
		// LeaveRqId=2;
		db.execSQL(updateReadQuery);

		sqlSave.writeFile2Sdcard(updateReadQuery);
	}

	/***********************************************************************************
	 * Update the Leave Status of the given leaveId
	 * ************************************************************************************/
	public void updateLeaveStatus(int leaveId, int leaveUpdateStatus) {

		String updateGoingStatus = "UPDATE " + UsersDBHelper.TABLE_LEAVE
				+ " SET " + UsersDBHelper.LeaveStatusId + "="
				+ leaveUpdateStatus + " WHERE " + UsersDBHelper.LeaveRqId + "="
				+ leaveId;

		Log.e("UPDATE LEAVE STATUS", "" + updateGoingStatus);
		db.execSQL(updateGoingStatus);

		sqlSave.writeFile2Sdcard(updateGoingStatus);
	}

	/***********************************************************************************
	 * Update the pending status in the given leaveId
	 * ************************************************************************************/
	public void updateLeaveStatusPending(int leaveId, String pendingStatus) {

		String updateReadQuery = "UPDATE " + UsersDBHelper.TABLE_LEAVE
				+ " SET " + UsersDBHelper.LeaveType + "='" + pendingStatus
				+ "' WHERE " + UsersDBHelper.LeaveRqId + "=" + leaveId;
		// UPDATE LEAVE SET LeaveType='leaveUpdatePending' WHERE LeaveRqId=2;
		db.execSQL(updateReadQuery);

		sqlSave.writeFile2Sdcard(updateReadQuery);
	}

	/***********************************************************************************
	 * Select the pending Leave of currently logged in employee
	 * ************************************************************************************/
	public ArrayList<Leave> selectPendingLeave(String pendingLeave) {

		String selectPendingQuery = "SELECT * FROM "
				+ UsersDBHelper.TABLE_LEAVE + " WHERE "
				+ UsersDBHelper.LeaveType + "='" + pendingLeave + "'" /*
																	 * + " AND "
																	 * +
																	 * UsersDBHelper
																	 * .
																	 * ApplicantId
																	 * + "=" +
																	 * employeeId
																	 */;

		Log.e("GET PENDING LEAVE", "" + selectPendingQuery);

		sqlSave.writeFile2Sdcard(selectPendingQuery);

		Cursor cursor = db.rawQuery(selectPendingQuery, null);
		Log.v("CURSOR PENDING LEAVE SIZE:", "" + cursor.getCount());

		ArrayList<Leave> tempArrList = new ArrayList<Leave>();

		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				Leave tempLeave = new Leave();

				tempLeave.setLeaveId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.LeaveRqId)));
				tempLeave.setApplicantId((cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.ApplicantId))));
				tempLeave.setApprovalId((cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.ApprovalId))));
				tempLeave.setLeaveTypeId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.LeaveTypeId)));
				tempLeave.setLeaveStatus(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.LeaveStatusId)));
				tempLeave.setLeaveStartDate(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.LeaveStartDate)));
				tempLeave.setRemarks(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Remark)));
				tempLeave.setIsNotificationSent(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.IsNotificationSent)));
				tempLeave.setLeaveType(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.LeaveType)));

				tempArrList.add(tempLeave);

				cursor.moveToNext();
			}

			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND for Pending leave",
					"Pending Leave Cursor Size = 0");
		}
		cursor.close();
		return null;
	}

	/*****************************************************************************************
	 * read the Leave of Viewed Item
	 * ************************************************************************************/
	public Leave getViewedLeave(int leaveId) {

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_LEAVE
				+ " WHERE " + UsersDBHelper.LeaveRqId + "=" + leaveId
				+ " ORDER BY " + UsersDBHelper.LeaveRqId + " DESC";

		Cursor cursor = db.rawQuery(readQuery, null);
		Log.v("CURSOR Viewed Leave SIZE:", "" + cursor.getCount());

		/**
		 * If the cursor is able to move to First, at least 1 value is present
		 **/
		if (cursor.moveToFirst()) {

			Leave tempLeave = new Leave();
			while (!cursor.isAfterLast()) {

				tempLeave.setLeaveId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.LeaveRqId)));
				tempLeave.setApplicantId((cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.ApplicantId))));
				tempLeave.setApprovalId((cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.ApprovalId))));
				tempLeave.setLeaveTypeId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.LeaveTypeId)));
				tempLeave.setLeaveStatus(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.LeaveStatusId)));
				tempLeave.setLeaveStartDate(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.LeaveStartDate)));
				tempLeave.setRemarks(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Remark)));
				tempLeave.setIsNotificationSent(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.IsNotificationSent)));
				tempLeave.setLeaveType(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.LeaveType)));

				cursor.moveToNext();
			}

			cursor.close();
			return tempLeave;
		}
		return null;
	}

}
