package com.ith.project;

import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.sqlite.LoginSQLite;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	 * **/

	private static volatile boolean loginStatus = false;
	private final String url = "Login";

	private Button LoginBut;
	private EditText uName, pwd;
	private HttpConnection conn;
	private LoginSQLite loginSQLite;
	private LoginAuthentication auth;
	private String uname, pass;
	private ProgressDialog pdialog;
	private JSONObject tempObject;
	private JSONObject jsonRemoteWebservice;
	private JSONObject workingJson = null;
	private static String workingStatus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * ProgressDialog pdialog = new ProgressDialog(this);
		 * pdialog.setCancelable(true); pdialog.setMessage("Loading ....");
		 * pdialog.show();
		 */
		setContentView(R.layout.login_screen);
		/* pdialog.cancel(); */
		initialize();

	}

	@Override
	protected void onPause() {
		if (loginSQLite != null)
			loginSQLite.closeDB();
		/* pdialog.dismiss(); */
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
		/*
		 * pdialog = new ProgressDialog(ITHProjectActivity.this);
		 * pdialog.setCancelable(true); pdialog.setMessage("Loading ....");
		 */
		LoginBut.setOnClickListener(this);
	}

	/*********************************************************************************
	 * Called when the Login Button is Clicked
	 * ******************************************************************************/
	public synchronized void onClick(View arg0) {

		/* pdialog.show(); */

		Thread thread = new Thread(new Runnable() {

			public synchronized void run() {

				uname = uName.getText().toString();
				pass = pwd.getText().toString();
				conn = HttpConnection.getSingletonConn();
				// conn = new HttpConnection(url);

				/** Initialize the loginSQLite **/
				loginSQLite = new LoginSQLite(ITHProjectActivity.this);

				loginSQLite.openDB();

				auth = new LoginAuthentication();
				tempObject = jsonFormValues(uname, pass);
				// auth.execute(tempObject);
				try {
					ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

					if (netInfo != null && netInfo.isAvailable()
							&& netInfo.isConnected()) {

						String connected = conn.getJSONFromUrl(tempObject, url);
						Log.e("Login return", ": " + connected);
						if (/* !connected.equals("") || */connected
								.startsWith("{")) {
							jsonRemoteWebservice = new JSONObject(connected);
							Log.e("Login auth return", "" + connected);
						}
					}

					/** Know that Internet is connected OR not connected **/
					if (jsonRemoteWebservice != null) {
						workingStatus = "Online";
						if (jsonRemoteWebservice
								.getBoolean("AutheticationStatus") == true) {
							// loginLocal.updateLocalFiles(jsonRemoteWebservice,tempObject);
							// tempObject contains Username and password
							loginSQLite.updateDBUsersTableJson(
									jsonRemoteWebservice, tempObject);
						} else {
							Log.e("Web service returned false authentication",
									"Please check your uname and pwd");
						}

					} else {
						workingStatus = "Offline";
					}

					/**
					 * Always read from local regardless of how the connection
					 * is done
					 **/
					// workingJson = loginLocal.getJSONFromLocal(tempObject);
					workingJson = loginSQLite.getJSONFromDB(tempObject);

				} catch (Exception e) {
					Log.e("Exception caught", "" + e.getMessage());
					// pdialog.dismiss();
					e.printStackTrace();
				}

				Log.e("Working Json", "is: " + workingJson.toString());
				auth.setFlagFromAuth(workingJson);

				runOnUiThread(new Runnable() {
					public void run() {
						Log.e("Login status", ""
								+ LoginAuthentication.AutheticationStatus);
						if (LoginAuthentication.AutheticationStatus) {

							// loginLocal.updateLocalFiles(workingJson,tempObject);
							// tempObject contains Username and password

							/*
							 * loginSQLite.updateDBUsersTableJson(workingJson,
							 * tempObject);
							 */
							auth.setValues(workingJson);
							/** here store locally for offline data **/
							// LoginLocal loginLocal = new LoginLocal();
							/**
							 * JSONObject jsonLocal = loginLocal
							 * .createJSON4LoginLocal( uname, pass,
							 * LoginAuthentication .getUserLoginId(),
							 * LoginAuthentication.getEmployeeId(),
							 * LoginAuthentication.getUserId(),
							 * LoginAuthentication.getUserRoleId());
							 * 
							 * loginLocal.writeFile2Sdcard(jsonLocal);
							 */

							/*
							 * loginSQLite.insertDBUsersTableValues(uname, pass,
							 * LoginAuthentication.UserloginId,
							 * LoginAuthentication.EmployeeId,
							 * LoginAuthentication.UserId,
							 * LoginAuthentication.UserRolesId);
							 */

							Toast.makeText(ITHProjectActivity.this,
									"" + workingStatus, Toast.LENGTH_SHORT)
									.show();
							Intent intent = new Intent(ITHProjectActivity.this,
									ListItemActivity.class);
							intent.putExtra("UserLoginId",
									LoginAuthentication.UserloginId);
							// loginSQLite.closeDB();
							ITHProjectActivity.this.startActivity(intent);
						} else {
							Toast.makeText(ITHProjectActivity.this,
									"Login Failure", Toast.LENGTH_SHORT).show();
							/* pdialog.dismiss(); */
						}
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

	private void finishActivity() {
		this.finish();
	}
	//
	// /****************************************************************************************
	// * update loginStatus
	// *
	// ***************************************************************************************/
	// public static void updateLoginStatus(boolean tmpStatus) {
	// loginStatus = tmpStatus;
	// Log.v("loginStatus @updateLoginstatus()", "" + loginStatus);
	// }

}