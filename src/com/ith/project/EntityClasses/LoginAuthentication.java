package com.ith.project.EntityClasses;

import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class LoginAuthentication {

	public static String UserloginId;
	public static int UserId;
	public static int EmployeeId;
	public static int UserRolesId;
	public static boolean AutheticationStatus = false;

	public LoginAuthentication() {

	}

	/******************************************************************************************
	 * To set the UserId, EmployeeId, UserLoginId
	 * ************************************************************************************/
	public void setValues(JSONObject remoteJson) {
		try {
			UserId = remoteJson.getInt("UserId");
			EmployeeId = remoteJson.getInt("EmployeeId");
			UserloginId = remoteJson.getString("UserLoginId");
			UserRolesId = remoteJson.getInt("UserRolesId");

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	/************************************************************************************
	 * set the Authentication Flag to true if login is succesful
	 * **************************************************************************************/
	public void setFlagFromAuth(JSONObject jsonObject) {
		JSONObject remoteJson = jsonObject;
		try {

			if (jsonObject == null)
				AutheticationStatus = false;
			else if ((Boolean) remoteJson.get("AutheticationStatus")) {
				AutheticationStatus = true;
				Log.v("onPostExecute@auth", "" + remoteJson.toString());
			} else if ((Boolean) remoteJson.get("AutheticationStatus") == false) {
				AutheticationStatus = false;
				Log.v("onPostExecute@auth", "" + remoteJson.toString());
			}

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e("NullPointerException", "" + e.getMessage());
			e.printStackTrace();
		}

	}

}
