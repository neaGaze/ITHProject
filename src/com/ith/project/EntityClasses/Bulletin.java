package com.ith.project.EntityClasses;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Bulletin {

	private static int DAY_INTERVAL_BULLETINS = -7;
	
	private int BulletinId, EmployeeId;
	private String Title, Description, BulletinDate, EmployeeName;
	private String date;
	private String time;
	private String currDate, previousDate; // for retrieving latest few days'
											// Bulletins only

	public Bulletin() {

	}

	/*************************************************************************************
	 * Make a JSONObject out of UserLoginId
	 * ***************************************************************************************/
	public JSONObject getJsonUserLoginId(String UserLoginId) {

		JSONObject tempJsonFile = new JSONObject();
		this.setDaysInterval();
		try {
			tempJsonFile.put("userLoginId", UserLoginId);
			tempJsonFile.put("startDateTime", previousDate);
			tempJsonFile.put("endDateTime", currDate);
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}

		return tempJsonFile;
	}

	/*************************************************************************************
	 * Make a JSONObject of new Bulletin that admin adds
	 * ***************************************************************************************/
	public JSONObject makeNewBulletinJSON(String userId, String Title,
			String Desc) {

		JSONObject tempJsonFile = new JSONObject();

		try {
			// tempJsonFile.put("BulletinDate", date);
			tempJsonFile.put("Description", Desc);
			tempJsonFile.put("Title", Title);
			tempJsonFile.put("EmployeeId", Integer.parseInt(userId));

		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}

		return tempJsonFile;
	}

	public int getBulletinId() {
		return this.BulletinId;
	}

	public int getEmployeeId() {
		return this.EmployeeId;
	}

	public String getEmployeeName() {
		return this.EmployeeName;
	}

	public String getTitle() {
		return this.Title;
	}

	public String getDescription() {
		return this.Description;
	}

	public String getBulletinDate() {
		return this.BulletinDate;
	}

	public String getDate() {
		return this.date;
	}

	public String getTime() {
		return this.time;
	}

	/******************************************************************************************
	 * To set the values of Bulletin
	 * ************************************************************************************/
	public void setValues(JSONObject remoteJson) {
		try {

			BulletinId = remoteJson.getInt("BulletinId");
			EmployeeName = remoteJson.getString("EmployeeName");
			Title = remoteJson.getString("Title");
			Description = remoteJson.getString("Description");
			BulletinDate = remoteJson.getString("BulletinDate");
			parseDateTime(BulletinDate);
		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void setBulletinId(int i) {
		BulletinId = i;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public void setDesc(String desc) {
		Description = desc;
	}

	public void setDate(String date) {
		BulletinDate = date;
	}

	public void setEmpName(String empName) {
		EmployeeName = empName;
	}

	/***************************************************************************************
	 * To separate Date From time
	 * ***************************************************************************************/
	public void parseDateTime(String dateTime2) {

		this.date = new StringBuilder().append(dateTime2.substring(6, 8))
				.append("-").append(dateTime2.substring(4, 6)).append("-")
				.append(dateTime2.substring(0, 4)).toString();

		this.time = new StringBuilder().append(dateTime2.substring(9, 11))
				.append(":").append(dateTime2.substring(11, 13))/*
																 * .append(":")
																 * .
																 * append(dateTime2
																 * .
																 * substring(13)
																 * )
																 */.toString();

	}

	/***************************************************************************************
	 * To get the start and end Date of the Bulletins
	 * ***************************************************************************************/
	public void setDaysInterval() {
		/** Get current day **/
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		currDate = dtFormat.format(cal.getTime());
		Log.e("CurrDate", "" + currDate);

		/** Get date of 7 days from now **/
		cal.add(Calendar.DAY_OF_YEAR, DAY_INTERVAL_BULLETINS);
		previousDate = dtFormat.format(cal.getTime());
		Log.e("PreviousDate", "" + previousDate);
	}
}
