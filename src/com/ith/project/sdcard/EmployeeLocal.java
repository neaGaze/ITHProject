package com.ith.project.sdcard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class EmployeeLocal extends LocalConnection {

	private String urlLocal = "/sdcard/EMS";
	private String usersFolder = "/Employees";
	private String employeesDetails = "/employees.json";

	private static int localEmployeeId;

	public EmployeeLocal() {
		localEmployeeId = 0;
	}

	/*****************************************************************************************
	 * read json data from the given URL
	 * ************************************************************************************/
	public JSONArray getJSONFromLocal(JSONObject jsonForm) {

		try {

			String fileData = getStringFromLocal(urlLocal + usersFolder
					+ employeesDetails);

			JSONArray localFileJSON = new JSONArray(fileData);

			JSONArray returnJsonArr = new JSONArray();

			for (int i = 0; i < localFileJSON.length(); i++) {
				JSONObject currentJson = localFileJSON.getJSONObject(i);
				JSONObject returnJsonObject = new JSONObject();
				returnJsonObject.put("EmployeeId",
						currentJson.getInt("EmployeeId"));
				returnJsonObject.put("EmployeeName",
						currentJson.getString("EmployeeName"));
				returnJsonObject.put("Gender", currentJson.getString("Gender"));
				returnJsonObject.put("HomePhone",
						currentJson.getString("HomePhone"));
				returnJsonObject.put("Mobile", currentJson.getString("Mobile"));
				returnJsonObject.put("Email", currentJson.getString("Email"));
				returnJsonObject.put("Address",
						currentJson.getString("Address"));
				returnJsonObject.put("Designation",
						currentJson.getString("Designation"));
				returnJsonObject.put("Remarks",
						currentJson.getString("Remarks"));

				returnJsonArr.put(returnJsonObject);
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
				+ employeesDetails);
	}

	/*****************************************************************************************
	 * update the Local File "employees.json" with manual fill of data
	 * ************************************************************************************/
	public void updateLocalFiles() {

		try {
			JSONArray jsonArr = new JSONArray();
			JSONObject jsonObj = new JSONObject();

			jsonObj.put("UserLoginId", "");
			jsonObj.put("EmployeeId", "1");
			jsonObj.put("EmployeeName", "Jason Bourne");
			jsonObj.put("Gender", "Male");
			jsonObj.put("HomePhone", "68979879");
			jsonObj.put("Mobile", "980454545");
			jsonObj.put("Email", "jasonbourne@unknown.com");
			jsonObj.put("Address", "Tokyo, Japan");
			jsonObj.put("Designation", "Kill the one that did this to me");
			jsonObj.put("Remarks",
					"Excellent fighting skill, good physique and extra smart");
			jsonArr.put(jsonObj);

			JSONObject newjsonObj = new JSONObject();

			newjsonObj.put("UserLoginId", "");
			newjsonObj.put("BulletinId", "2");
			newjsonObj.put("EmployeeName", "James Bond");
			newjsonObj.put("Gender", "Male");
			newjsonObj.put("HomePhone", "1179879");
			newjsonObj.put("Mobile", "984454545");
			newjsonObj.put("Email", "jamesbond@007.com");
			newjsonObj.put("Address", "London, UK");
			newjsonObj.put("Designation", "Save M");
			newjsonObj
					.put("Remarks",
							"High tech spy, good physique and uhm uhm ladies beware of him !!");
			jsonArr.put(newjsonObj);

			this.createOrUseFile(jsonArr.toString(), urlLocal + usersFolder
					+ employeesDetails);

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}


	/*****************************************************************************************
	 * update the Local File "employees.json" according to the value of webservice
	 * ************************************************************************************/
	public void updateLocalFiles(JSONObject inputJson, String employeesFromWS) {
		try {
			JSONObject employeesObj = new JSONObject(employeesFromWS);
			JSONArray employeesArr = employeesObj
					.getJSONArray("GetEmployeesResult");

			JSONArray jsonArr = new JSONArray();

			for (int i = 0; i < employeesArr.length(); i++) {
				JSONObject jsonObj = new JSONObject();

				jsonObj.put("UserLoginId", inputJson.getString("userLoginId"));
				// jsonObj.put("BulletinId",bulletinsArr.getJSONObject(i).getInt("BulletinId"));
				jsonObj.put("EmployeeId", localEmployeeId++);
				jsonObj.put("EmployeeName", employeesArr.getJSONObject(i)
						.getString("EmployeeName"));
				jsonObj.put("Gender",
						employeesArr.getJSONObject(i).getString("Gender"));
				jsonObj.put("HomePhone", employeesArr.getJSONObject(i)
						.getString("HomePhone"));			
				jsonObj.put("Mobile", employeesArr.getJSONObject(i)
						.getString("Mobile"));			
				jsonObj.put("Email", employeesArr.getJSONObject(i)
						.getString("Email"));			
				jsonObj.put("Address", employeesArr.getJSONObject(i)
						.getString("Address"));			
				jsonObj.put("Designation", employeesArr.getJSONObject(i)
						.getString("Designation"));			
				jsonObj.put("Remarks", employeesArr.getJSONObject(i)
						.getString("Remarks"));
				jsonArr.put(jsonObj);
			}

			this.createOrUseFile(jsonArr.toString(), urlLocal + usersFolder
					+ employeesDetails);

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		} 
	}
}
