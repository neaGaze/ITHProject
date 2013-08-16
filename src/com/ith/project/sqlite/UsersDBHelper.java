package com.ith.project.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UsersDBHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "EMS.db";
	public static final int DB_VERSION = 1;

	/**
	 * For USERS Table
	 * **/
	public static final String UserId = "UserId";
	public static final String Username = "Username";
	public static final String Password = "Password";
	public static final String UserLoginId = "UserLoginId";
	public static final String UserRolesId = "UserRolesId";
	public static final String EmployeeId = "EmployeeId";
	public static final String TABLE_USERS = "USERS";

	private static final String USERS_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_USERS + " (" + UserId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Username
			+ " TEXT NOT NULL, " + Password + " TEXT NOT NULL, " + UserLoginId
			+ " TEXT NOT NULL, " + UserRolesId + " INTEGER NOT NULL, "
			+ EmployeeId + " INTEGER NOT NULL)";

	/**
	 * For BULLETIN Table
	 * **/
	public static final String BulletinId = "BulletinId";
	public static final String Description = "Description";
	public static final String EmployeeName = "EmployeeName";
	public static final String Title = "Title";
	public static final String BulletinDate = "BulletinDate";
	public static final String TABLE_BULLETINS = "BULLETINS";

	private static final String BULLETINS_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_BULLETINS + " (" + BulletinId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Title
			+ " TEXT NOT NULL, " + Description + " TEXT NOT NULL" + ", "
			+ BulletinDate + " TEXT NOT NULL, " + EmployeeName
			+ " INTEGER NOT NULL )";

	/**
	 * For EMPLOYEES Table
	 * **/
	public static final String Gender = "Gender";
	public static final String HomePhone = "HomePhone";
	public static final String Mobile = "Mobile";
	public static final String Email = "Email";
	public static final String Address = "Address";
	public static final String Designation = "Designation";
	public static final String Remarks = "Remarks";
	public static final String TABLE_EMPLOYEES = "EMPLOYEES";
	public static final String DateModified = "DateModified";

	private static final String EMPLOYEES_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_EMPLOYEES + " (" + EmployeeId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + UserId + " INTEGER, "
			+ EmployeeName + " TEXT NOT NULL, " + Gender + " TEXT NOT NULL, "
			+ HomePhone + " TEXT, " + Mobile + " TEXT NOT NULL, " + Email
			+ " TEXT NOT NULL, " + Address + " TEXT NOT NULL, " + Designation
			+ " TEXT NOT NULL, " + Remarks + " TEXT, " + DateModified + " TEXT"
			+ ")";

	/**
	 * For Latest DateTime in Employees Log
	 * **/
	public static final String LogId = "LogId";
	public static final String LatestDate = "LatestDate";
	public static final String TABLE_ENTRYLOG = "ENTRYLOG";

	private static final String ENTRYLOG_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_ENTRYLOG + " (" + LogId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + LatestDate + " TEXT)";

	/**
	 * For MESSAGE TABLE
	 * **/
	public static final String MessageId = "MessageId";
	public static final String MessageRealId = "MessageRealId";
	public static final String MessageSubject = "MessageSubject";
	public static final String MessageDesc = "MessageDesc";
	public static final String MessageFrom = "MessageFrom";
	public static final String MessageTo = "MessageTo";
	public static final String MessageDate = "MessageDate";
	public static final String MessageRead = "MessageRead";
	public static final String MessageType = "MessageType";
	public static final String TABLE_MESSAGE = "MESSAGES";

	private static final String MESSAGE_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_MESSAGE + " (" + MessageId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + MessageRealId
			+ " INTEGER, " + MessageSubject + " TEXT NOT NULL, " + MessageDesc
			+ " TEXT NOT NULL, " + MessageFrom + " INTEGER NOT NULL, "
			+ MessageTo + " INTEGER NOT NULL, " + MessageDate + " TEXT, "
			+ MessageRead + " INTEGER NOT NULL, " + MessageType + " TEXT)";

	/**
	 * For EVENTS TABLE
	 * **/
	public static final String EventId = "EventId";
	public static final String EventRealId = "EventRealId";
	public static final String EventName = "EventName";
	public static final String EventDesc = "EventDesc";
	public static final String EventFrom = "EventFrom";
	public static final String EventTo = "EventTo";
	public static final String EventPlace = "EventPlace";
	public static final String EventDate = "EventDate";
	public static final String GoingStatus = "GoingStatus";
	public static final String IsEventRead = "IsEventRead";
	public static final String Longitude = "Longitude";
	public static final String Latitude = "Latitude";
	public static final String EventType = "EventType";
	public static final String EventStatus = "EventStatus";
	public static final String TABLE_EVENT = "EVENTS";

	private static final String EVENT_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_EVENT + " (" + EventId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + EventRealId
			+ " INTEGER, " + EventName + " TEXT NOT NULL, " + EventDesc
			+ " TEXT NOT NULL, " + EventFrom + " INTEGER NOT NULL, " + EventTo
			+ " INTEGER NOT NULL, " + EventPlace + " TEXT NOT NULL, "
			+ EventDate + " TEXT NOT NULL, " + Longitude + " TEXT, " + Latitude
			+ " TEXT, " + GoingStatus + " TEXT NOT NULL, " + IsEventRead
			+ " INTEGER NOT NULL, " + EventStatus + " INTEGER, " + EventType
			+ " TEXT)";

	/**
	 * For Latest DateTime in Messages Log
	 * **/
	public static final String MsgLogId = "MsgLogId";
	public static final String LatestDateMsg = "LatestDateMsg";
	public static final String LogType = "LogType";
	public static final String TABLE_MSGENTRYLOG = "MSGENTRYLOG";

	private static final String MSGENTRYLOG_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_MSGENTRYLOG + " (" + MsgLogId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + LatestDateMsg + " TEXT,"
			+ EmployeeId + " INTEGER NOT NULL," + LogType + " TEXT NOT NULL)";

	public UsersDBHelper(Context context) {
		// super(context);
		super(context, DB_NAME, null, DB_VERSION);
	}

	/**
	 * For Event Participants
	 * **/
	public static final String GoingId = "GoingId";
	public static final String TABLE_EVENTGOING = "EVENTGOING";

	private static final String EVENTGOING_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_EVENTGOING + "(" + GoingId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + EventRealId
			+ " INTEGER NOT NULL, " + EventTo + " INTEGER NOT NULL, "
			+ GoingStatus + " TEXT NOT NULL)";

	/**
	 * For Leave
	 * **/
	public static final String LeaveId = "LeaveId";
	public static final String LeaveRqId = "LeaveRqId";
	public static final String ApplicantId = "ApplicantId";
	public static final String ApprovalId = "ApprovalId";
	public static final String LeaveTypeId = "LeaveTypeId";
	public static final String LeaveStatusId = "LeaveStatusId";
	public static final String Remark = "Remark";
	public static final String IsNotificationSent = "IsNotificationSent";
	public static final String LeaveStartDate = "LeaveStartDate";
	public static final String LeaveEndDate = "LeaveEndDate";
	public static final String LeaveType = "LeaveType";
	public static final String TABLE_LEAVE = "LEAVE";

	private static final String LEAVE_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_LEAVE + " (" + LeaveId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ LeaveRqId + " INTEGER , " + ApplicantId + " INTEGER NOT NULL, "
			+ ApprovalId + " INTEGER NOT NULL, " + LeaveTypeId
			+ " INTEGER NOT NULL, " + LeaveStatusId + " INTEGER NOT NULL, "
			+ Remark + " TEXT, " + LeaveStartDate + " TEXT NOT NULL, "
			+ LeaveEndDate + " TEXT, " + IsNotificationSent
			+ " INTEGER NOT NULL, " + UsersDBHelper.LeaveType + " TEXT)";

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(USERS_CREATE_QUERY);
		Log.v("CREATE USERS TABLE", "" + USERS_CREATE_QUERY);

		db.execSQL(BULLETINS_CREATE_QUERY);
		Log.v("CREATE BULLETINS TABLE", "" + BULLETINS_CREATE_QUERY);

		Log.e("CREATE EMPLOYEE TABLE", "" + EMPLOYEES_CREATE_QUERY);
		db.execSQL(EMPLOYEES_CREATE_QUERY);

		Log.e("CREATE ENTRYLOG TABLE", "" + ENTRYLOG_CREATE_QUERY);
		db.execSQL(ENTRYLOG_CREATE_QUERY);

		Log.e("CREATE MESSAGE TABLE", "" + MESSAGE_CREATE_QUERY);
		db.execSQL(MESSAGE_CREATE_QUERY);

		Log.e("CREATE EVENT TABLE", "" + EVENT_CREATE_QUERY);
		db.execSQL(EVENT_CREATE_QUERY);

		Log.e("CREATE MSGENTRYLOG TABLE", "" + MSGENTRYLOG_CREATE_QUERY);
		db.execSQL(MSGENTRYLOG_CREATE_QUERY);

		Log.e("CREATE EVENTGOING TABLE", "" + EVENTGOING_CREATE_QUERY);
		db.execSQL(EVENTGOING_CREATE_QUERY);

		Log.e("CREATE LEAVE TABLE", "" + LEAVE_CREATE_QUERY);
		db.execSQL(LEAVE_CREATE_QUERY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion) {
			switch (oldVersion) {
			case 1:
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
				onCreate(db);
			case 2:
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
				onCreate(db);
			}
		}

	}

}
