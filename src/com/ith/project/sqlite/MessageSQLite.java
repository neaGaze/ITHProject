package com.ith.project.sqlite;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Message;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MessageSQLite {
	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;

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

	/************************************************************************************
	 * Update the Message datas according to JSON object
	 * *************************************************************************************/
	public void updateDBMsgsTableJson(String messagesFromWS,
			DateLogSQLite dateLogSQLite) {

		try {

			JSONObject messagesObj;
			messagesObj = new JSONObject(messagesFromWS);
			JSONArray messagesArr = messagesObj
					.getJSONArray("GetMessagesResult");
			for (int i = 0; i < messagesArr.length(); i++) {

				String updateQuery = "INSERT OR REPLACE INTO "
						+ UsersDBHelper.TABLE_MESSAGE + " ( "
						+ UsersDBHelper.MessageSubject + ", "
						+ UsersDBHelper.MessageDesc + ", "
						+ UsersDBHelper.MessageDate + ", "
						+ UsersDBHelper.MessageFrom + ", "
						+ UsersDBHelper.MessageTo + ", "
						+ UsersDBHelper.MessageRead + ") VALUES ('"
						+ messagesArr.getJSONObject(i).getString("Title")
						+ "', '"
						+ messagesArr.getJSONObject(i).getString("Description")
						+ "', '"
						+ messagesArr.getJSONObject(i).getString("MessageDate")
						+ "', "
						+ messagesArr.getJSONObject(i).getInt("MessageFrom")
						+ ", "
						+ messagesArr.getJSONObject(i).getInt("MessageTo")
						+ ", "
						+ messagesArr.getJSONObject(i).getInt("MessageRead")
						+ ")";

				// Log.v("UPDATE QUERY BULLETINS", "" + updateQuery);
				db.execSQL(updateQuery);
			}
		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}

	}

	/*****************************************************************************************
	 * read all message rows and populate Arraylist of Message
	 * ************************************************************************************/
	public ArrayList<Message> getJSONFromDB() {

		ArrayList<Message> tempArrList = new ArrayList<Message>();

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_MESSAGE;

		// Log.v("SELECT QUERY BULLETINS", "" + readQuery);
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
			Log.e("NO ROW FOUND at MESSAGE", "Cursor Size is 0");
		}
		cursor.close();
		return null;
	}
	
	/*****************************************************************************************
	 * read all message rows and populate Arraylist of Message
	 * ************************************************************************************/
	
	
}
