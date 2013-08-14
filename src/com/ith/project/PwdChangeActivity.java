package com.ith.project;

import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.LoginSQLite;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

public class PwdChangeActivity extends Activity implements OnClickListener {

	private final String url = "ChangePassword";

	private HttpConnection conn;
	private Dialog dialog;
	private ImageButton menuButton, submitButton;
	private TextView employeeName;
	private EditText oldPwd, newPwd1, newPwd2;
	private EmployeeSQLite employeeSQLite;
	private LoginSQLite loginSQLite;
	private boolean validation, error;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.pwd_change);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (employeeSQLite != null)
			employeeSQLite.closeDB();

		if (dialog != null)
			dialog.dismiss();

		this.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void init() {

		employeeSQLite = new EmployeeSQLite(this);
		if (!employeeSQLite.isOpen())
			employeeSQLite.openDB();

		employeeName = (TextView) findViewById(R.id.textViewUserPwdChange);
		employeeName.setText(employeeSQLite
				.getEmpName(LoginAuthentication.EmployeeId));

		oldPwd = (EditText) findViewById(R.id.EditTextCurrentPwd);
		newPwd1 = (EditText) findViewById(R.id.editTextChangePassword1);
		newPwd2 = (EditText) findViewById(R.id.editTextChangePassword2);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(PwdChangeActivity.this);

		validation = true;
		error = false;
		submitButton = (ImageButton) findViewById(R.id.buttonPwdChange);
		submitButton.setOnClickListener(this);

	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {

			Intent intent = new Intent(PwdChangeActivity.this,
					GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);

		} else if (v.equals(submitButton)) {
			validateFields();
			if (validation) {
				submitPwd();
			}
		}

	}

	private void submitPwd() {

		Thread thread = new Thread(new Runnable() {

			public void run() {

				JSONObject inputJson;
				conn = HttpConnection.getSingletonConn();

				/** Make a json Object out of UserLoginId **/
				inputJson = getPwdChangeJson(LoginAuthentication.UserloginId,
						LoginAuthentication.EmployeeId, newPwd1.getText()
								.toString(), oldPwd.getText().toString());

				Log.v("getPwdChangeinquiry", "" + inputJson.toString());

				/** To establish connection to the web service **/
				String resultFromWS = conn.getJSONFromUrl(inputJson, url);
				Log.e("Pwd Change Result:", "here: " + resultFromWS);

				try {

					JSONObject changeStatusJson = new JSONObject(resultFromWS);
					String changeStatus = (String) changeStatusJson
							.get("ChangePasswordResult");

					if (changeStatus
							.equals("Your Password Have Been Changed Successfully")) {

						Log.e("Pwd has been changed",
								"PASSWORD sent successfully !!!");
						loginSQLite = new LoginSQLite(getApplicationContext());
						loginSQLite.openDB();
						loginSQLite.changePwd(newPwd1.getText().toString(),
								LoginAuthentication.EmployeeId);
						loginSQLite.closeDB();

					} else {

						Log.e("Problem Changing Pwd ", "PWd not changed"
								+ changeStatus);
						error = true;
					}
				} catch (JSONException e) {

					Log.e("JSONException while changing PWd",
							"" + e.getMessage());
					e.printStackTrace();
					error = true;
				}
				runOnUiThread(new Runnable() {

					public void run() {
						Intent intent = new Intent(PwdChangeActivity.this,
								GridItemActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);

					}
				});
			}
		});

		if (validation)
			thread.start();
		if (error)
			Toast.makeText(this, "Passwords Don't Match", Toast.LENGTH_SHORT)
					.show();

	}

	protected JSONObject getPwdChangeJson(String userLoginId, int employeeId,
			String pwd, String oldPwd) {

		JSONObject pwdChangeQuery = new JSONObject();
		try {
			pwdChangeQuery.put("userLoginId", userLoginId);
			pwdChangeQuery.put("passwordChangerEmployeeId",
					new StringBuilder().append(employeeId));
			pwdChangeQuery.put("newPassword", pwd);
			pwdChangeQuery.put("oldPassword", oldPwd);
		} catch (JSONException e) {
			Log.e("JSON EXCEPTION", "" + e.getMessage());
			e.printStackTrace();
		}
		return pwdChangeQuery;

	}

	private void validateFields() {

		if (!(newPwd1.getText().toString().equals(newPwd2.getText().toString()))) {
			validation = false;
			Toast.makeText(this, "New Passwords don't match",
					Toast.LENGTH_SHORT).show();

		} else if (newPwd1.getText() == null || newPwd2.getText() == null
				|| oldPwd.getText() == null || newPwd1.getText().equals("")
				|| newPwd2.getText().equals("") || oldPwd.getText().equals("")) {
			validation = false;
			Toast.makeText(this, "Fields Empty", Toast.LENGTH_SHORT).show();
		} else
			validation = true;

	}
}
