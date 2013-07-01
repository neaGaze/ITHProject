package com.ith.project;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Employee {

	private String EmployeeName, Gender, HomePhone, Mobile, Email, Address,
			Designation, Remarks;
	private boolean Checked = false;
	private int EmployeeId;

	public Employee() {

	}

	/*************************************************************************************
	 * Make a JSONObject out of UserLoginId
	 * ***************************************************************************************/
	public JSONObject getJsonUserLoginId(String UserLoginId) {

		JSONObject tempJsonFile = new JSONObject();

		try {
			tempJsonFile.put("userLoginId", UserLoginId);

		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}

		return tempJsonFile;
	}

	/*************************************************************************************
	 * Make a JSONObject of new Employee that admin adds
	 * ***************************************************************************************/
	public JSONObject makeNewEmployeeJSON(String Name, String gender,
			String homePhone, String mobile, String email, String address,
			String designation, String remarks, String username, String password) {

		JSONObject UserNEmployee = new JSONObject();
		JSONObject tempJsonFile = new JSONObject();
		JSONObject onlyUser = new JSONObject();

		try {

			tempJsonFile.put("EmployeeName", Name);
			tempJsonFile.put("Gender", gender);
			tempJsonFile.put("HomePhone", homePhone);
			tempJsonFile.put("Mobile", mobile);
			tempJsonFile.put("Email", email);
			tempJsonFile.put("Address", address);
			tempJsonFile.put("Designation", designation);
			tempJsonFile.put("Remarks", remarks);

			UserNEmployee.put("employee", tempJsonFile);

			onlyUser.put("Username", username);
			onlyUser.put("Password", password);

			UserNEmployee.put("user", onlyUser);

		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		Log.e("User and Employee JSON", "" + UserNEmployee.toString());
		return UserNEmployee;
	}

	/******************************************************************************************
	 * To set the values of Bulletin
	 * ************************************************************************************/
	public void setValues(JSONObject remoteJson) {
		try {

			EmployeeId = remoteJson.getInt("EmployeeId");
			EmployeeName = remoteJson.getString("EmployeeName");
			Gender = remoteJson.getString("Gender");
			HomePhone = remoteJson.getString("HomePhone");
			Mobile = remoteJson.getString("Mobile");
			Email = remoteJson.getString("Email");
			Address = remoteJson.getString("Address");
			Designation = remoteJson.getString("Designation");
			Remarks = remoteJson.getString("Remarks");

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}
	}

	public int getEmployeeId() {
		return this.EmployeeId;
	}

	public String getEmployeeName() {
		return this.EmployeeName;
	}

	public String getGender() {
		return this.Gender;
	}

	public String getHomePhone() {
		return this.HomePhone;
	}

	public String getMobile() {
		return this.Mobile;
	}

	public String getEmail() {
		return this.Email;
	}

	public String getAddress() {
		return this.Address;
	}

	public String getDesignation() {
		return this.Designation;
	}

	public String getRemarks() {
		return this.Remarks;
	}

	public boolean getChecked() {
		return this.Checked;
	}

	public void setEmployeeId(int empId) {
		this.EmployeeId = empId;
	}

	public void setEmployeeName(String empName) {
		this.EmployeeName = empName;
	}

	public void setGender(String gender) {
		this.Gender = gender;
	}

	public void setHomePhone(String homePhone) {
		this.HomePhone = homePhone;
	}

	public void setMobile(String mobile) {
		this.Mobile = mobile;
	}

	public void setEmail(String email) {
		this.Email = email;
	}

	public void setAddress(String address) {
		this.Address = address;
	}

	public void setDesignation(String designation) {
		this.Designation = designation;
	}

	public void setRemarks(String remarks) {
		this.Remarks = remarks;
	}

	public void setChecked(boolean checked) {
		this.Checked = checked;
	}
}
