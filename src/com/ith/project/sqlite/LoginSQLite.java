package com.ith.project.sqlite;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LoginSQLite {

	private String saltKey = "Z077O88OTILW50ENE03Y";

	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;

	public LoginSQLite(Context context) {
		// super(context, new UsersDBHelper(context));
		usersDBHelper = new UsersDBHelper(context);
	}

	/***********************************************************************************
	 * To instantiate SQLiteDatabase with writable mode
	 * ************************************************************************************/
	public void openDB() {
		db = usersDBHelper.getWritableDatabase();
		// db.execSQL("PRAGMA foreign_keys = ON;");
	}

	/***********************************************************************************
	 * To close the DBHelper class
	 * ************************************************************************************/
	public void closeDB() {
		usersDBHelper.close();
	}

	/************************************************************************************
	 * Insert the login and user data into sqlite according to values
	 * *************************************************************************************/
	public void insertDBUsersTableValues(String username, String password,
			String UserLoginId, int EmployeeId, int UserId, int UserRolesId) {

		try {

			String cipheredPwd = encrypt(password);

			String insertQuery = "INSERT OR REPLACE INTO "
					+ UsersDBHelper.TABLE_USERS + " ( " + UsersDBHelper.UserId
					+ ", " + UsersDBHelper.Username + ", "
					+ UsersDBHelper.Password + ", " + UsersDBHelper.UserLoginId
					+ ", " + UsersDBHelper.UserRolesId + ", "
					+ UsersDBHelper.EmployeeId + " ) VALUES(" + UserId + ", '"
					+ username + "', '" + cipheredPwd + "', '" + UserLoginId
					+ "', " + UserRolesId + ", " + EmployeeId + " )";

			// Log.v("INSERT QUERY", "" + insertQuery);
			db.execSQL(insertQuery);

		} catch (NoSuchAlgorithmException e) {
			Log.e("ALGORITHMEXCEPTION", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	/************************************************************************************
	 * Update the login credentials according to JSON object
	 * *************************************************************************************/
	public void updateDBUsersTableJson(JSONObject otherAttr,
			JSONObject userNamePwd) {

		try {

			String cipheredPwd = encrypt(userNamePwd.getString("Password"));

			String updateQuery = "INSERT OR REPLACE INTO "
					+ UsersDBHelper.TABLE_USERS + " ( " + UsersDBHelper.UserId
					+ ", " + UsersDBHelper.Username + ", "
					+ UsersDBHelper.Password + ", " + UsersDBHelper.UserLoginId
					+ ", " + UsersDBHelper.UserRolesId + ", "
					+ UsersDBHelper.EmployeeId + " ) VALUES("
					+ otherAttr.getInt("UserId") + ", '"
					+ userNamePwd.getString("Username") + "', '" + cipheredPwd
					+ "', '" + otherAttr.getString("UserLoginId") + "', "
					+ otherAttr.getInt("UserRolesId") + ", "
					+ otherAttr.getInt("EmployeeId") + " )";

			// Log.v("UPDATE QUERY", "" + updateQuery);
			db.execSQL(updateQuery);

		} catch (NoSuchAlgorithmException e) {
			Log.e("ALGORITHMEXCEPTION", "" + e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e("JSONEXCEPTION", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	/*****************************************************************************************
	 * read json data from the given URL
	 * ************************************************************************************/
	public JSONObject getJSONFromDB(JSONObject jsonForm) {
		try {

			String cipheredPwd = encrypt(jsonForm.getString("Password"));
			JSONObject returnJsonObject = new JSONObject();

			/**
			 * A sql query to select all columns from TABLE_USERS if uName and
			 * Pwd match
			 **/
			String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_USERS
					+ " WHERE " + UsersDBHelper.Username + " = '"
					+ jsonForm.getString("Username") + "' AND "
					+ UsersDBHelper.Password + " = '" + cipheredPwd + "'";

			// Log.v("SELECT QUERY", "" + readQuery);
			Cursor cursor = db.rawQuery(readQuery, null);
			Log.v("CURSOR SIZE:", "" + cursor.getCount());

			/**
			 * If the cursor is able to move to First, at least 1 value is
			 * present
			 **/
			if (cursor.moveToFirst()) {

				returnJsonObject.put("UserId", cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.UserId)));
				returnJsonObject.put("UserLoginId", cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.UserLoginId)));
				returnJsonObject.put("UserRolesId", cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.UserRolesId)));
				returnJsonObject.put("EmployeeId", cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EmployeeId)));
				returnJsonObject.put("AutheticationStatus", true);

			} else {
				returnJsonObject.put("UserId", 0);
				returnJsonObject.put("UserLoginId", "null");
				returnJsonObject.put("UserRolesId", 0);
				returnJsonObject.put("EmployeeId", 0);
				returnJsonObject.put("AutheticationStatus", false);
			}

			cursor.close();
			return returnJsonObject;
		} catch (NoSuchAlgorithmException e) {
			Log.e("ALGORITHMEXCEPTION", "" + e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e("JSONEXCEPTION", "" + e.getMessage());
			e.printStackTrace();
		}

		return null;
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

			// Log.v("Direct encryption is of " + pwd + " is:", "" +
			// mb.toString()+ " : " + mb.length);
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
