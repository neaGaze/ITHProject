package com.ith.project.sdcard;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("SdCardPath")
public class LoginLocal extends LocalConnection {

	private String urlLocal = "/sdcard/EMS";
	private String usersFolder = "/users";
	private String loginCredentials = "/username.json";
	private String saltKey = "Z077O88OTILW50ENE03Y";

	private JSONObject tempJsonFile;

	public LoginLocal() {
		tempJsonFile = new JSONObject();
	}

	public JSONObject createJSON4LoginLocal(String username, String password,
			String UserLoginId, int EmployeeId, int UserId, int UserRolesId) {

		try {
			String cipheredPwd = encrypt(password);

			tempJsonFile.put("Password", cipheredPwd);
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
		} catch (NoSuchAlgorithmException e) {
			Log.e("NoSuchAlgorithmException @ updateLocalFiles",
					"" + e.getMessage());
			e.printStackTrace();
		}
		return tempJsonFile;
	}

	public void writeFile2Sdcard(JSONObject thisJsonFile) {
		this.createOrUseFile(thisJsonFile.toString(), urlLocal + usersFolder,
				loginCredentials);
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

			// if (!(new File(fileData)).exists())
			// return null;
			String cipheredPwd = encrypt(jsonForm.getString("Password"));
			JSONObject localFileJSON;

			localFileJSON = new JSONObject(fileData);
			JSONObject returnJsonObject = new JSONObject();
			Log.v("Username1:Username2", "" + jsonForm.getString("Username")
					+ ":" + localFileJSON.getString("Username"));

			if ((jsonForm.getString("Username").equals(localFileJSON
					.getString("Username")))
					&& (cipheredPwd.equals(localFileJSON.getString("Password")))) {

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
		} catch (NoSuchAlgorithmException e) {
			Log.e("NoSuchAlgorithmException @ updateLocalFiles",
					"" + e.getMessage());
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
			Log.v("Web JSON", "" + jsonFromWebservice);
			loginCredentials = new StringBuilder().append("/")
					.append(jsonForWebservice.get("Username")).append(".json")
					.toString();

			String cipheredPwd = encrypt(jsonForWebservice
					.getString("Password"));

			JSONObject sdcardJson = new JSONObject();
			sdcardJson.put("UserId", jsonFromWebservice.getInt("UserId"));
			sdcardJson.put("Username", jsonForWebservice.getString("Username"));
			sdcardJson.put("Password", cipheredPwd);
			sdcardJson.put("UserLoginId",
					jsonFromWebservice.getString("UserLoginId"));
			sdcardJson.put("EmployeeId",
					jsonFromWebservice.getInt("EmployeeId"));
			sdcardJson.put("UserRolesId",
					jsonFromWebservice.getInt("UserRolesId"));

			this.createOrUseFile(sdcardJson.toString(), urlLocal + usersFolder,
					loginCredentials);

		} catch (JSONException e) {
			Log.e("JSONException @ updateLocalFiles", "" + e.getMessage());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {

			Log.e("NoSuchAlgorithmException @ updateLocalFiles",
					"" + e.getMessage());
			e.printStackTrace();
		}
	}

	/************************************************************************************
	 * Encrypt Password using SHA-512 Managed Encryption and salt Key
	 * *************************************************************************************/
	public String encrypt(String pwd) throws NoSuchAlgorithmException {

		String encrypted = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(pwd.getBytes());
			byte[] mb = md.digest();

			Log.v("Direct encryption is of " + pwd + " is:", "" + mb.toString()
					+ " : " + mb.length);
			for (int i = 0; i < mb.length; i++) {
				byte temp = mb[i];
				String tempStr = Integer.toHexString(temp);
				encrypted += tempStr;
			}
			encrypted += saltKey;
			Log.v("Encrypted string of " + pwd + " is:", "" + encrypted);

		} catch (NoSuchAlgorithmException e) {
			Log.e("NoSuchAlgorithmException", "" + e.getMessage());
			e.printStackTrace();
		}
		return encrypted;
	}
}
