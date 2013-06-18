package com.ith.project.sdcard;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LoginLocal extends LocalConnection {

	private String urlLocal = "/sdcard/EMS";
	private String usersFolder = "/users";
	private String loginCredentials = "/username.json";

	private JSONObject tempJsonFile;

	public LoginLocal() {
		tempJsonFile = new JSONObject();
	}

	public JSONObject createJSON4LoginLocal(String username, String password,
			String UserLoginId, int EmployeeId, int UserId, int UserRolesId) {

		try {
			tempJsonFile.put("Password", password);
			tempJsonFile.put("Username", username);
			tempJsonFile.put("UserLoginId", UserLoginId);
			tempJsonFile.put("EmployeeId", EmployeeId);
			tempJsonFile.put("UserId", UserId);
			tempJsonFile.put("UserRolesId", UserRolesId);

			loginCredentials = new StringBuilder().append("/").append(username)
					.append(".json").toString();

		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", ":P :P :P");
			e.printStackTrace();
		}
		return tempJsonFile;
	}

	public void writeFile2Sdcard(JSONObject thisJsonFile) {
		this.createOrUseFile(thisJsonFile.toString(), urlLocal + usersFolder
				+ loginCredentials);
	}

	/*****************************************************************************************
	 * read json data from the given URL
	 * ************************************************************************************/
	public JSONObject getJSONFromLocal(JSONObject jsonForm) {

		try {
			loginCredentials = new StringBuilder().append("/")
					.append(jsonForm.get("Username")).append(".json")
					.toString();

			String fileData = getStringFromLocal(urlLocal + usersFolder
					+ loginCredentials);
			JSONObject localFileJSON;

			localFileJSON = new JSONObject(fileData);
			JSONObject returnJsonObject = new JSONObject();
			Log.v("Username1:Username2", "" + jsonForm.getString("Username")
					+ ":" + localFileJSON.getString("Username"));
			if ((jsonForm.getString("Username").equals(localFileJSON
					.getString("Username")))
					&& (jsonForm.getString("Password").equals(localFileJSON
							.getString("Password")))) {

				returnJsonObject.put("UserId", localFileJSON.getInt("UserId"));
				returnJsonObject.put("UserLoginId",
						localFileJSON.getString("UserLoginId"));
				returnJsonObject.put("UserRolesId",
						localFileJSON.getInt("UserRolesId"));
				returnJsonObject.put("EmployeeId",
						localFileJSON.getInt("EmployeeId"));
				returnJsonObject.put("AutheticationStatus", true);
			} else {
				returnJsonObject.put("UserId", 0);
				returnJsonObject.put("UserLoginId", "null");
				returnJsonObject.put("UserRolesId", 0);
				returnJsonObject.put("EmployeeId", 0);
				returnJsonObject.put("AutheticationStatus", false);
			}

			return returnJsonObject;

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	/************************************************************************************
	 * Update the login credentials
	 * *************************************************************************************/
	public void updateLocalFiles(JSONObject jsonFromWebservice,
			JSONObject jsonForWebservice) {

		try {
			Log.v("Username from webservice",
					"" + jsonForWebservice.get("Username"));
			loginCredentials = new StringBuilder().append("/")
					.append(jsonForWebservice.get("Username")).append(".json")
					.toString();
			JSONObject sdcardJson = new JSONObject();
			sdcardJson.put("UserId", jsonFromWebservice.getInt("UserId"));
			sdcardJson.put("Username", jsonForWebservice.getString("Username"));
			sdcardJson.put("Password", jsonForWebservice.getString("Password"));
			sdcardJson.put("UserLoginId",
					jsonFromWebservice.getString("UserLoginId"));
			sdcardJson.put("EmployeeId",
					jsonFromWebservice.getInt("EmployeeId"));
			sdcardJson.put("UserRolesId",
					jsonFromWebservice.getInt("UserRolesId"));

			this.createOrUseFile(sdcardJson.toString(), urlLocal + usersFolder
					+ loginCredentials);
		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}
}
