package com.ith.project.sqlite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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
	private EntryLogSQLite entryLogSQLite;

	public EmployeeSQLite(Context context) {
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

	/************************************************************************************
	 * Update the Employee datas according to JSON object
	 * *************************************************************************************/
	public void updateDBEmployees(String employeeFromWS,
			EntryLogSQLite entryLogSQLite) {

		try {
			this.entryLogSQLite = entryLogSQLite;
			JSONObject employeesObj;
			employeesObj = new JSONObject(employeeFromWS);
			JSONArray employeesArr = employeesObj
					.getJSONArray("GetEmployeeListResult");
			/** Calculate the current Date and Time **/
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss",Locale.US);
			String currDate = dateFormat.format(cal.getTime());

			for (int i = 0; i < employeesArr.length(); i++) {

				if (employeesArr.getJSONObject(i).getBoolean("Deleted")) {

					deleteOneEmployee(employeesArr.getJSONObject(i).getInt(
							"EmployeeId"));
					continue;
				}

				/** To check for occurence of quotes ' in the String **/
				String unNormalizedEmployeeRemark = employeesArr.getJSONObject(
						i).getString("Remarks");
				StringBuilder normalizedRemark = new StringBuilder();
				String[] remarkParts = unNormalizedEmployeeRemark.split("'");
				for (int j = 0; j < remarkParts.length; j++) {
					if (j == remarkParts.length - 1)
						normalizedRemark.append(remarkParts[j]);
					else
						normalizedRemark.append(remarkParts[j] + "''");
				}

				String unNormalizedEmployeeDesc = employeesArr.getJSONObject(i)
						.getString("Designation");
				StringBuilder normalizedDesc = new StringBuilder();
				String[] descParts = unNormalizedEmployeeDesc.split("'");
				for (int j = 0; j < descParts.length; j++) {
					if (j == descParts.length - 1)
						normalizedDesc.append(descParts[j]);
					else
						normalizedDesc.append(descParts[j] + "''");
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
								"EmployeeName") + "', '"
						+ employeesArr.getJSONObject(i).getString("Gender")
						+ "', '"
						+ employeesArr.getJSONObject(i).getString("HomePhone")
						+ "', '"
						+ employeesArr.getJSONObject(i).getString("Mobile")
						+ "', '"
						+ employeesArr.getJSONObject(i).getString("Email")
						+ "', '"
						+ employeesArr.getJSONObject(i).getString("Address")
						+ "', '" + normalizedDesc.toString() + "', '"
						+ normalizedRemark.toString() + "', '"
						+ updateDateMod(currDate) + "')";

				Log.v("UPDATE QUERY EmployeeS", "" + updateQuery);
				db.execSQL(updateQuery);
			}
			this.entryLogSQLite.updateEntryLog(currDate);

		} catch (JSONException e) {
			Log.e("JSONException probably because u r updateTODate / OFFLINE",
					"" + e.getMessage());
			e.printStackTrace();
		}
	}

	/*****************************************************************************************
	 * read all Employees rows and populate Arraylist of Employee
	 * ************************************************************************************/
	public ArrayList<Employee> getEmpListFromDB() {

		ArrayList<Employee> tempArrList = new ArrayList<Employee>();

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_EMPLOYEES;

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
			Log.e("NO ROW FOUND at EMPLOYEE TABLE", "Cursor Size is 0 ");
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

		Log.v("DELETE QUERY EmployeeS", "" + deleteQuery);
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

	/*****************************************************************************************
	 * read the Employee of Viewed Item
	 * ************************************************************************************/
	public Employee getViewedEmployee(int employeeId) {

		String readQuery = "SELECT * FROM " + UsersDBHelper.TABLE_EMPLOYEES
				+ " WHERE " + UsersDBHelper.EmployeeId + "=" + employeeId
				+ " ORDER BY " + UsersDBHelper.EmployeeId + " DESC";

		Cursor cursor = db.rawQuery(readQuery, null);
		Log.v("CURSOR Viewed Employee SIZE:", "" + cursor.getCount());

		/**
		 * If the cursor is able to move to First, at least 1 value is present
		 **/
		if (cursor.moveToFirst()) {

			Employee tempEmployee = new Employee();
			while (!cursor.isAfterLast()) {

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

				cursor.moveToNext();
			}

			cursor.close();
			return tempEmployee;
		}
		return null;
	}
}
