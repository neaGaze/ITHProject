package com.ith.project;

import java.util.HashMap;

import com.ith.project.EntityClasses.Bulletin;
import com.ith.project.menu.CallMenuDialog;
import com.ith.project.sqlite.BulletinSQLite;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BulletinViewActivity extends Activity implements OnClickListener {

	private TextView bulletinTitle;
	private TextView bulletinDesc;
	private TextView bulletinDate;
	private ImageButton menuButton;
	private ImageButton BulletinButton;
	private ImageButton homeButton;
	private BulletinSQLite bulletinSQLite;
	public ProgressDialog pdialog;
	static ListItemArrayAdapter listItemArrAdapter;
	private Dialog dialog;
	private HashMap<String, String> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.bulletin_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (bulletinSQLite != null)
			bulletinSQLite.closeDB();
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void init() {

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutBulletin);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.bulletin_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		int position = bundle.getInt("BulletinId");
		
		bulletinSQLite = new BulletinSQLite(this);
		bulletinSQLite.openDB();
		Bulletin viewedBulletin = bulletinSQLite.getViewedBulletin(position);

		Log.v("bulletinTitle", "" + viewedBulletin.getTitle());

		bulletinTitle = (TextView) findViewById(R.id.editTextBulletinTitleView);
		bulletinTitle.setText(viewedBulletin.getTitle());

		bulletinDesc = (TextView) findViewById(R.id.editTextBulletinDescView);
		bulletinDesc.setText(viewedBulletin.getDescription());

		bulletinDate = (TextView) findViewById(R.id.editTextBulletinDate);
		bulletinDate.setText(viewedBulletin.getDate() + "  "
				+ viewedBulletin.getTime());

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(BulletinViewActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(BulletinViewActivity.this);

		menuItems = new HashMap<String, String>();
	
	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(BulletinButton)) {

			Intent intent = new Intent(this, BulletinAddActivity.class);
			this.startActivity(intent);

		} else if (v.equals(homeButton)) {

			/** Set up the Menu **/

			menuItems.put("Add Bulletin", "add_employee");
			new CallMenuDialog(this, dialog, menuItems);

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

}
