package com.ith.project.sqlite;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ith.project.EntityClasses.Bulletin;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BulletinSQLite {

	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;
	private static int BulletinId = 0;

	public BulletinSQLite(Context context) {
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

	/***********************************************************************************
	 * To check if the db is open or not
	 * ************************************************************************************/
	public boolean isOpen() {
		if (db == null)
			return false;
		else
			return db.isOpen();
	}

	/************************************************************************************
	 * Insert the Bulletin datas into sqlite according to values
	 * *************************************************************************************/
	public void insertDBUsersTableValues(String Title, String Desc,
			String BulletinDate, String empName) {

		String insertQuery = "INSERT OR REPLACE INTO "
				+ UsersDBHelper.TABLE_BULLETINS + " (" + UsersDBHelper.Title
				+ ", " + UsersDBHelper.Description + ", "
				+ UsersDBHelper.BulletinDate + ", "
				+ UsersDBHelper.EmployeeName + " ) VALUES ('" + Title + "', '"
				+ Desc + "', '" + BulletinDate + "', '" + empName + "')";

		// Log.v("INSERT QUERY BULLETINS", "" + insertQuery);
		db.execSQL(insertQuery);
	}

	/************************************************************************************
	 * Update the Bulletin datas according to JSON object
	 * *************************************************************************************/
	public void updateDBUsersTableJson(String bulletinFromWS) {
		try {

			JSONObject bulletinsObj;
			bulletinsObj = new JSONObject(bulletinFromWS);
			JSONArray bulletinsArr = bulletinsObj
					.getJSONArray("GetBulletinsResult");
			BulletinId = 0;
			for (int i = 0; i < bulletinsArr.length(); i++) {

				/** To check for occurence of quotes ' in the String **/
				String unNormalizedBulletinTitle = bulletinsArr
						.getJSONObject(i).getString("Title");
				StringBuilder normalizedTitle = new StringBuilder();
				String[] titleParts = unNormalizedBulletinTitle.split("'");
				for (int j = 0; j < titleParts.length; j++) {
					if (j == titleParts.length - 1)
						normalizedTitle.append(titleParts[j]);
					else
						normalizedTitle.append(titleParts[j] + "''");
				}

				String unNormalizedBulletinDesc = bulletinsArr.getJSONObject(i)
						.getString("Description");
				StringBuilder normalizedDesc = new StringBuilder();
				String[] descParts = unNormalizedBulletinDesc.split("'");
				for (int j = 0; j < descParts.length; j++) {
					if (j == descParts.length - 1)
						normalizedDesc.append(descParts[j]);
					else
						normalizedDesc.append(descParts[j] + "''");
				}

				String updateQuery = "INSERT OR REPLACE INTO "
						+ UsersDBHelper.TABLE_BULLETINS
						+ " ( "
						+ UsersDBHelper.BulletinId
						+ ", "
						+ UsersDBHelper.Title
						+ ", "
						+ UsersDBHelper.Description
						+ ", "
						+ UsersDBHelper.BulletinDate
						+ ", "
						+ UsersDBHelper.EmployeeName
						+ ") VALUES ("
						+ (BulletinId++)
						+ ", '"
						+ normalizedTitle.toString()
						+ "', '"
						+ normalizedDesc.toString()
						+ "', '"
						+ bulletinsArr.getJSONObject(i).getString(
								"BulletinDate")
						+ "', '"
						+ bulletinsArr.getJSONObject(i).getString(
								"EmployeeName") + "')";

				// Log.v("UPDATE QUERY BULLETINS", "" + updateQuery);
				db.execSQL(updateQuery);
			}
		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	/*****************************************************************************************
	 * Delete All Table Rows
	 * ************************************************************************************/
	public void deleteAllRows() {
		db.delete(UsersDBHelper.TABLE_BULLETINS, null, null);
	}

	/*****************************************************************************************
	 * read all bulletin rows and populate Arraylist of Bulletin
	 * ************************************************************************************/
	public ArrayList<Bulletin> getJSONFromDB() {

		ArrayList<Bulletin> tempArrList = new ArrayList<Bulletin>();

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_BULLETINS
				+ " ORDER BY " + UsersDBHelper.BulletinId + " DESC";

		// Log.v("SELECT QUERY BULLETINS", "" + readQuery);
		Cursor cursor = db.rawQuery(readQuery, null);
		Log.v("CURSOR BULLETINS SIZE:", "" + cursor.getCount());

		/**
		 * If the cursor is able to move to First, at least 1 value is present
		 **/
		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				Bulletin tempBulletin = new Bulletin();

				tempBulletin.setBulletinId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.BulletinId)));
				tempBulletin.setTitle(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Title)));
				tempBulletin.setDesc(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Description)));
				tempBulletin.setDate(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.BulletinDate)));
				tempBulletin.setEmpName(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EmployeeName)));
				tempBulletin.parseDateTime(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.BulletinDate)));

				tempArrList.add(tempBulletin);

				cursor.moveToNext();
			}

			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND", "Cursor Size is 0");
		}
		cursor.close();
		return null;
	}

}
