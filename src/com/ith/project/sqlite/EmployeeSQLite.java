package com.ith.project.sqlite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Employee;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EmployeeSQLite {

	private UsersDBHelper usersDBHelper;
	private SQLiteDatabase db;
	private DateLogSQLite dateLogSQLite;

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
	public void updateDBUsersTableJson(String bulletinFromWS,
			DateLogSQLite dateLogSQLite) {

		try {
			this.dateLogSQLite = dateLogSQLite;
			JSONObject employeesObj;
			employeesObj = new JSONObject(bulletinFromWS);
			JSONArray employeesArr = employeesObj
					.getJSONArray("GetEmployeeListResult");
			/** Calculate the current Date and Time **/
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			String currDate = dateFormat.format(cal.getTime());

			for (int i = 0; i < employeesArr.length(); i++) {

				if (employeesArr.getJSONObject(i).getBoolean("Deleted")) {

					deleteOneEmployee(employeesArr.getJSONObject(i).getInt(
							"EmployeeId"));
					continue;
				}

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
						+ ", "
						+ UsersDBHelper.DateModified
						+ ") VALUES ("
						+ /** (EmployeeId++) **/
						employeesArr.getJSONObject(i).getInt("EmployeeId")
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
						+ "', '" + updateDateMod(currDate) + "')";

				Log.v("UPDATE QUERY BULLETINS", "" + updateQuery);
				db.execSQL(updateQuery);
			}
			// this.dateLogSQLite.openDB();
			this.dateLogSQLite.updateDateLog(currDate);

		} catch (JSONException e) {
			Log.e("JSONException probably because u r updateTODate / OFFLINE",
					"" + e.getMessage());
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
				tempEmployee.setDateModified(cursor.getString(cursor
						.getColumnIndex(UsersDBHelper.DateModified)));

				tempArrList.add(tempEmployee);

				cursor.moveToNext();
			}
			cursor.close();
			return tempArrList;
		} else {
			Log.e("NO ROW FOUND at EMPLOYEE TABLE", "Cursor Size is 0");
		}
		cursor.close();
		return null;
	}

	/*****************************************************************************************
	 * delete From sqlite deleted employees
	 * ************************************************************************************/
	public void deleteEmployees(ArrayList<Employee> delArr) {

		Integer[] empId = new Integer[delArr.size()];
		for (int i = 0; i < delArr.size(); i++) {
			empId[i] = delArr.get(i).getEmployeeId();

			deleteOneEmployee(empId[i]);
		}
	}

	private void deleteOneEmployee(int empId) {
		String deleteQuery = "DELETE FROM " + UsersDBHelper.TABLE_EMPLOYEES
				+ " WHERE " + UsersDBHelper.EmployeeId + " = " + empId;

		Log.v("DELETE QUERY BULLETINS", "" + deleteQuery);
		db.execSQL(deleteQuery);
	}

	/*****************************************************************************************
	 * update the DateModified acc to the latest time
	 * ************************************************************************************/
	public String updateDateMod(String currDate) {

		String emptyChkQuery = "SELECT * FROM " + UsersDBHelper.TABLE_EMPLOYEES;
		Cursor cursor = db.rawQuery(emptyChkQuery, null);
		/** If not sync yet **/
		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		} else {
			cursor.close();
			return currDate;
		}
	}

	/*****************************************************************************************
	 * Retrieve the EmployeeName from EmployeeID
	 * ************************************************************************************/
	public String getEmpName(int EmployeeId) {
		String EmpNameQuery = "SELECT " + UsersDBHelper.EmployeeName + " FROM "
				+ UsersDBHelper.TABLE_EMPLOYEES + " WHERE "
				+ UsersDBHelper.EmployeeId + " = " + EmployeeId;
		String empName = null;
		Cursor cursor = db.rawQuery(EmpNameQuery, null);
		if (cursor.moveToFirst())
			empName = cursor.getString(cursor
					.getColumnIndex(UsersDBHelper.EmployeeName));
		return empName;

	}
}
