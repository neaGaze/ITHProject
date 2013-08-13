package com.ith.project.sqlite;

import com.ith.project.EntityClasses.LoginAuthentication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MsgEntryLogSQLite {

	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;

	public MsgEntryLogSQLite(Context context) {
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
	public void updateMsgEntryLog(String currDate, String logType) {

		String updateDateLog = null;
		if (getLatestMsgDateModified(logType) == null) {
			updateDateLog = "INSERT INTO " + UsersDBHelper.TABLE_MSGENTRYLOG
					+ " ( " + UsersDBHelper.LatestDateMsg + ", "
					+ UsersDBHelper.EmployeeId + "," + UsersDBHelper.LogType
					+ ") VALUES (" + "'" + currDate + "',"
					+ LoginAuthentication.EmployeeId + ",'" + logType + "')";
			Log.e("INSERT MSGENTRYLOG Query", "" + updateDateLog);
		} else {
			updateDateLog = "UPDATE " + UsersDBHelper.TABLE_MSGENTRYLOG
					+ " SET " + UsersDBHelper.LatestDateMsg + "='" + currDate
					+ "' WHERE " + UsersDBHelper.EmployeeId + "="
					+ LoginAuthentication.EmployeeId;
			Log.e("UPDATE MSGENTRYLOG Query", "" + updateDateLog);
		}
		db.execSQL(updateDateLog);

	}

	/*****************************************************************************************
	 * read the latest date of Sync
	 * ************************************************************************************/
	public String getLatestMsgDateModified(String logType) {

		String ModDateViewQuery = "SELECT " + UsersDBHelper.LatestDateMsg
				+ " FROM " + UsersDBHelper.TABLE_MSGENTRYLOG + " WHERE "
				+ UsersDBHelper.EmployeeId + "="
				+ LoginAuthentication.EmployeeId + " AND "
				+ UsersDBHelper.LogType + "='" + logType + "'" + " ORDER BY "
				+ UsersDBHelper.MsgLogId + " DESC limit 1";

		Cursor cursor = db.rawQuery(ModDateViewQuery, null);
		if (cursor.moveToFirst()) {
			String returnLatestDate = cursor.getString(cursor
					.getColumnIndex(UsersDBHelper.LatestDateMsg));
			Log.e("Latest Date from MSGENTRYLOG for " + logType, ""
					+ returnLatestDate);
			return returnLatestDate;
		} else {
			return null;
		}
	}

	/*	*//*****************************************************************************************
	 * read the latest date of Sync
	 * ************************************************************************************/
	/*
	 * public String getLatestEventDateModified() {
	 * 
	 * String ModDateViewQuery = "SELECT " + UsersDBHelper.LatestDateMsg +
	 * " FROM " + UsersDBHelper.TABLE_MSGENTRYLOG + " WHERE " +
	 * UsersDBHelper.EmployeeId + "=" + LoginAuthentication.EmployeeId + " AND "
	 * + UsersDBHelper.LogType + "='eventLog'" + " ORDER BY " +
	 * UsersDBHelper.MsgLogId + " DESC limit 1";
	 * 
	 * Cursor cursor = db.rawQuery(ModDateViewQuery, null); if
	 * (cursor.moveToFirst()) { String returnLatestDate =
	 * cursor.getString(cursor .getColumnIndex(UsersDBHelper.LatestDateMsg));
	 * Log.e("Latest Date from MSGENTRYLOG for Event", "" + returnLatestDate);
	 * return returnLatestDate; } else { return null; } }
	 */

}
