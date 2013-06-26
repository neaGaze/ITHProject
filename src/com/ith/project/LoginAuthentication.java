package com.ith.project;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class LoginAuthentication {

	private JSONObject formDets, remoteJson;
	private String remoteStr;

	private static String UserloginId;
	private static int UserId;
	private static int EmployeeId;
	private static int UserRolesId;
	private volatile boolean AutheticationStatus = false;

	public LoginAuthentication() {

	}

	/*************************************************************************************
	 * Make a JSONObject out of username & pssword
	 * ***************************************************************************************/
	public JSONObject jsonFormValues(String username, String password) {
		JSONObject tempJsonFile = new JSONObject();
		try {
			tempJsonFile.put("Password", password);
			tempJsonFile.put("Username", username);
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", ":P :P :P");
			e.printStackTrace();
		}
		return tempJsonFile;
	}

	/***************************************************************************************
	 * set the EmployeeId
	 * ************************************************************************************/
	protected void setEmployeeId(int empId) {
		EmployeeId = empId;
	}

	/***************************************************************************************
	 * set the userId
	 * ************************************************************************************/
	protected void setUserId(int userId) {
		UserId = userId;
	}

	/***************************************************************************************
	 * set the userLoginId
	 * ************************************************************************************/
	protected void setUserLoginId(String userLoginId) {
		UserloginId = userLoginId;
	}

	/***************************************************************************************
	 * set the authenticationStatus
	 * ************************************************************************************/
	protected void setAuthStatus(boolean authStatus) {
		this.AutheticationStatus = authStatus;
	}

	/***************************************************************************************
	 * set the userRoleID
	 * ************************************************************************************/
	protected void setUserRoleId(int userRoleId) {
		UserRolesId = userRoleId;
	}

	public boolean getAuthStatus() {
		return this.AutheticationStatus;
	}

	public static int getUserId() {
		return UserId;
	}

	public static int getEmployeeId() {
		return EmployeeId;
	}

	public static String getUserLoginId() {
		return UserloginId;
	}

	public static int getUserRoleId() {
		return UserRolesId;
	}

	/******************************************************************************************
	 * To set the UserId, EmployeeId, UserLoginId
	 * ************************************************************************************/
	public void setValues() {
		try {

			setUserId(remoteJson.getInt("UserId"));
			setEmployeeId(remoteJson.getInt("EmployeeId"));
			setUserLoginId(remoteJson.getString("UserLoginId"));
			setUserRoleId(remoteJson.getInt("UserRolesId"));

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	/************************************************************************************
	 * set the Authentication Flag to true if login is succesful
	 * **************************************************************************************/
	public void setFlagFromAuth(JSONObject jsonObject) {
		remoteJson = jsonObject;
		try {

			if (jsonObject == null)
				setAuthStatus(false);
			else if ((Boolean) remoteJson.get("AutheticationStatus")) {
				setAuthStatus(true);
				Log.v("onPostExecute@auth", "" + remoteJson.toString());
			}

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}catch (NullPointerException e) {
			Log.e("NullPointerException", "" + e.getMessage());
			e.printStackTrace();
		}

		// Log.v("AutheticationStatus@onPostExecute is:", "" +
		// this.AutheticationStatus);

	}

}
