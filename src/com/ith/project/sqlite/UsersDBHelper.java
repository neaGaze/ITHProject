package com.ith.project.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UsersDBHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "EIS.db";
	public static final int DB_VERSION = 1;

	/** For USERS Table **/
	public static final String UserId = "UserId";
	public static final String Username = "Username";
	public static final String Password = "Password";
	public static final String UserLoginId = "UserLoginId";
	public static final String UserRolesId = "UserRolesId";
	public static final String EmployeeId = "EmployeeId";
	public static final String DateModified = "DateModified";
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

	private static final String EMPLOYEES_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_EMPLOYEES + " (" + EmployeeId
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + UserId + " INTEGER, "
			+ EmployeeName + " TEXT NOT NULL, " + Gender + " TEXT NOT NULL, "
			+ HomePhone + " TEXT, " + Mobile + " TEXT NOT NULL, " + Email
			+ " TEXT NOT NULL, " + Address + " TEXT NOT NULL, " + Designation
			+ " TEXT NOT NULL, " + Remarks + " TEXT " + ")";

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
