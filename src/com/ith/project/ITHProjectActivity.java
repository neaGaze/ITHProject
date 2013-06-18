package com.ith.project;

import org.json.JSONObject;
import com.ith.project.sdcard.LoginLocal;
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
	private final String url = "http://192.168.100.2/EMSWebService/Service1.svc/json/Login";

	private Button LoginBut;
	private EditText uName, pwd;
	private HttpConnection conn;
	private LoginLocal loginLocal;
	private LoginAuthentication auth;
	private String uname, pass;
	private ProgressDialog pdialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ProgressDialog pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();
		setContentView(R.layout.login_screen);
		pdialog.cancel();
		initialize();

	}

	/**************************************************************************************
	 * Initialize the values
	 * *************************************************************************************/
	private void initialize() {
		uName = (EditText) findViewById(R.id.editText1);
		pwd = (EditText) findViewById(R.id.editText2);
		LoginBut = (Button) findViewById(R.id.button1);
		pdialog = new ProgressDialog(ITHProjectActivity.this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		LoginBut.setOnClickListener(this);
	}

	/*********************************************************************************
	 * Called when the Login Button is Clicked
	 * ******************************************************************************/
	public synchronized void onClick(View arg0) {

		pdialog.show();

		Thread thread = new Thread(new Runnable() {

			public synchronized void run() {

				uname = uName.getText().toString();
				pass = pwd.getText().toString();
				JSONObject tempObject;
				conn = HttpConnection.getSingletonConn();
				// conn = new HttpConnection(url);
				loginLocal = new LoginLocal();
				auth = new LoginAuthentication();
				tempObject = auth.jsonFormValues(uname, pass);
				// auth.execute(tempObject);
				try {
					ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

					if (conMgr.getActiveNetworkInfo() != null
							&& conMgr.getActiveNetworkInfo().isAvailable()
							&& conMgr.getActiveNetworkInfo().isConnected()
							&& netInfo != null && netInfo.isConnected())

					{

						JSONObject jsonRemoteWebservice = new JSONObject(conn
								.getJSONFromUrl(tempObject, url));

						loginLocal.updateLocalFiles(jsonRemoteWebservice,
								tempObject); // tempObject contains Username and
												// password
					}

				} catch (Exception e) {
					Log.e("JSONException", "" + e.getMessage());
					// pdialog.dismiss();
					e.printStackTrace();
				}

				JSONObject jsonRemote = loginLocal.getJSONFromLocal(tempObject);

				auth.setFlagFromAuth(jsonRemote);

				runOnUiThread(new Runnable() {
					public void run() {
						Log.v("Login status", "" + auth.getAuthStatus());
						if (auth.getAuthStatus()) {

							Log.v("loginStatus @main Thread", "");
							auth.setValues();
							/** here store locally for offline data **/
							// LoginLocal loginLocal = new LoginLocal();
							JSONObject jsonLocal = loginLocal
									.createJSON4LoginLocal(uname, pass,
											auth.getUserLoginId(),
											auth.getEmployeeId(),
											auth.getUserId(),
											auth.getUserRoleId());

							loginLocal.writeFile2Sdcard(jsonLocal);

							pdialog.dismiss();
							Intent intent = new Intent(ITHProjectActivity.this,
									ListItemActivity.class);
							intent.putExtra("UserLoginId",
									auth.getUserLoginId());
							ITHProjectActivity.this.startActivity(intent);
							ITHProjectActivity.this.finish();
						} else {
							Toast.makeText(ITHProjectActivity.this,
									"Login Failure", Toast.LENGTH_SHORT).show();
						}
					}
				});

			}

		});

		thread.start();
	}

	/****************************************************************************************
	 * update loginStatus
	 * ***************************************************************************************/
	public static synchronized void updateLoginStatus(boolean tmpStatus) {
		loginStatus = tmpStatus;
		Log.v("loginStatus @updateLoginstatus()", "" + loginStatus);
	}
}