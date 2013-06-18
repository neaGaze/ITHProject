package com.ith.project;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
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
import android.widget.Toast;

public class BulletinViewActivity extends Activity implements OnClickListener {

	private TextView bulletinTitle;
	private TextView bulletinDesc;
	private TextView bulletinAuthor;
	private TextView bulletinDate;
	private ImageButton menuButton;
	private ImageButton BulletinButton;
	private ImageButton homeButton;
	private ProgressDialog pdialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.bulletin_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		init();

	}

	@Override
	public void onPause() {
		super.onPause();
		pdialog.dismiss();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		pdialog.dismiss();
	}

	private void init() {

		/** To remove add Bulletin for normal users **/
		modifyBulletinAdd4Admin(LoginAuthentication.getUserRoleId());

		LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayoutBulletin);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.bulletin_view, lin, false);
		Bundle bundle = getIntent().getExtras();
		int position = bundle.getInt("PositionOfBulletin");

		Log.v("bulletinTitle", ""
				+ ListItemActivity.getBulletinArrayList().get(position)
						.getTitle());

		bulletinTitle = (TextView) findViewById(R.id.editTextBulletinTitleView);
		bulletinTitle.setText(ListItemActivity.getBulletinArrayList()
				.get(position).getTitle());

		bulletinDesc = (TextView) findViewById(R.id.editTextBulletinDescView);
		bulletinDesc.setText(ListItemActivity.getBulletinArrayList()
				.get(position).getDescription());

		bulletinAuthor = (TextView) findViewById(R.id.editTextBulletinAuthor);
		bulletinAuthor.setText(ListItemActivity.getBulletinArrayList()
				.get(position).getEmployeeName());

		bulletinDate = (TextView) findViewById(R.id.editTextBulletinDate);
		bulletinDate.setText(ListItemActivity.getBulletinArrayList()
				.get(position).getDate()
				+ "  "
				+ ListItemActivity.getBulletinArrayList().get(position)
						.getTime());

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(BulletinViewActivity.this);

		BulletinButton = (ImageButton) findViewById(R.id.bulletin_add_icon);
		BulletinButton.setOnClickListener(BulletinViewActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setOnClickListener(BulletinViewActivity.this);
		
		pdialog.dismiss();
	}

	public void onClick(View v) {
		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(BulletinButton)) {

			pdialog.show();
			// Toast.makeText(this, "Add Bulletin", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, BulletinAddActivity.class);
			this.startActivity(intent);

		} else if (v.equals(homeButton)) {
			Toast.makeText(this, "Home Button", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        // do something on back.
	    	pdialog.show();
	    	this.finish();
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	/*********************************************************************************
	 * Called when the LExit Button is Clicked
	 * ******************************************************************************/
	public void modifyBulletinAdd4Admin(int userRolesId) {
		if (userRolesId == 2)
			findViewById(R.id.bulletin_add_icon).setVisibility(View.INVISIBLE);
	}
}
