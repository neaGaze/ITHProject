package com.ith.project;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Bulletin;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.connection.HttpConnection;
import com.ith.project.menu.CallMenuDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class BulletinAddActivity extends Activity implements OnClickListener {

	private final String url = "InsertBulletins";

	private EditText bulletinTitle;
	private EditText bulletinDesc;
	private Button bulletinSubmit;
	private Bulletin bulletinAdd;
	private String employeeId, title, desc, date;
	private Calendar cal;
	private JSONObject insertBulletin;
	private HttpConnection conn;
	private ImageButton menuButton;
	private ImageButton homeButton;
	private Dialog dialog;
	private HashMap<String, String> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.bulletin_add);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void init() {
		bulletinAdd = new Bulletin();

		bulletinTitle = (EditText) findViewById(R.id.editTextBulletinTitle);
		bulletinDesc = (EditText) findViewById(R.id.editTextBulletinDesc);
		bulletinSubmit = (Button) findViewById(R.id.buttonBulletinSubmit);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(BulletinAddActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(BulletinAddActivity.this);

		bulletinSubmit.setOnClickListener(this);

		menuItems = new HashMap<String, String>();

	}

	public void onClick(View v) {

		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/
			new CallMenuDialog(this, dialog, menuItems);

		}
		/** When Bulletin Add button is CLicked **/
		else {

			employeeId = new StringBuilder().append(
					LoginAuthentication.EmployeeId).toString();
			title = bulletinTitle.getText().toString();
			desc = bulletinDesc.getText().toString();
			date = getCurrentDate();
			Log.d("Bulletin Add Date", "" + date);
			insertBulletin = bulletinAdd.makeNewBulletinJSON(employeeId, title,
					desc);
			Log.v("insertBulletin status", "" + insertBulletin.toString());

			/** send the JSONObject to update the bulletins in the database **/
			new Thread(new Runnable() {

				public void run() {

					conn = HttpConnection.getSingletonConn();

					Log.e("insertStringJson", "" + insertBulletin.toString());
					String insertStatusStr = conn.getJSONFromUrl(
							insertBulletin, url);
					Log.e("Here comes insertStatusStr", "" + insertStatusStr);
					try {

						JSONObject insertStatusJson = new JSONObject(
								insertStatusStr);

						boolean insertStatus = (Boolean) insertStatusJson
								.get("InsertBulletins");

						if (insertStatus) {
							Toast.makeText(BulletinAddActivity.this,
									"Successfully Bulletin Added",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(BulletinAddActivity.this,
									"Problem Adding Bulletin",
									Toast.LENGTH_SHORT).show();

							Log.e("Problem Adding Bulletin ",
									"InsertStatus From Web Service"
											+ insertStatus);
						}

					} catch (JSONException e) {
						Log.e("JSONException", "" + e.getMessage());
						e.printStackTrace();
					}

					runOnUiThread(new Runnable() {

						public void run() {
							BulletinAddActivity.this.finish();
							Intent intent = new Intent(
									BulletinAddActivity.this,
									ListItemActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);

						}
					});
				}

			}).start();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private String getCurrentDate() {
		String formattedDateAndTime;
		cal = Calendar.getInstance();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.US);
		formattedDateAndTime = formatter.format(cal.getTime());

		Log.v("TODAY'S DATE: ", "" + formattedDateAndTime);
		return formattedDateAndTime;
	}
}
