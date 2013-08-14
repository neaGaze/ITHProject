package com.ith.project.sqlite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.EntityClasses.Message;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MessageSQLite {
	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;
	private MsgEntryLogSQLite msgEntryLogSQLite;

	public MessageSQLite(Context context) {
		usersDBHelper = new UsersDBHelper(context);
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

	/*************************************************************************************
	 * Save the type msgs as draft with MessageType = pending
	 * *************************************************************************************/
	public void saveMsgDraft(int msgFrom, Integer[] receiversId,
			String msgTitle, String msgDesc, String dateTime, int msgRead,
			String pending) {

		for (int i = 0; i < receiversId.length; i++) {
			String updateQuery = "INSERT OR REPLACE INTO "
					+ UsersDBHelper.TABLE_MESSAGE + " ( "
					+ UsersDBHelper.MessageSubject + ", "
					+ UsersDBHelper.MessageDesc + ", "
					+ UsersDBHelper.MessageDate + ", "
					+ UsersDBHelper.MessageFrom + ", "
					+ UsersDBHelper.MessageTo + ", "
					+ UsersDBHelper.MessageRead + ", "
					+ UsersDBHelper.MessageType + ") VALUES ('" + msgTitle
					+ "', '" + msgDesc + "', '" + dateTime + "', " + msgFrom
					+ ", " + receiversId[i] + ", " + msgRead + ",'" + pending
					+ "')";

			db.execSQL(updateQuery);
		}

	}

	/************************************************************************************
	 * INSERT OR UPDATE the Message datas according to JSON object
	 * *************************************************************************************/
	public void updateMessageTable(String messagesFromWS,
			MsgEntryLogSQLite msgEntryLogSQLite) {
		// INSERT INTO ENTRYLOG (MessageSubject, MessageDesc, MessageFrom,
		// MessageTo, MessageDate, MessageRead, MessageType) VALUES ('Its CS
		// Time','Ok Guys enough Work. Now its CS time. Come
		// on',3,10,'20130724103241',0,'web');
		try {
			this.msgEntryLogSQLite = msgEntryLogSQLite;
			JSONObject messagesObj;
			messagesObj = new JSONObject(messagesFromWS);
			JSONArray messagesArr = messagesObj
					.getJSONArray("GetMessagesResult");

			/** Calculate the current Date and Time **/
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmss", Locale.US);
			String currDate = dateFormat.format(cal.getTime());

			for (int i = 0; i < messagesArr.length(); i++) {

				/** To check for occurence of quotes ' in the String **/
				String unNormalizedBulletinTitle = messagesArr.getJSONObject(i)
						.getString("Subject");
				StringBuilder normalizedTitle = new StringBuilder();
				String[] titleParts = unNormalizedBulletinTitle.split("'");
				for (int j = 0; j < titleParts.length; j++) {
					if (j == titleParts.length - 1)
						normalizedTitle.append(titleParts[j]);
					else
						normalizedTitle.append(titleParts[j] + "''");
				}

				String unNormalizedBulletinDesc = messagesArr.getJSONObject(i)
						.getString("Description");
				StringBuilder normalizedDesc = new StringBuilder();
				String[] descParts = unNormalizedBulletinDesc.split("'");
				for (int j = 0; j < descParts.length; j++) {
					if (j == descParts.length - 1)
						normalizedDesc.append(descParts[j]);
					else
						normalizedDesc.append(descParts[j] + "''");
				}

				String checkQuery = "SELECT * FROM "
						+ UsersDBHelper.TABLE_MESSAGE + " WHERE "
						+ UsersDBHelper.MessageSubject + "='"
						+ normalizedTitle.toString() + "' AND "
						+ UsersDBHelper.MessageDesc + "='"
						+ normalizedDesc.toString() + "' AND "
						+ UsersDBHelper.MessageFrom + "="
						+ messagesArr.getJSONObject(i).getInt("FromEmployeeId")
						+ " AND " + UsersDBHelper.MessageTo + "="
						+ LoginAuthentication.EmployeeId;
				Cursor cursor = db.rawQuery(checkQuery, null);

				if (cursor.moveToFirst()) {
					/**
					 * i.e. there are rows with same msgSubject, Desc, Creator
					 * and reader so do update.(Now after change in sqlite I can
					 * also query for msgRealId and reader only; which gives
					 * same result)
					 **/
					String updateQuery = "UPDATE "
							+ UsersDBHelper.TABLE_MESSAGE
							+ " SET "
							+ UsersDBHelper.MessageDate
							+ "='"
							+ messagesArr.getJSONObject(i).getString(
									"StringDateSent")
							+ "', "
							+ UsersDBHelper.MessageRead
							+ "="
							+ messagesArr.getJSONObject(i).getInt(
									"IsMessageRead")
							+ ", "
							+ UsersDBHelper.MessageType
							+ "='' "
							+ " WHERE "
							+ UsersDBHelper.MessageSubject
							+ "='"
							+ normalizedTitle.toString()
							+ "' AND "
							+ UsersDBHelper.MessageDesc
							+ "='"
							+ normalizedDesc.toString()
							+ "' AND "
							+ UsersDBHelper.MessageFrom
							+ "="
							+ messagesArr.getJSONObject(i).getInt(
									"FromEmployeeId") + " AND "
							+ UsersDBHelper.MessageTo + "="
							+ LoginAuthentication.EmployeeId;

					db.execSQL(updateQuery);

				} else {
					String insertQuery = "INSERT INTO "
							+ UsersDBHelper.TABLE_MESSAGE
							+ " ( "
							+ UsersDBHelper.MessageRealId
							+ ", "
							+ UsersDBHelper.MessageSubject
							+ ", "
							+ UsersDBHelper.MessageDesc
							+ ", "
							+ UsersDBHelper.MessageDate
							+ ", "
							+ UsersDBHelper.MessageFrom
							+ ", "
							+ UsersDBHelper.MessageTo
							+ ", "
							+ UsersDBHelper.MessageRead
							+ ") VALUES ("
							+ messagesArr.getJSONObject(i).getInt("MessageId")
							+ ", '"
							+ normalizedTitle.toString()
							+ "', '"
							+ normalizedDesc.toString()
							+ "', '"
							+ messagesArr.getJSONObject(i).getString(
									"StringDateSent")
							+ "', "
							+ messagesArr.getJSONObject(i).getInt(
									"FromEmployeeId")
							+ ", "
							+ LoginAuthentication.EmployeeId
							+ ", "
							+ messagesArr.getJSONObject(i).getInt(
									"IsMessageRead") + ")";

					db.execSQL(insertQuery);
				}
			}
			this.msgEntryLogSQLite.updateMsgEntryLog(currDate, "messageLog");
		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	/*****************************************************************************************
	 * read all message rows and populate Arraylist of Message
	 * ************************************************************************************/
	public ArrayList<Message> getMsgsFromDB() {

		ArrayList<Message> tempArrList = new ArrayList<Message>();

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_MESSAGE
				+ " WHERE " + UsersDBHelper.MessageTo + "="
				+ LoginAuthentication.EmployeeId + " ORDER BY "
				+ UsersDBHelper.MessageId + " DESC";

		Log.e("EmployeeId is:", "" + LoginAuthentication.EmployeeId);
		Cursor cursor = db.rawQuery(readQuery, null);
		Log.v("CURSOR MESSAGES SIZE:", "" + cursor.getCount());

		/**
		 * If the cursor is able to move to First, at least 1 value is present
		 **/
		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				Message tempMessage = new Message();

				tempMessage.setMsgId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageId)));
				tempMessage.setMsgRealId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageRealId)));
				tempMessage.setMsgTitle(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageSubject)));
				tempMessage.setMsgDesc(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageDesc)));
				tempMessage.setMsgDate(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageDate)));
				tempMessage.setMsgFrom(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageFrom)));
				tempMessage.setMsgTo(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageTo)));
				tempMessage.setMsgRead(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageRead)));
				tempMessage.setMsgType(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageType)));

				tempMessage.parseDateTime(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageDate)));

				tempArrList.add(tempMessage);

				cursor.moveToNext();
			}

			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND at MESSAGE@getMsgsFromDB", "Cursor Size is 0");
		}
		cursor.close();
		return null;
	}

	/*****************************************************************************************
	 * This is used to change the MessageRead=1 after reading is complete
	 * ************************************************************************************/
	public void updateMsgRead(int msgTo, int msgRealId) {
		String updateReadQuery = "UPDATE " + UsersDBHelper.TABLE_MESSAGE
				+ " SET " + UsersDBHelper.MessageRead + "=" + 1 + " WHERE "
				+ UsersDBHelper.MessageTo + "=" + msgTo + " AND "
				+ UsersDBHelper.MessageRealId + "=" + msgRealId;
		// UPDATE MESSAGES SET MessageRead=1 WHERE MessageTo=10 AND
		// MessageRealId=2;
		db.execSQL(updateReadQuery);
	}

	/***********************************************************************************
	 * This is used to change the MessageType = readUpdatePending
	 * ***********************************************************************************/
	public void updateReadPending(int msgTo, int msgRealId, String pendingStatus) {
		String updateReadQuery = "UPDATE " + UsersDBHelper.TABLE_MESSAGE
				+ " SET " + UsersDBHelper.MessageType + "='" + pendingStatus
				+ "' WHERE " + UsersDBHelper.MessageTo + "=" + msgTo + " AND "
				+ UsersDBHelper.MessageRealId + "=" + msgRealId;
		// UPDATE MESSAGES SET MessageType='readUpdatePending' WHERE
		// MessageTo=10 AND MessageRealId=2;
		db.execSQL(updateReadQuery);
	}

	/*****************************************************************************************
	 * delete From sqlite deleted Messages
	 * ************************************************************************************/
	public void deleteMessages(ArrayList<Message> delArr) {
		Integer[] msgId = new Integer[delArr.size()];
		for (int i = 0; i < delArr.size(); i++) {
			msgId[i] = delArr.get(i).getMsgId();

			deleteOneMessage(msgId[i]);

		}
	}

	public void deleteOneMessage(int msgId) {
		String deleteQuery = "DELETE FROM " + UsersDBHelper.TABLE_MESSAGE
				+ " WHERE " + UsersDBHelper.MessageId + " = " + msgId;

		Log.v("DELETE QUERY MESSAGES", "" + deleteQuery);
		db.execSQL(deleteQuery);
	}

	/*************************************************************************************
	 * This is used to get the pending msgs in sqlite and send them to the
	 * webservice
	 * *************************************************************************************/
	public ArrayList<Message> selectPendingMsgs(String pendingMsg) {
		String getPendingQuery = "SELECT * FROM " + UsersDBHelper.TABLE_MESSAGE
				+ " WHERE " + UsersDBHelper.MessageType + "='" + pendingMsg
				+ "'";

		Log.e("GET PENDING MESSAGES", "" + getPendingQuery);

		Cursor cursor = db.rawQuery(getPendingQuery, null);
		Log.v("CURSOR PENDING MESSAGES SIZE:", "" + cursor.getCount());

		ArrayList<Message> tempArrList = new ArrayList<Message>();

		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				Message tempMessage = new Message();

				tempMessage.setMsgId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageId)));
				tempMessage.setMsgRealId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageRealId)));
				tempMessage.setMsgTitle(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageSubject)));
				tempMessage.setMsgDesc(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageDesc)));
				tempMessage.setMsgDate(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageDate)));
				tempMessage.setMsgFrom(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageFrom)));
				tempMessage.setMsgTo(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageTo)));
				tempMessage.setMsgRead(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageRead)));
				tempMessage.parseDateTime(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageDate)));

				tempArrList.add(tempMessage);

				cursor.moveToNext();
			}

			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND for Pending Msgs",
					"Pending Msgs Cursor Size = 0");
		}
		cursor.close();
		return null;
	}

	/*****************************************************************************************
	 * read the Message of Viewed Item
	 * ************************************************************************************/
	public Message getViewedMessage(int messageId) {

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_MESSAGE
				+ " WHERE " + UsersDBHelper.MessageId + "=" + messageId
				+ " ORDER BY " + UsersDBHelper.MessageId + " DESC";

		Cursor cursor = db.rawQuery(readQuery, null);
		Log.v("CURSOR Viewed Message SIZE:", "" + cursor.getCount());

		/**
		 * If the cursor is able to move to First, at least 1 value is present
		 **/
		if (cursor.moveToFirst()) {

			Message tempMessage = new Message();
			while (!cursor.isAfterLast()) {

				tempMessage.setMsgId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageId)));
				tempMessage.setMsgRealId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageRealId)));
				tempMessage.setMsgTitle(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageSubject)));
				tempMessage.setMsgDesc(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageDesc)));
				tempMessage.setMsgDate(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageDate)));
				tempMessage.setMsgFrom(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageFrom)));
				tempMessage.setMsgTo(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageTo)));
				tempMessage.setMsgRead(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.MessageRead)));
				tempMessage.parseDateTime(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.MessageDate)));

				cursor.moveToNext();
			}

			cursor.close();
			return tempMessage;
		}
		return null;
	}

}
