package com.ith.project;

import com.ith.project.sqlite.MessageSQLite;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Window;

public class MessageListActivity extends Activity {

	private ProgressDialog pdialog;
	private Dialog dialog;
	private MessageSQLite messageSQLite;
	private static MessageListActivity context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.list_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		context = this;
		init();

	}

	@Override
	public void onPause() {
		super.onPause();
		messageSQLite.closeDB();
		pdialog.dismiss();
		if (dialog != null)
			dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();
		pdialog.dismiss();
	}
	private void init() {
		// TODO Auto-generated method stub
		
	}
}
