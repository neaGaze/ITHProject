package com.ith.project.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DateLogSQLite {

	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;
	public DateLogSQLite(Context context) {
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
	 * Update the DateLog datas according to the date provided
	 * *************************************************************************************/
	public void updateDateLog(String currDate) {

		String updateDateLog = "INSERT INTO " + UsersDBHelper.TABLE_DATELOG
				+ " ( " + UsersDBHelper.LatestDate + ") VALUES (" + "'"
				+ currDate + "')";
		Log.e("INSERT DATELOG Query", "" + updateDateLog);
		db.execSQL(updateDateLog);

	}

	/*****************************************************************************************
	 * read the latest date of Sync
	 * ************************************************************************************/
	public String getLatestDateModified() {

		String ModDateViewQuery = "SELECT " + UsersDBHelper.LatestDate
				+ " FROM " + UsersDBHelper.TABLE_DATELOG + " ORDER BY "
				+ UsersDBHelper.LogId + " DESC limit 1";

		Cursor cursor = db.rawQuery(ModDateViewQuery, null);
		if (cursor.moveToFirst()) {
			String returnLatestDate = cursor.getString(cursor
					.getColumnIndex(UsersDBHelper.LatestDate));
			Log.e("Latest Date from DATELOG", "" + returnLatestDate);
			return returnLatestDate;
		} else {
			return null;
		}
	}

}
