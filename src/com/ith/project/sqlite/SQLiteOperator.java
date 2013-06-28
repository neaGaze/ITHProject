package com.ith.project.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class SQLiteOperator {

	protected DBHelper dbHelper;
	protected SQLiteDatabase db;
	
	public SQLiteOperator(Context context, DBHelper dbHelper) {
		this.dbHelper = dbHelper;
		//this.dbHelper = new 
	}
	
	/***********************************************************************************
	 * To instantiate SQLiteDatabase with writable mode
	 * ************************************************************************************/
	public void openDB() {
		db = dbHelper.getWritableDatabase();
	}

	/***********************************************************************************
	 * To close the DBHelper class
	 * ************************************************************************************/
	public void closeDB() {
		dbHelper.close();
	}
}
