package com.ith.project;

import java.util.ArrayList;

import com.ith.project.R;
import com.ith.project.sdcard.EmployeeLocal;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class EmployeeListActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private static final int MENU_EXIT = 0;
	private final String url = "http://192.168.100.2/EMSWebService/Service1.svc/json/GetEmployees";

	private static ArrayList<Employee> itemDetails;
	private ListView listView;
	private Button ExitBut;
	private ImageButton menuButton;
	private ImageButton EmployeeButton;
	private ImageButton homeButton;
	private HttpConnection conn;
	private EmployeeLocal employeeLocal;
	private Employee employee;
	static ListItemArrayAdapter listItemArrAdapter;
	private static int employeeCount;
	private ProgressDialog pdialog;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.employee_list_view);
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

	/************************************************************************************
	 * Initialize values first
	 * ***********************************************************************************/
	private void init() {
		// TODO Auto-generated method stub
		
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
