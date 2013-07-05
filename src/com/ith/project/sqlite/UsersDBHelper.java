package com.ith.project.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UsersDBHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "EMS.db";
	public static final int DB_VERSION = 1;

	/** For USERS Table **/
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

	/** For BULLETIN Table **/
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

	/** For EMPLOYEES Table **/
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

	/** For Latest DateTime Log **/
	public static final String LogId = "LogId";
	public static final String LatestDate = "LatestDate";
	public static final String TABLE_DATELOG = "DATELOG";

	private static final String DATELOG_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_DATELOG + " (" + LogId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + LatestDate + " TEXT)";

	/** For MESSAGE TABLE **/
	public static final String MessageId = "MessageId";
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
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + MessageSubject
			+ " TEXT NOT NULL, " + MessageDesc + " TEXT NOT NULL, "
			+ MessageFrom + " INTEGER NOT NULL, " + MessageTo + " INTEGER, "
			+ MessageDate + " TEXT NOT NULL, " + MessageRead
			+ " INTEGER NOT NULL, " + MessageType + " TEXT NOT NULL)";

	public UsersDBHelper(Context context) {
		// super(context);
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(USERS_CREATE_QUERY);
		Log.v("CREATE USERS TABLE", "" + USERS_CREATE_QUERY);

		db.execSQL(BULLETINS_CREATE_QUERY);
		Log.v("CREATE BULLETINS TABLE", "" + BULLETINS_CREATE_QUERY);

		Log.e("CREATE EMPLOYEE TABLE", "" + EMPLOYEES_CREATE_QUERY);
		db.execSQL(EMPLOYEES_CREATE_QUERY);

		Log.e("CREATE DATELOG TABLE", "" + DATELOG_CREATE_QUERY);
		db.execSQL(DATELOG_CREATE_QUERY);

		Log.e("CREATE MESSAGE TABLE", "" + MESSAGE_CREATE_QUERY);
		db.execSQL(MESSAGE_CREATE_QUERY);
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
