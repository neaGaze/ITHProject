package com.ith.project.EntityClasses;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class Message {

	public static int DAY_INTERVAL_MESSAGES = -14;
	private int MessageId, MessageRealId, MessageFrom, MessageTo;
	private String MessageTitle, MessageDesc, MessageDateTime,
			MessageDate = null, MessageTime = null, MessageType;
	private static String previousDate, currDate;
	private boolean MessageRead;
	private static Calendar calendar1, calendar2;
	public static boolean isDefault;
	private boolean Checked = false;

	public Message() {

	}

	public static JSONObject getInquiryJson(String userLoginId, int msgTo,
			String startDate) {
		JSONObject tempJsonFile = new JSONObject();
		setDaysInterval();

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

	public void setMsgId(int Id) {
		this.MessageId = Id;
	}

	public void setMsgRealId(int id) {
		MessageRealId = id;
	}

	public void setMsgFrom(int msgFrom) {
		this.MessageFrom = msgFrom;
	}

	public void setMsgTo(int msgTo) {
		this.MessageTo = msgTo;
	}

	public void setMsgRead(int msgRead) {
		if (msgRead == 1)
			this.MessageRead = true;
		else
			this.MessageRead = false;
	}

	public void setMsgTitle(String msgTitle) {
		this.MessageTitle = msgTitle;
	}

	public void setMsgDesc(String msgDesc) {
		this.MessageDesc = msgDesc;
	}

	public void setMsgDate(String msgDateTime) {
		this.MessageDateTime = msgDateTime;
	}

	public void setMsgType(String msgType) {
		this.MessageType = msgType;
	}

	public void setChecked(boolean checked) {
		this.Checked = checked;
	}

	/******************************************************************************************
	 * To set the values of Employees
	 * ************************************************************************************/
	public void setValues(JSONObject remoteJson) {
		try {

			MessageId = remoteJson.getInt("MessageId");
			MessageRealId = remoteJson.getInt("Messageid");
			MessageTitle = remoteJson.getString("MessageTitle");
			MessageDesc = remoteJson.getString("MessageDesc");
			MessageDateTime = remoteJson.getString("MessageDate");
			MessageFrom = remoteJson.getInt("MessageFrom");
			MessageTo = remoteJson.getInt("MessageTo");
			MessageRead = remoteJson.getBoolean("MessageRead");
			MessageType = "web";
			parseDateTime(MessageDateTime);
		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	/***************************************************************************************
	 * To separate Date From time
	 * ***************************************************************************************/
	public void parseDateTime(String dateTime2) {

		// Log.e("dateTime equals", "here it is: " + dateTime2);

		if ((dateTime2 == null) || dateTime2.equals("")) {
			MessageDate = null;
			MessageTime = null;

		} else {
			this.MessageDate = new StringBuilder()
					.append(dateTime2.substring(6, 8)).append("-")
					.append(dateTime2.substring(4, 6)).append("-")
					.append(dateTime2.substring(0, 4)).toString();

			this.MessageTime = new StringBuilder()
					.append(dateTime2.substring(9, 11)).append(":")
					.append(dateTime2.substring(11, 13)).toString();
		}

	}

	public boolean getChecked() {
		return this.Checked;
	}

	public String getDate() {
		return this.MessageDate;
	}

	public String getTime() {
		return this.MessageTime;
	}

	public String getDateTime() {
		return this.MessageDateTime;
	}

	public String getMsgTitle() {
		return this.MessageTitle;
	}

	public String getMsgDesc() {
		return this.MessageDesc;
	}

	public int getMsgId() {
		return this.MessageId;
	}

	public int getMsgRealId() {
		return this.MessageRealId;
	}

	public int getMsgFrom() {
		return this.MessageFrom;
	}

	public int getMsgTo() {
		return this.MessageTo;
	}

	public boolean getMsgRead() {
		return this.MessageRead;
	}

	public String getMsgType() {
		return this.MessageType;
	}

	/*************************************************************************************
	 * Make a JSONObject of new Message that user fills
	 * ***************************************************************************************/
	public JSONObject makeNewMessageJSON(int msgFrom, Integer[] msgTo,
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

	/***************************************************************************************
	 * To get the start and end Date of the Messages
	 * ***************************************************************************************/
	public static void setDaysInterval() {
		if (currDate == null && previousDate == null)
			isDefault = true;
		if (isDefault) {

			/** Get current day **/
			calendar1 = Calendar.getInstance();
			SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmmss",
					Locale.US);
			currDate = dtFormat.format(calendar1.getTime());

			Log.e("CurrDate", "" + currDate);

			/** Get date of 14 days from now **/
			calendar2 = Calendar.getInstance();
			calendar2.add(Calendar.DAY_OF_YEAR, DAY_INTERVAL_MESSAGES);
			previousDate = dtFormat.format(calendar2.getTime());

			Log.e("PreviousDate", "" + previousDate);
		}
	}

	public static Calendar getFirstCalendar() {
		return calendar1;
	}

	public static Calendar getSecondCalendar() {
		return calendar2;
	}

	public static void setFirstCalendar(int year, int month, int day) {
		calendar1.set(year, month, day, 12, 00, 00);
	}

	public static void setSecondCalendar(int year, int month, int day) {
		calendar2.set(year, month, day, 12, 00, 00);
	}

	/***************************************************************************************
	 * To return the Json object of the selectedItemDetails
	 **************************************************************************************/
	public static JSONObject getDeleteQuery(
			ArrayList<Message> selectedItemDetails) {

		JSONObject deleteEmp = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		JSONArray tempJsonFile = new JSONArray();
		for (int i = 0; i < selectedItemDetails.size(); i++) {
			// if(selectedItemDetails.get(i).getMsgRealId() != null)
			tempJsonFile.put(selectedItemDetails.get(i).getMsgRealId());
			/**
			 * Can also insert empty msgRealId if that's only locally created,
			 * don't know how server deals with that. To test try to delete the
			 * Msg where date created == null and see result
			 **/
		}
		try {
			jsonObject.put("employeeId", LoginAuthentication.EmployeeId);
			jsonObject.put("messageId", tempJsonFile);
			deleteEmp.put("DeleteMessage", jsonObject);
		} catch (JSONException e) {
			Log.e("JSONEXception @ getDeleteMessages", "" + e.getMessage());
			e.printStackTrace();
		}
		return jsonObject;

	}
}
