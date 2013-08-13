package com.ith.project.sqlite;

import java.util.ArrayList;
import org.json.JSONArray;
import com.ith.project.EntityClasses.EventParticipants;
import com.ith.project.sdcard.SQLQueryStore;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EventGoingSQLite {
	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;
	private SQLQueryStore sqlSave;

	public EventGoingSQLite(Context context) {

		usersDBHelper = new UsersDBHelper(context);
		sqlSave = new SQLQueryStore();
	}

	/***********************************************************************************
	 * To instantiate SQLiteDatabase with writable mode
	 * ************************************************************************************/
	public void openDB() {
		db = usersDBHelper.getWritableDatabase();
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

	public void updateEventGoingTable(JSONArray jsonArr) {

	}

	/**
	 * Insert into the EventGoing Table
	 * **/
	public void insertEventGoing(int eventRealId, int[] goingNamesOfAll,
			String[] goingStatsOfAll) {

		for (int i = 0; i < goingNamesOfAll.length; i++) {

			String selectCheck = "SELECT * FROM "
					+ UsersDBHelper.TABLE_EVENTGOING + " WHERE EventRealId="
					+ eventRealId + " AND EventTo=" + goingNamesOfAll[i];

			Cursor cursor = db.rawQuery(selectCheck, null);

			sqlSave.writeFile2Sdcard(selectCheck);
			if (cursor.moveToFirst()) {

				String updateQuery = "UPDATE EVENTGOING SET GoingStatus='"
						+ goingStatsOfAll[i] + "' WHERE EventRealId="
						+ eventRealId + " AND EventTo=" + goingNamesOfAll[i];
				db.execSQL(updateQuery);

				sqlSave.writeFile2Sdcard(updateQuery);
			} else {

				String insertStr = "INSERT INTO "
						+ UsersDBHelper.TABLE_EVENTGOING + " ("
						+ UsersDBHelper.EventRealId + ", "
						+ UsersDBHelper.EventTo + ", "
						+ UsersDBHelper.GoingStatus + ") VALUES ("
						+ eventRealId + ", " + goingNamesOfAll[i] + ", '"
						+ goingStatsOfAll[i] + "')";

				Log.e("INSERT INTO EVENTGOING TABLE", "" + insertStr);
				db.execSQL(insertStr);
				sqlSave.writeFile2Sdcard(insertStr);
			}
		}

	}

	/************************************************************************************
	 * Get the EventGoing columns of the sqlite
	 *********************************************************************************** **/
	public ArrayList<EventParticipants> getEventGoing(int eventRealId) {

		String getPendingQuery = "SELECT * FROM "
				+ UsersDBHelper.TABLE_EVENTGOING + " WHERE "
				+ UsersDBHelper.EventRealId + "=" + eventRealId;

		Log.e("GET EVENTSGOING", "" + getPendingQuery);

		Cursor cursor = db.rawQuery(getPendingQuery, null);
		sqlSave.writeFile2Sdcard(getPendingQuery);
		Log.v("CURSOR EVENT GOING SIZE:", "" + cursor.getCount());

		ArrayList<EventParticipants> tempArrList = new ArrayList<EventParticipants>();

		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				EventParticipants tempEvent = new EventParticipants();

				tempEvent.setGoingId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.GoingId)));
				tempEvent.setEventRealId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventRealId)));
				tempEvent.setToEmployeeId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventTo)));
				tempEvent.setGoingStatus(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.GoingStatus)));

				tempArrList.add(tempEvent);

				cursor.moveToNext();
			}

			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND for EventGoing table",
					"EventGoing Cursor Size = 0");
		}
		cursor.close();
		// db.execSQL(getPendingQuery);
		return null;
	}

	/***********************************************************************************
	 * Delete the entries from EventGoing Table Where EventRealId is given
	 * ************************************************************************************/
	public void deleteEventGoing(int eventRealId) {

		String deleteEventGoing = "DELETE FROM "
				+ UsersDBHelper.TABLE_EVENTGOING + " WHERE "
				+ UsersDBHelper.EventRealId + "=" + eventRealId;

		db.execSQL(deleteEventGoing);
		sqlSave.writeFile2Sdcard(deleteEventGoing);
	}
}
