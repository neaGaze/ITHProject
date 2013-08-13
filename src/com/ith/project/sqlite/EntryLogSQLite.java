package com.ith.project.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EntryLogSQLite {

	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;

	public EntryLogSQLite(Context context) {
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
	 * Update the ENTRYLOG datas according to the date provided
	 * *************************************************************************************/
	public void updateEntryLog(String currDate) {

		String updateDateLog = "INSERT INTO " + UsersDBHelper.TABLE_ENTRYLOG
				+ " ( " + UsersDBHelper.LatestDate + ") VALUES (" + "'"
				+ currDate + "')";
		Log.e("INSERT ENTRYLOG Query", "" + updateDateLog);
		db.execSQL(updateDateLog);

	}

	/*****************************************************************************************
	 * read the latest date of Sync
	 * ************************************************************************************/
	public String getLatestDateModified() {

		String ModDateViewQuery = "SELECT " + UsersDBHelper.LatestDate
				+ " FROM " + UsersDBHelper.TABLE_ENTRYLOG + " ORDER BY "
				+ UsersDBHelper.LogId + " DESC limit 1";

		Cursor cursor = db.rawQuery(ModDateViewQuery, null);
		if (cursor.moveToFirst()) {
			String returnLatestDate = cursor.getString(cursor
					.getColumnIndex(UsersDBHelper.LatestDate));
			Log.e("Latest Date from ENTRYLOG", "" + returnLatestDate);
			return returnLatestDate;
		} else {
			return null;
		}
	}

}
