package com.ith.project.sdcard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class BulletinLocal extends LocalConnection {

	private String urlLocal = "/sdcard/EMS";
	private String usersFolder = "/Bulletins";
	private String bulletinsDetails = "/bulletins.json";

	private JSONObject tempJsonFile;
	private static int localBulletinId;

	public BulletinLocal() {
		tempJsonFile = new JSONObject();
		localBulletinId = 0;
	}

	public JSONObject createJSON4BulletinLocal() {
		return null;

	}

	/*****************************************************************************************
	 * read json data from the given URL
	 * ************************************************************************************/
	public JSONArray getJSONFromLocal(JSONObject jsonForm) {

		try {

			String fileData = getStringFromLocal(urlLocal + usersFolder
					+ bulletinsDetails);

			JSONArray localFileJSON = new JSONArray(fileData);

			JSONArray returnJsonArr = new JSONArray();

			if (true) {
				for (int i = 0; i < localFileJSON.length(); i++) {
					JSONObject currentJson = localFileJSON.getJSONObject(i);
					JSONObject returnJsonObject = new JSONObject();
					returnJsonObject.put("BulletinId",
							currentJson.getInt("BulletinId"));
					returnJsonObject.put("EmployeeName",
							currentJson.getString("EmployeeName"));
					returnJsonObject.put("Title",
							currentJson.getString("Title"));
					returnJsonObject.put("Description",
							currentJson.getString("Description"));
					returnJsonObject.put("BulletinDate",
							currentJson.getString("BulletinDate"));

					returnJsonArr.put(returnJsonObject);
				}
			}

			return returnJsonArr;

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	/*****************************************************************************************
	 * write files to sdcard
	 * ************************************************************************************/
	public void writeFile2Sdcard(JSONObject thisJsonFile) {
		this.createOrUseFile(thisJsonFile.toString(), urlLocal + usersFolder
				, bulletinsDetails);
	}

	/*****************************************************************************************
	 * update the Local File "bulletins.json" with manuall fill of data
	 * ************************************************************************************/
	public void updateLocalFiles() {

		try {
			JSONArray jsonArr = new JSONArray();
			JSONObject jsonObj = new JSONObject();

			jsonObj.put("UserLoginId", "");
			jsonObj.put("BulletinId", "1");
			jsonObj.put("EmployeeName", "Jason Bourne");
			jsonObj.put("Title", "New Project");
			jsonObj.put("Description", "This is the new project");
			jsonObj.put("BulletinDate", "");
			jsonArr.put(jsonObj);

			JSONObject newjsonObj = new JSONObject();

			newjsonObj.put("UserLoginId", "");
			newjsonObj.put("BulletinId", "2");
			newjsonObj.put("EmployeeName", "James Bond");
			newjsonObj.put("Title", "Day Off");
			newjsonObj.put("Description", "Let's Picnic");
			newjsonObj.put("BulletinDate", "");
			jsonArr.put(newjsonObj);

			this.createOrUseFile(jsonArr.toString(), urlLocal + usersFolder
					, bulletinsDetails);

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	/*****************************************************************************************
	 * update the Local File "bulletins.json" acc to the value of webservice
	 * ************************************************************************************/
	public void updateLocalFiles(JSONObject inputJson, String bulletinsFromWS) {
		try {
			JSONObject bulletinsObj = new JSONObject(bulletinsFromWS);
			JSONArray bulletinsArr = bulletinsObj
					.getJSONArray("GetBulletinsResult");

			JSONArray jsonArr = new JSONArray();

			for (int i = 0; i < bulletinsArr.length(); i++) {
				JSONObject jsonObj = new JSONObject();

				jsonObj.put("UserLoginId", inputJson.getString("userLoginId"));
				// jsonObj.put("BulletinId",bulletinsArr.getJSONObject(i).getInt("BulletinId"));
				jsonObj.put("BulletinId", localBulletinId++);
				jsonObj.put("EmployeeName", bulletinsArr.getJSONObject(i)
						.getString("EmployeeName"));
				jsonObj.put("Title",
						bulletinsArr.getJSONObject(i).getString("Title"));
				jsonObj.put("Description", bulletinsArr.getJSONObject(i)
						.getString("Description"));			
				jsonObj.put("BulletinDate", bulletinsArr.getJSONObject(i)
						.getString("BulletinDate"));
				jsonArr.put(jsonObj);
			}

			this.createOrUseFile(jsonArr.toString(), urlLocal + usersFolder
					, bulletinsDetails);

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		} 
	}
}
