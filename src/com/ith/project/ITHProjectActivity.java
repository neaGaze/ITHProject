package com.ith.project;

import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.sqlite.LoginSQLite;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ITHProjectActivity extends Activity implements OnClickListener {
	/**
	 * @author : neaGaze
	 ***/

	private final String url = "Login";

	private Button LoginBut;
	private EditText uName, pwd;
	private HttpConnection conn;
	private LoginSQLite loginSQLite;
	private LoginAuthentication auth;
	private String uname, pass;
	private JSONObject tempObject;
	private JSONObject jsonRemoteWebservice;
	private JSONObject workingJson = null;
	private static String workingStatus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_screen);
		initialize();
	}

	@Override
	protected void onPause() {
		if (loginSQLite != null)
			loginSQLite.closeDB();

		super.onPause();
		this.finish();
	}

	/**************************************************************************************
	 * Initialize the values
	 * *************************************************************************************/
	private void initialize() {
		uName = (EditText) findViewById(R.id.editText1);
		pwd = (EditText) findViewById(R.id.editText2);
		LoginBut = (Button) findViewById(R.id.button1);

		LoginBut.setOnClickListener(this);
	}

	/*********************************************************************************
	 * Called when the Login Button is Clicked
	 * ******************************************************************************/
	public synchronized void onClick(View arg0) {

		Thread thread = new Thread(new Runnable() {

			public synchronized void run() {

				uname = uName.getText().toString();
				pass = pwd.getText().toString();
				conn = HttpConnection.getSingletonConn();

				/** Initialize the loginSQLite **/
				loginSQLite = new LoginSQLite(ITHProjectActivity.this);
				if (!loginSQLite.isOpen()) {
					loginSQLite.openDB();
				}

				auth = new LoginAuthentication();
				tempObject = jsonFormValues(uname, pass);

				try {

					if (HttpConnection
							.getConnectionAvailable(ITHProjectActivity.this)) {

						String connected = conn.getJSONFromUrl(tempObject, url);
						Log.e("Login return", ": " + connected);
						if (connected.startsWith("{")) {
							jsonRemoteWebservice = new JSONObject(connected);
							Log.e("Login auth return", "" + connected);
						}
					}
				} catch (Exception e) {
					Log.e("Exception caught", "" + e.getMessage());

					e.printStackTrace();
				}

				try {
					/** Know that Internet is connected OR not connected **/
					if (jsonRemoteWebservice != null) {
						workingStatus = "Online";
						if (jsonRemoteWebservice
								.getBoolean("AutheticationStatus") == true) {

							loginSQLite.updateDBUsersTableJson(
									jsonRemoteWebservice, tempObject);
						} else {
							Log.e("Web service returned false authentication",
									"Please check your uname and pwd");
						}

					} else {
						workingStatus = "Offline";
					}

				} catch (Exception e) {
					Log.e("Exception caught", "" + e.getMessage());

					e.printStackTrace();
				}

				/**
				 * Always read from local regardless of how the connection is
				 * done
				 **/
				workingJson = loginSQLite.getJSONFromDB(tempObject);

				Log.e("Working Json", "is: " + workingJson.toString());
				auth.setFlagFromAuth(workingJson);

				runOnUiThread(new Runnable() {
					public void run() {
						Log.e("Login status", ""
								+ LoginAuthentication.AutheticationStatus);
						if (LoginAuthentication.AutheticationStatus) {

							auth.setValues(workingJson);

							Toast.makeText(ITHProjectActivity.this,
									"" + workingStatus, Toast.LENGTH_SHORT)
									.show();
							Intent intent = new Intent(ITHProjectActivity.this,
									ListItemActivity.class);
							intent.putExtra("UserLoginId",
									LoginAuthentication.UserloginId);

							ITHProjectActivity.this.startActivity(intent);
						} else {
							Toast.makeText(ITHProjectActivity.this,
									"Login Failure", Toast.LENGTH_SHORT).show();

						}
						loginSQLite.closeDB();
					}
				});

			}

		});

		thread.start();
	}

	/*************************************************************************************
	 * Make a JSONObject out of username & password
	 * ***************************************************************************************/
	public JSONObject jsonFormValues(String username, String password) {
		JSONObject tempJsonFile = new JSONObject();
		try {
			tempJsonFile.put("Password", password);
			tempJsonFile.put("Username", username);

			Log.e("login info", "" + tempJsonFile.toString());
		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", ":P :P :P");
			e.printStackTrace();
		}
		return tempJsonFile;
	}

}