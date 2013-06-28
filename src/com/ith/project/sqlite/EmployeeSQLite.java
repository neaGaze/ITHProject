package com.ith.project.sqlite;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ith.project.Bulletin;
import com.ith.project.Employee;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EmployeeSQLite {

	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;
	private static int EmployeeId = 0;

	public EmployeeSQLite(Context context) {
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

	/************************************************************************************
	 * Update the Employee datas according to JSON object
	 * *************************************************************************************/
	public void updateDBUsersTableJson(String bulletinFromWS) {
		try {

			JSONObject employeesObj;
			employeesObj = new JSONObject(bulletinFromWS);
			JSONArray employeesArr = employeesObj
					.getJSONArray("GetEmployeeListResult");
			EmployeeId = 0;
			for (int i = 0; i < employeesArr.length(); i++) {

				String updateQuery = "INSERT OR REPLACE INTO "
						+ UsersDBHelper.TABLE_EMPLOYEES
						+ " ( "
						+ UsersDBHelper.EmployeeId
						+ ", "
						+ UsersDBHelper.UserId
						+ ", "
						+ UsersDBHelper.EmployeeName
						+ ", "
						+ UsersDBHelper.Gender
						+ ", "
						+ UsersDBHelper.HomePhone
						+ ", "
						+ UsersDBHelper.Mobile
						+ ", "
						+ UsersDBHelper.Email
						+ ", "
						+ UsersDBHelper.Address
						+ ", "
						+ UsersDBHelper.Designation
						+ ", "
						+ UsersDBHelper.Remarks
						+ ") VALUES ("
						+ (EmployeeId++)
						+ ", "
						+ "0"
						+ ", '"
						+ employeesArr.getJSONObject(i).getString(
								"EmployeeName")
						+ "', '"
						+ employeesArr.getJSONObject(i).getString("Gender")
						+ "', '"
						+ employeesArr.getJSONObject(i).getString("HomePhone")
						+ "', '"
						+ employeesArr.getJSONObject(i).getString("Mobile")
						+ "', '"
						+ employeesArr.getJSONObject(i).getString("Email")
						+ "', '"
						+ employeesArr.getJSONObject(i).getString("Address")
						+ "', '"
						+ employeesArr.getJSONObject(i)
								.getString("Designation") + "', '"
						+ employeesArr.getJSONObject(i).getString("Remarks")
						+ "')";

				Log.v("UPDATE QUERY BULLETINS", "" + updateQuery);
				db.execSQL(updateQuery);
			}
		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	/*****************************************************************************************
	 * read all Employees rows and populate Arraylist of Employee
	 * ************************************************************************************/
	public ArrayList<Employee> getJSONFromDB() {

		ArrayList<Employee> tempArrList = new ArrayList<Employee>();

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_EMPLOYEES;

		// Log.v("SELECT QUERY EMPLOYEES", "" + readQuery);
		Cursor cursor = db.rawQuery(readQuery, null);
		Log.v("CURSOR EMPLOYEES SIZE:", "" + cursor.getCount());

		/**
		 * If the cursor is able to move to First, at least 1 value is present
		 **/
		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {
				Employee tempEmployee = new Employee();

				tempEmployee.setEmployeeId(cursor.getInt(cursor
						.getColumnIndex(UsersDBHelper.EmployeeId)));
				tempEmployee.setEmployeeName(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.EmployeeName)));
				tempEmployee.setGender(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Gender)));
				tempEmployee.setHomePhone(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.HomePhone)));
				tempEmployee.setMobile(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Mobile)));
				tempEmployee.setEmail(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Email)));
				tempEmployee.setAddress(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Address)));
				tempEmployee.setDesignation(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Designation)));
				tempEmployee.setRemarks(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.Remarks)));

				tempArrList.add(tempEmployee);

				cursor.moveToNext();
			}
			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND @ EMPLOYEE TABLE", "Cursor Size is 0");
		}
		cursor.close();
		return null;
	}
}
