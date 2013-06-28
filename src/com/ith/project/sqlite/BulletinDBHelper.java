package com.ith.project.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BulletinDBHelper extends DBHelper {

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
			+ BulletinDate+" TEXT NOT NULL, "+EmployeeName+" INTEGER NOT NULL )";

	public BulletinDBHelper(Context context) {
		super(context);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(BULLETINS_CREATE_QUERY);
		Log.v("CREATE BULLETINS TABLE", "" + BULLETINS_CREATE_QUERY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion) {
			switch (oldVersion) {
			case 1:
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_BULLETINS);
				onCreate(db);
			case 2:
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_BULLETINS);
				onCreate(db);
			}
		}

	}
}
