package com.ith.project;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Bulletin {

	private int BulletinId, EmployeeId;
	private String Title, Description, BulletinDate,EmployeeName;
	private String date;
	private String time;
	
	public Bulletin() {

	}

	/*************************************************************************************
	 * Make a JSONObject out of UserLoginId
	 * ***************************************************************************************/
	public JSONObject getJsonUserLoginId(String UserLoginId) {

		JSONObject tempJsonFile = new JSONObject();

		try {
			tempJsonFile.put("userLoginId", UserLoginId);

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
			//tempJsonFile.put("BulletinDate", date);
			tempJsonFile.put("Description", Desc);
			tempJsonFile.put("Title", Title);
			tempJsonFile.put("UserId",userId);

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
	
	/***************************************************************************************
	 * To separate Date From time
	 * ***************************************************************************************/
	private void parseDateTime(String dateTime2) {

		this.date = new StringBuilder().append(dateTime2.substring(6, 8))
				.append("-").append(dateTime2.substring(4, 6)).append("-")
				.append(dateTime2.substring(0, 4)).toString();

		this.time = new StringBuilder().append(dateTime2.substring(9, 11))
				.append(":").append(dateTime2.substring(11, 13))/*.append(":")
				.append(dateTime2.substring(13))*/.toString();

	}
}
