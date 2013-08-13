package com.ith.project.sqlite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ith.project.EntityClasses.Event;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.EntityClasses.Event;
import com.ith.project.EntityClasses.Message;
import com.ith.project.sdcard.SQLQueryStore;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EventSQLite {

	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;
	private MsgEntryLogSQLite eventEntryLogSQLite;
	private EventGoingSQLite eventGoingSQLite;
	private SQLQueryStore sqlSave;

	public EventSQLite(Context context) {
		// super(context, new UsersDBHelper(context));
		usersDBHelper = new UsersDBHelper(context);
		eventGoingSQLite = new EventGoingSQLite(context);
		sqlSave = new SQLQueryStore();
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

	public boolean isOpen() {
		if (db == null)
			return false;
		else
			return db.isOpen();
	}

	public void updateEventTable(String eventsFromWS,
			MsgEntryLogSQLite eventEntryLogSQLite) {

		eventGoingSQLite.openDB();

		try {
			this.eventEntryLogSQLite = eventEntryLogSQLite;
			JSONObject eventsObj;
			eventsObj = new JSONObject(eventsFromWS);
			JSONArray eventsArr = eventsObj.getJSONArray("ListOfEventsResult");

			/** Calculate the current Date and Time **/
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			String currDate = dateFormat.format(cal.getTime());

			for (int i = 0; i < eventsArr.length(); i++) {

				/** For gathering data for storing in the EventGoing Table **/
				JSONArray eventParticipantsStatus = eventsArr.getJSONObject(i)
						.getJSONArray("IsGoing");
				JSONArray eventParticipantsName = eventsArr.getJSONObject(i)
						.getJSONArray("ToEmployeeId");
				int length = eventParticipantsStatus.length();

				String goingStatus = "";
				String[] GoingStatsOfAll = new String[length];
				int[] GoingNamesOfAll = new int[length];

				for (int j = 0; j < length; j++) {

					GoingNamesOfAll[j] = eventParticipantsName.getInt(j);
					if (eventParticipantsStatus.getString(j).equals("1"))
						GoingStatsOfAll[j] = "Going";
					else if (eventParticipantsStatus.getString(j).equals("0"))
						GoingStatsOfAll[j] = "NotGoing";
					else
						GoingStatsOfAll[j] = "Pending";

					// GoingStatsOfAll[j] =
					// eventParticipantsStatus.getString(j);

					if (eventParticipantsName.getInt(j) == LoginAuthentication.EmployeeId) {

						goingStatus = GoingStatsOfAll[j];
						/*
						 * int eventGoingCode =
						 * eventParticipantsStatus.getInt(j);
						 * 
						 * if (eventGoingCode == 2) goingStatus = "Going"; else
						 * if (eventGoingCode == 1) goingStatus = "NotGoing";
						 * else goingStatus = "Pending";
						 */
						// break;
					}
				}

				/** To check for occurence of quotes ' in the String **/
				String unNormalizedBulletinTitle = eventsArr.getJSONObject(i)
						.getString("Title");
				StringBuilder normalizedTitle = new StringBuilder();
				String[] titleParts = unNormalizedBulletinTitle.split("'");
				for (int j = 0; j < titleParts.length; j++) {
					if (j == titleParts.length - 1)
						normalizedTitle.append(titleParts[j]);
					else
						normalizedTitle.append(titleParts[j] + "''");
				}

				String unNormalizedBulletinDesc = eventsArr.getJSONObject(i)
						.getString("Description");
				StringBuilder normalizedDesc = new StringBuilder();
				String[] descParts = unNormalizedBulletinDesc.split("'");
				for (int j = 0; j < descParts.length; j++) {
					if (j == descParts.length - 1)
						normalizedDesc.append(descParts[j]);
					else
						normalizedDesc.append(descParts[j] + "''");
				}

				/** Query to check if the row already exists or not **/
				String checkQuery = "SELECT * FROM "
						+ UsersDBHelper.TABLE_EVENT
						+ " WHERE "
						+ UsersDBHelper.EventRealId
						+ "="
						+ Integer.parseInt(eventsArr.getJSONObject(i)
								.getString("EventId")) /*
														 * + " AND " +
														 * UsersDBHelper
														 * .MessageDesc + "='" +
														 * eventsArr
														 * .getJSONObject
														 * (i).getString
														 * ("Description") +
														 * "' AND " +
														 * UsersDBHelper
														 * .MessageFrom + "=" +
														 * eventsArr
														 * .getJSONObject
														 * (i).getInt
														 * ("FromEmployeeId")
														 */
						+ " AND " + UsersDBHelper.EventTo + "="
						+ LoginAuthentication.EmployeeId;

				Log.e("SELECT QUERY @updateEventTable", "" + checkQuery);
				Cursor cursor = db.rawQuery(checkQuery, null);

				if (cursor.moveToFirst()) {
					/**
					 * i.e. there are rows with same eventSubject, eventDesc,
					 * Creator and reader so do update.(Now after change in
					 * sqlite I can also query for eventRealId and reader only;
					 * which gives same result)
					 **/
					String updateQuery = "UPDATE "
							+ UsersDBHelper.TABLE_EVENT
							+ " SET "
							+ UsersDBHelper.EventDate
							+ "='"
							+ eventsArr.getJSONObject(i).getString(
									"StringEventDate")
							+ "', "
							+ UsersDBHelper.GoingStatus
							+ "='"
							+ goingStatus
							+ "', "
							+ UsersDBHelper.Latitude
							+ "='"
							+ eventsArr.getJSONObject(i).getString("Latitude")
							+ "', "
							+ UsersDBHelper.Longitude
							+ "='"
							+ eventsArr.getJSONObject(i).getString("Longitude")
							+ "', "
							+ UsersDBHelper.IsEventRead
							+ "="
							+ eventsArr.getJSONObject(i).getInt("IsEventRead")
							+ ", "
							+ UsersDBHelper.EventStatus
							+ "="
							+ Integer.parseInt(eventsArr.getJSONObject(i)
									.getString("EventStatusId"))
							+ ", "
							+ UsersDBHelper.EventType
							+ "='' "
							+ " WHERE "
							+ UsersDBHelper.EventName
							+ "='"
							+ normalizedTitle.toString()
							+ "' AND "
							+ UsersDBHelper.EventDesc
							+ "='"
							+ normalizedDesc.toString()
							+ "' AND "
							+ UsersDBHelper.EventFrom
							+ "="
							+ eventsArr.getJSONObject(i).getInt(
									"OrganizerEmployeeId") + " AND "
							+ UsersDBHelper.EventTo + "="
							+ LoginAuthentication.EmployeeId;

					Log.e("UPDATE TABLE QUERY @updateEventTable", ""
							+ updateQuery);
					db.execSQL(updateQuery);
					sqlSave.writeFile2Sdcard(updateQuery);

				} else {

					String insertQuery = "INSERT INTO "
							+ UsersDBHelper.TABLE_EVENT
							+ " ( "
							+ UsersDBHelper.EventRealId
							+ ", "
							+ UsersDBHelper.EventName
							+ ", "
							+ UsersDBHelper.EventDesc
							+ ", "
							+ UsersDBHelper.EventFrom
							+ ", "
							+ UsersDBHelper.EventTo
							+ ", "
							+ UsersDBHelper.EventPlace
							+ ", "
							+ UsersDBHelper.EventDate
							+ ", "
							+ UsersDBHelper.Longitude
							+ ", "
							+ UsersDBHelper.Latitude
							+ ", "
							+ UsersDBHelper.GoingStatus
							+ ", "
							+ UsersDBHelper.IsEventRead
							+ ", "
							+ UsersDBHelper.EventStatus
							+ ") VALUES ("
							+ eventsArr.getJSONObject(i).getInt("EventId")
							+ ", '"
							+ normalizedTitle.toString()
							+ "', '"
							+ normalizedDesc.toString()
							+ "', "
							+ eventsArr.getJSONObject(i).getInt(
									"OrganizerEmployeeId")
							+ ", "
							+ LoginAuthentication.EmployeeId
							+ ", '"
							+ eventsArr.getJSONObject(i).getString("Venue")
							+ "', '"
							+ eventsArr.getJSONObject(i).getString(
									"StringEventDate")
							+ "', '"
							+ eventsArr.getJSONObject(i).getString("Longitude")
							+ "', '"
							+ eventsArr.getJSONObject(i).getString("Latitude")
							+ "', '"
							+ goingStatus
							+ "', "
							// +
							// eventsArr.getJSONObject(i).getInt("MessageTo")

							+ eventsArr.getJSONObject(i).getInt("IsEventRead")
							+ ", "
							+ eventsArr.getJSONObject(i)
									.getInt("EventStatusId") + ")";

					Log.e("UPDATE QUERY EVENTS @updateEventTable", ""
							+ insertQuery);

					/**
					 * insert into EventGoingSQLite about the receivers and
					 * goingstatus of the Events
					 **/

					db.execSQL(insertQuery);
					sqlSave.writeFile2Sdcard(insertQuery);
				}

				eventGoingSQLite.insertEventGoing(eventsArr.getJSONObject(i)
						.getInt("EventId"), GoingNamesOfAll, GoingStatsOfAll);
			}
			this.eventEntryLogSQLite.updateMsgEntryLog(currDate, "eventLog");
		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}

		eventGoingSQLite.closeDB();
	}

	public ArrayList<Event> selectPendingEvents(String pendingEvent) {

		String getPendingQuery = "SELECT * FROM " + UsersDBHelper.TABLE_EVENT
				+ " WHERE " + UsersDBHelper.EventType + "='" + pendingEvent
				+ "'";

		Log.e("GET PENDING EVENTS", "" + getPendingQuery);

		sqlSave.writeFile2Sdcard(getPendingQuery);

		Cursor cursor = db.rawQuery(getPendingQuery, null);
		Log.v("CURSOR PENDING EVENTS SIZE:", "" + cursor.getCount());

		ArrayList<Event> tempArrList = new ArrayList<Event>();

		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				Event tempEvent = new Event();

				tempEvent.setEventId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventId)));
				tempEvent.setEventRealId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventRealId)));
				tempEvent.setEventName(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventName)));
				tempEvent.setEventDesc(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventDesc)));
				tempEvent.setEventDateTime(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventDate)));
				tempEvent.setEventCreator(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventFrom)));
				tempEvent.setEventTo(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventTo)));
				tempEvent.setEventRead(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.IsEventRead)));
				tempEvent.setEventPlace(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventPlace)));
				tempEvent.setEventParticipation(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.GoingStatus)));
				tempEvent.setEventLongitude(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Longitude)));
				tempEvent.setEventLatitude(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Latitude)));
				tempEvent.setEventStatus(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventStatus)));

				tempArrList.add(tempEvent);

				cursor.moveToNext();
			}

			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND for Pending Events",
					"Pending Events Cursor Size = 0");
		}
		cursor.close();
		// db.execSQL(getPendingQuery);
		return null;
	}

	/***********************************************************************************
	 * Collectively delete the ArrayList of Events (mainly called after
	 * 'eventPending' is successfully updated to the main DB)
	 ***********************************************************************************/
	public void deleteEvents(ArrayList<Event> sentEvents) {

		for (int i = 0; i < sentEvents.size(); i++) {
			String deleteEvents = "DELETE FROM " + UsersDBHelper.TABLE_EVENT
					+ " WHERE " + UsersDBHelper.EventId + "="
					+ sentEvents.get(i).getEventId();

			Log.e("deleteEVents query", "" + deleteEvents);
			db.execSQL(deleteEvents);
			sqlSave.writeFile2Sdcard(deleteEvents);
		}
	}

	/***********************************************************************************
	 * Update the Cancelled Status in the Created Events by the Creator
	 * ************************************************************************************/
	public void cancelEvent(int eventId, String eventType) {
		String cancelEventQuery = "UPDATE " + UsersDBHelper.TABLE_EVENT
				+ " SET " + UsersDBHelper.EventType + "='" + eventType + "', "
				+ UsersDBHelper.GoingStatus + "='Cancelled', "
				+ UsersDBHelper.EventStatus + "=" + 3 + " WHERE "
				+ UsersDBHelper.EventRealId + "=" + eventId;

		Log.e("cancel Event Query: ", "" + cancelEventQuery);
		db.execSQL(cancelEventQuery);

		sqlSave.writeFile2Sdcard(cancelEventQuery);
	}

	/***********************************************************************************
	 * Delete the Cancelled Flagged Event from sqlite
	 * ************************************************************************************/
	public void deleteEvent(int eventId) {
		String deleteEvent = "DELETE FROM " + UsersDBHelper.TABLE_EVENT
				+ " WHERE " + UsersDBHelper.EventId + "=" + eventId;

		Log.e("Delete from EVENTS: ", "" + deleteEvent);
		db.execSQL(deleteEvent);

		sqlSave.writeFile2Sdcard(deleteEvent);

		eventGoingSQLite.openDB();
		eventGoingSQLite.deleteEventGoing(eventId);
		eventGoingSQLite.closeDB();
	}

	/*****************************************************************************************
	 * This is used to change the EventRead=1 after reading is complete
	 * ************************************************************************************/
	public void updateEventRead(int eventTo, int eventRealId) {
		String updateReadQuery = "UPDATE " + UsersDBHelper.TABLE_EVENT
				+ " SET " + UsersDBHelper.IsEventRead + "=" + 1 + " WHERE "
				+ UsersDBHelper.EventTo + "=" + eventTo + " AND "
				+ UsersDBHelper.EventRealId + "=" + eventRealId;
		// UPDATE EVENTS SET EventRead=1 WHERE EventTo=10 AND
		// EventRealId=2;
		db.execSQL(updateReadQuery);

		sqlSave.writeFile2Sdcard(updateReadQuery);
	}

	/***********************************************************************************
	 * This is used to change the EventType = eventUpdatePending
	 * ***********************************************************************************/
	public void updateEventReadPending(int eventTo, int eventRealId,
			String pendingStatus) {

		String updateReadQuery = "UPDATE " + UsersDBHelper.TABLE_EVENT
				+ " SET " + UsersDBHelper.EventType + "='" + pendingStatus
				+ "' WHERE " + UsersDBHelper.EventTo + "=" + eventTo + " AND "
				+ UsersDBHelper.EventRealId + "=" + eventRealId;
		// UPDATE EVENTS SET EventType='readEventUpdatePending' WHERE
		// EventTo=10 AND MEventRealId=2;
		db.execSQL(updateReadQuery);

		sqlSave.writeFile2Sdcard(updateReadQuery);
	}

	/***********************************************************************************
	 * This is used to change the EventType = GoingUpdatePending
	 * ***********************************************************************************/
	public void updateGoingPending(int eventTo, int eventRealId,
			String pendingStatus) {

		String updateReadQuery = "UPDATE " + UsersDBHelper.TABLE_EVENT
				+ " SET " + UsersDBHelper.EventType + "='" + pendingStatus
				+ "' WHERE " + UsersDBHelper.EventTo + "=" + eventTo + " AND "
				+ UsersDBHelper.EventRealId + "=" + eventRealId;
		// UPDATE EVENTS SET EventType='goingUpdatePending' WHERE
		// EventTo=10 AND EventRealId=2;
		db.execSQL(updateReadQuery);

		sqlSave.writeFile2Sdcard(updateReadQuery);
	}

	public void updateEventGoing(int eventTo, int eventRealId,
			String goingStatus) {

		String updateGoingStatus = "UPDATE " + UsersDBHelper.TABLE_EVENT
				+ " SET " + UsersDBHelper.GoingStatus + "='" + goingStatus
				+ "' WHERE " + UsersDBHelper.EventTo + "=" + eventTo + " AND "
				+ UsersDBHelper.EventRealId + "=" + eventRealId;

		Log.e("UPDATE GOING STATUS", "" + updateGoingStatus);
		db.execSQL(updateGoingStatus);

		sqlSave.writeFile2Sdcard(updateGoingStatus);
	}

	/*****************************************************************************************
	 * read all Events rows and populate Arraylist of Events
	 * ************************************************************************************/
	public ArrayList<Event> getEventsFromDB() {

		ArrayList<Event> tempArrList = new ArrayList<Event>();

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_EVENT
				+ " WHERE " + UsersDBHelper.EventTo + "="
				+ LoginAuthentication.EmployeeId + " ORDER BY "
				+ UsersDBHelper.EventId + " DESC";

		// Log.e("EmployeeId is:", "" + LoginAuthentication.EmployeeId);
		// Log.v("SELECT QUERY BULLETINS", "" + readQuery);
		Cursor cursor = db.rawQuery(readQuery, null);

		sqlSave.writeFile2Sdcard(readQuery);
		Log.v("CURSOR EVENTS SIZE:", "" + cursor.getCount());

		/**
		 * If the cursor is able to move to First, at least 1 value is present
		 **/
		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				Event tempEvent = new Event();

				tempEvent.setEventId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventId)));
				tempEvent.setEventRealId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventRealId)));
				tempEvent.setEventName(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventName)));
				tempEvent.setEventDesc(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventDesc)));
				tempEvent.setEventDateTime(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventDate)));
				tempEvent.setEventPlace(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventPlace)));
				tempEvent.setEventCreator(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventFrom)));
				tempEvent.setEventTo(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventTo)));
				tempEvent.setEventRead(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.IsEventRead)));
				tempEvent.setEventParticipation(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.GoingStatus)));
				tempEvent.setEventLongitude(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Longitude)));
				tempEvent.setEventLatitude(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Latitude)));
				tempEvent.setEventType(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventType)));
				tempEvent.setEventStatus(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EventStatus)));

				// String dateTime = cursor.getString(cursor
				// .getColumnIndex(UsersDBHelper.EventDate));
				// if (dateTime == null || dateTime.equals("") ||
				// dateTime.equals(null))
				// Log.e("tait auda ", "huwa fucchheee !!!");
				// else {

				// Log.e("tait auda ", "huwa kutttteeee !!!");
				tempEvent.parseDateTime(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EventDate)));
				// }
				tempArrList.add(tempEvent);

				cursor.moveToNext();
			}

			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND at Event@getEventsFromDB", "Cursor Size is 0");
		}
		cursor.close();
		return null;
	}

	/*************************************************************************************
	 * Save the type Events as draft with EventType = eventPending
	 * *************************************************************************************/
	public void saveEventDraft(int eventFrom, Integer[] receiversId,
			String eventTitleStr, String eventDescStr, String eventDateTimeStr,
			String eventVenueStr, String longitude, String latitude,
			String goingStatus, int eventStatus, int isRead, String pending) {

		for (int i = 0; i < receiversId.length; i++) {
			String updateQuery = "INSERT OR REPLACE INTO "
					+ UsersDBHelper.TABLE_EVENT + " ( "
					+ UsersDBHelper.EventName + ", " + UsersDBHelper.EventDesc
					+ ", " + UsersDBHelper.EventFrom + ", "
					+ UsersDBHelper.EventTo + ", " + UsersDBHelper.EventPlace
					+ ", " + UsersDBHelper.EventDate + ", "
					+ UsersDBHelper.Latitude + ", " + UsersDBHelper.Longitude
					+ ", " + UsersDBHelper.GoingStatus + ", "
					+ UsersDBHelper.IsEventRead + ","
					+ UsersDBHelper.EventStatus + ", "
					+ UsersDBHelper.EventType + ") VALUES ('" + eventTitleStr
					+ "', '" + eventDescStr + "', " + eventFrom + ", "
					+ receiversId[i] + ", '" + eventVenueStr + "', '"
					+ eventDateTimeStr + "', '" + 0.0/* latitude */+ "', '"
					+ 0.0/* longitude */
					+ "', '" + goingStatus + "', " + isRead + "," + eventStatus
					+ ",'" + pending + "')";
			// Log.v("Save Event as Draft", "" + updateQuery);

			/*
			 * INSERT INTO EVENTS ( EventName, EventDesc, EventFrom, EventTo,
			 * EventPlace,EventDate, Longitude,Latitude, GoingStatus,
			 * IsEventRead, EventStatus, EventType) VALUES('Android
			 * Training','google bvata manxe audai ho',3,4,'Hotel
			 * Soaltee','20130705125900',0.0,0.0,"Pending",0,1,'eventPending');
			 */

			db.execSQL(updateQuery);

			sqlSave.writeFile2Sdcard(updateQuery);
		}

	}

	/*************************************************************************************
	 * Postpone the Event
	 **************************************************************************************/
	public void updateEventPostponed(int eventRealId, String eventDateTimeStr,
			String postponeStr) {

		String postponeQuery = "UPDATE " + UsersDBHelper.TABLE_EVENT + " SET "
				+ UsersDBHelper.EventDate + "='" + eventDateTimeStr + "', "
				+ UsersDBHelper.EventStatus + "=" + 4 + ", "
				+ UsersDBHelper.EventType + "='" + postponeStr + "' WHERE "
				+ UsersDBHelper.EventRealId + "=" + eventRealId;

		Log.e("postpone Query", "" + postponeQuery);
		db.execSQL(postponeQuery);
		sqlSave.writeFile2Sdcard(postponeQuery);

	}

}
