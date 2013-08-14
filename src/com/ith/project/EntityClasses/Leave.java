package com.ith.project.EntityClasses;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Leave {

	/**
	 * leaveType: 1 = Sick, 2 = Personal, 3 = Family Function, 4 = Others
	 * leaveStatus: 1 = Pending, 2 = Approved, 3 = Declined
	 * **/
	private int leaveId, applicantId, approvalId, leaveTypeId, leaveStatus;
	private String remarks, leaveStartDate, leaveEndDate, formattedStartDate,
			leaveType, applicantName, approvalName;
	private boolean isNotificationSent;

	public Leave() {

	}

	public int getLeaveId() {
		return leaveId;
	}

	public int getApplicantId() {
		return applicantId;
	}

	public int getApprovalId() {
		return approvalId;
	}

	public int getLeaveTypeId() {
		return leaveTypeId;
	}

	public int getLeaveStatus() {
		return leaveStatus;
	}

	public String getLeaveRemarks() {
		return remarks;
	}

	public String getLeaveStartDate() {
		return leaveStartDate;
	}

	public String getFormattedDate() {
		return formattedStartDate;
	}

	public String getLeaveEndDate() {
		return leaveEndDate;
	}

	public boolean getNotificationSentStatus() {
		return isNotificationSent;
	}

	public String getLeaveType() {
		return leaveType;
	}

	public String getApplicantName() {
		return applicantName;
	}

	public String getApprovalName() {
		return approvalName;
	}

	public void setValues(JSONObject remoteJson) {

		try {

			leaveId = remoteJson.getInt("LeaveRequestId");
			applicantId = remoteJson.getInt("ApplicantEmployeeId");
			approvalId = remoteJson.getInt("ApprovalEmployeeId");
			leaveTypeId = remoteJson.getInt("LeaveTypeId");
			leaveStatus = remoteJson.getInt("LeaveStatusId");
			remarks = remoteJson.getString("Remarks");
			leaveStartDate = remoteJson.getString("StringLeaveDateTime");
			leaveEndDate = "";
			isNotificationSent = remoteJson.getInt("IsNotificationSent") == 1 ? true
					: false;
			formattedStartDate = parseDateTime(leaveStartDate);
			// formattedEndDate = parseDateTime(leaveEndDate);

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}

	}

	/***************************************************************************************
	 * To separate Date From time
	 * ***************************************************************************************/
	public String parseDateTime(String dateTime2) {

		Log.e("dateTime equals", "here it is: " + dateTime2);
		String leaveDate;

		if ((dateTime2 == null) || dateTime2.equals("")) {
			leaveDate = null;

		} else {
			leaveDate = new StringBuilder().append(dateTime2.substring(6, 8))
					.append("-").append(dateTime2.substring(4, 6)).append("-")
					.append(dateTime2.substring(0, 4)).toString();
		}
		return leaveDate;
	}

	public void setLeaveId(int leaveId) {
		this.leaveId = leaveId;
	}

	public void setApplicantId(int applicantId) {
		this.applicantId = applicantId;
	}

	public void setApprovalId(int approvalId) {
		this.approvalId = approvalId;
	}

	public void setLeaveTypeId(int leaveType) {
		this.leaveTypeId = leaveType;
	}

	public void setLeaveStatus(int leaveStatus) {
		this.leaveStatus = leaveStatus;
	}

	public void setLeaveStartDate(String leaveStartDate) {
		this.leaveStartDate = leaveStartDate;
		formattedStartDate = parseDateTime(leaveStartDate);
	}

	public void setLeaveEndDate(String leaveEndDate) {
		this.leaveEndDate = leaveEndDate;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public void setIsNotificationSent(int isNotificationSent) {
		this.isNotificationSent = isNotificationSent == 1 ? true : false;
	}

	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}

	public void setApprovalName(String approvalName) {
		this.approvalName = approvalName;
	}

	public void setLeaveType(String leaveType) {
		this.leaveType = leaveType;
	}

	/**
	 * To make the json Object out of 
	 * **/
	public static JSONObject makeNewLeaveJSON(int applicantId2,
			int approvalId2, int leaveSpinner, String leaveDateTimeStr,
			String leaveRemarkStr) {

		JSONObject tempJsonFile = new JSONObject();
		try {

			tempJsonFile.put("applicantEmployeeId",
					new StringBuilder().append(applicantId2));
			tempJsonFile.put("toEmployeeId",
					new StringBuilder().append(approvalId2));
			tempJsonFile.put("leaveTypeId",
					new StringBuilder().append(leaveSpinner));
			tempJsonFile.put("leaveDate", leaveDateTimeStr);
			tempJsonFile.put("remark", leaveRemarkStr);
			tempJsonFile.put("userLoginId", LoginAuthentication.UserloginId);

		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return tempJsonFile;

	}

}
