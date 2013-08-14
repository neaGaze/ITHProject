package com.ith.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.ith.project.EntityClasses.Employee;
import com.ith.project.EntityClasses.LoginAuthentication;
import com.ith.project.EntityClasses.Message;
import com.ith.project.connection.HttpConnection;
import com.ith.project.sqlite.EmployeeSQLite;
import com.ith.project.sqlite.MessageSQLite;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MessageAddActivity extends Activity implements OnClickListener,
		TextWatcher {

	private final String url = "SendMessage";

	private EditText messageTo, searchBox;
	private EditText messageTitle;
	private EditText messageDesc;
	private Button messageSubmit, empSelectBut;
	private Message messageAdd;
	private String msgTitle, msgDesc;
	private int msgFrom;
	private Integer[] receiversId;
	private JSONObject insertMessage;
	private HttpConnection conn;
	private ImageButton menuButton, empSearch;
	private ImageButton homeButton;
	private Dialog dialog;
	private Dialog empSelectDialog;
	private ListView empListView;
	private static int employeeCount;
	private static EmployeeListItemArrayAdapter listItemArrAdapter;
	private EmployeeSQLite employeeSQLite;
	private MessageSQLite messageSQLite;
	private static ArrayList<Employee> itemDetails;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.message_add);
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

		messageAdd = new Message();

		messageTo = (EditText) findViewById(R.id.editTextMessageTo);
		empSearch = (ImageButton) findViewById(R.id.imageButtonEmployeeSearch);
		messageTitle = (EditText) findViewById(R.id.editTextMessageTitle);
		messageDesc = (EditText) findViewById(R.id.editTextMessageDesc);
		messageSubmit = (Button) findViewById(R.id.buttonMessageSubmit);

		/** To set the Email of persons in the "MessageTo" field **/
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.linearLayoutAddMessage);
		layoutInflater.inflate(R.layout.message_add, linLayout, false);

		ArrayList<Employee> selectedEmployees = EmployeeListActivity
				.getSelected();
		String numbers = getEmployeeName(selectedEmployees);

		/** To set the default Names and make it UnEditable **/
		messageTo.setText(numbers);
		messageTo.setFocusable(false);
		messageTo.setClickable(true);
		messageTo.setFocusableInTouchMode(true);

		empSearch.setOnClickListener(this);

		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setOnClickListener(MessageAddActivity.this);

		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setVisibility(View.GONE);
		homeButton.setOnClickListener(MessageAddActivity.this);

		messageSubmit.setOnClickListener(this);

		EmployeeListActivity.clearEmployeeChecked();

		/** To open up the Database and query for the EmployeeList **/
		employeeSQLite = new EmployeeSQLite(MessageAddActivity.this);
		if (!employeeSQLite.isOpen())
			employeeSQLite.openDB();
		itemDetails = employeeSQLite.getEmpListFromDB();

		/**
		 * Here returnItemDetails is required to store the searched list. By
		 * default it is always equals to itemDetails
		 **/
		employeeSQLite.closeDB();

	}

	/** TO set the selectedEmployee list which are checked **/
	public String getEmployeeName(ArrayList<Employee> selectedEmployees) {

		String[] receiversName = new String[selectedEmployees.size()];
		receiversId = new Integer[selectedEmployees.size()];
		StringBuilder str = new StringBuilder();
		String numbers = null;

		for (int i = 0; i < selectedEmployees.size(); i++) {
			receiversName[i] = selectedEmployees.get(i).getEmployeeName();
			receiversId[i] = selectedEmployees.get(i).getEmployeeId();
			if (i == (selectedEmployees.size() - 1))
				numbers = str.append(receiversName[i]).toString();
			else
				numbers = str.append(receiversName[i]).append("; ").toString();
		}
		return numbers;
	}

	public void onClick(View v) {

		if (v.equals(menuButton)) {
			Intent intent = new Intent(this, GridItemActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			this.finish();
		} else if (v.equals(homeButton)) {

		} else if (v.equals(empSearch)) {

			callEmployeeSearch();

		}
		/** This is called for sending the message to the web service **/
		else {
			msgFrom = LoginAuthentication.EmployeeId;
			msgTitle = messageTitle.getText().toString();
			msgDesc = messageDesc.getText().toString();

			/** Make an inquiry json object **/
			convertNamesToId();
			insertMessage = messageAdd.makeNewMessageJSON(msgFrom, receiversId,
					msgTitle, msgDesc);
			Log.v("insertMessage status", "" + insertMessage.toString());

			/** send the JSONObject to update the messages in the database **/
			new Thread(new Runnable() {

				public void run() {

					conn = HttpConnection.getSingletonConn();

					Log.e("insertStringJson", "" + insertMessage.toString());
					String insertStatusStr = conn.getJSONFromUrl(insertMessage,
							url);
					Log.e("Here comes insertStatusStr", "" + insertStatusStr);
					messageSQLite = new MessageSQLite(MessageAddActivity.this);
					messageSQLite.openDB();
					try {

						JSONObject insertStatusJson = new JSONObject(
								insertStatusStr);

						boolean insertStatus = (Boolean) insertStatusJson
								.get("SendMessageResult");

						if (insertStatus) {

							Log.e("Messages has been sent",
									"MESSAGE sent successfully !!!");
						} else {

							messageSQLite.saveMsgDraft(msgFrom, receiversId,
									msgTitle, msgDesc, null, 0, "msgPending");

							Log.e("Problem Sending Message ",
									"Msg saved as draft" + insertStatus);
						}

					} catch (JSONException e) {
						/**
						 * Save the typed msgs as draft in sqlite if connection
						 * return is *JPT*
						 **/

						messageSQLite.saveMsgDraft(msgFrom, receiversId,
								msgTitle, msgDesc, "", 0, "msgPending");

						Log.e("JSONException while sending msgs",
								"" + e.getMessage());
						Log.e("Messges Saved as Draft",
								"Draft msgs will be sent next time");
						e.printStackTrace();
					}
					messageSQLite.closeDB();

					runOnUiThread(new Runnable() {

						public void run() {
							Intent intent = new Intent(MessageAddActivity.this,
									MessageListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);

						}
					});
				}

			}).start();
		}

	}

	/**
	 * To read the names of Employees in the "TO" Field and convert into
	 * corresponding Id and prevent repeat of names
	 **/
	private void convertNamesToId() {
		String namesInMsgTo = messageTo.getText().toString();
		String[] employeeNames = namesInMsgTo.split("; ");
		HashMap<String, Integer> hMap = new HashMap<String, Integer>();
		for (String oneName : employeeNames) {
			for (int i = 0; i < employeeCount; i++) {
				if (oneName.equals(itemDetails.get(i).getEmployeeName())) {
					hMap.put(oneName, itemDetails.get(i).getEmployeeId());
					break;
				}
			}
		}
		int i = 0;
		Log.e("Size of hMap", "Number or Receivers: " + hMap.size());
		receiversId = new Integer[hMap.size()];
		Iterator<Entry<String, Integer>> hMapIterator = hMap.entrySet()
				.iterator();
		while (hMapIterator.hasNext()) {

			Map.Entry<String, Integer> value = (Map.Entry<String, Integer>) hMapIterator
					.next();
			receiversId[i++] = value.getValue();
			Log.e("Msg Send Names",
					"" + value.getKey() + " : " + value.getValue());
		}

	}

	/******************************************************************************************
	 * To search the required Employees
	 * *****************************************************************************************/
	private void callEmployeeSearch() {
		LayoutInflater menuInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linLayoutEmpListItem = null;

		/** To bring front the Dialog box **/
		empSelectDialog = new Dialog(this);
		empSelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		empSelectDialog.setCanceledOnTouchOutside(true);

		menuInflater.inflate(R.layout.employee_list_view, linLayoutEmpListItem,
				false);

		/** To set the alignment of the Dialog box in the screen **/
		WindowManager.LayoutParams WMLP = empSelectDialog.getWindow()
				.getAttributes();

		WMLP.gravity = Gravity.TOP;
		WMLP.verticalMargin = 0.08f; // To put it below header
		empSelectDialog.getWindow().setAttributes(WMLP);

		/** To set the dialog box with the List layout in the android xml **/
		empSelectDialog.setContentView(R.layout.employee_list_view);

		/** To make the dialog width small **/
		ViewGroup parent = (ViewGroup) empSelectDialog
				.findViewById(R.id.linearLayoutEmployeeListView);
		LayoutInflater hawaInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		parent.addView(hawaInflater.inflate(R.layout.employee_list_view, null),
				500, 2);

		/** for initializing EmpSelectBut and searchBox **/
		empSelectBut = (Button) empSelectDialog
				.findViewById(R.id.selectedEmployees);
		empSelectBut.setVisibility(View.VISIBLE);
		empSelectBut.setFocusable(false);
		searchBox = (EditText) empSelectDialog
				.findViewById(R.id.editTextSearch);
		searchBox.setFocusable(true);
		searchBox.setFocusableInTouchMode(true);
		searchBox.addTextChangedListener(this);

		empListView = (ListView) empSelectDialog.findViewById(R.id.listView1);

		/** Add the listener to the list of Employees items **/
		listItemArrAdapter = new EmployeeListItemArrayAdapter(this,
				R.id.linearLayoutEmployeeListItems, itemDetails);
		empListView.setAdapter(listItemArrAdapter);
		employeeCount = listItemArrAdapter.getCount();

		empSelectDialog.show();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/******************************************************************************************
	 * A new ArrayAdapter to handle the List View of Employees
	 * *****************************************************************************************/
	private class EmployeeListItemArrayAdapter extends ArrayAdapter<Employee>
			implements Filterable {

		private Context cntxt;
		private ArrayList<Employee> itemDets;

		public EmployeeListItemArrayAdapter(Context context,
				int textViewResourceId, ArrayList<Employee> itemDetails) {
			super(context, textViewResourceId);
			this.cntxt = context;
			this.itemDets = itemDetails;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) cntxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view;

			view = new View(this.cntxt);

			view = inflater
					.inflate(R.layout.employee_list_items, parent, false);

			parent.setBackgroundColor(Color.rgb(221, 221, 221));

			TextView textView = (TextView) view
					.findViewById(R.id.textViewEmployeeName);
			textView.setText(this.itemDets.get(position).getEmployeeName());
			textView.setFocusable(false);

			TextView location = (TextView) view
					.findViewById(R.id.textViewEmployeeLocation);
			location.setText(this.itemDets.get(position).getAddress());
			location.setFocusable(false);

			CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox1);
			view.setTag(checkBox);

			checkBox.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					CheckBox chkBox = (CheckBox) v;
					Employee emp = (Employee) chkBox.getTag();
					Log.e("CheckBox check@", "" + chkBox.isChecked());
					emp.setChecked(chkBox.isChecked());
				}

			});

			inflater = null;

			Employee employee = itemDets.get(position);
			checkBox.setChecked(employee.getChecked());
			checkBox.setTag(employee);

			/** When Finished ?? button is clicked **/
			empSelectBut.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					empSelectDialog.dismiss();
					ArrayList<Employee> selectedItemDetail = new ArrayList<Employee>();
					for (int i = 0; i < employeeCount; i++) {
						if (itemDetails.get(i).getChecked()) {
							selectedItemDetail.add(itemDetails.get(i));
							Log.e("Checked Items", ""
									+ itemDetails.get(i).getEmployeeName());
						}
					}
					String numbers = getEmployeeName(selectedItemDetail);
					if (numbers == null)
						numbers = "";
					String numberFromMsgTo = messageTo.getText().toString();
					if (!numberFromMsgTo.isEmpty()) {
						if (numbers.equals(""))
							numbers = numberFromMsgTo;
						else
							numbers = numberFromMsgTo + "; " + numbers;
						Log.e("Numbers from MessageTo", "" + numbers);
					}
					messageTo.setText(numbers);
				}
			});

			return view;
		}

		public int getCount() {

			return itemDets.size();
		}

		public Employee getItem(int arg0) {

			return itemDets.get(arg0);
		}

		public long getItemId(int position) {

			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public Filter getFilter() {

			Filter filter = new Filter() {

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {

					FilterResults results = new FilterResults();
					ArrayList<Employee> FilteredItemDetails = new ArrayList<Employee>();

					/** If search string (constraint) is null, display all **/
					if (constraint == null || constraint.length() == 0) {

						results.values = itemDetails;
						results.count = itemDetails.size();
					}
					/** If constraint is not null */
					else {

						constraint = constraint.toString().toLowerCase();
						for (int i = 0; i < itemDetails.size(); i++) {
							Employee dataNames = itemDetails.get(i);
							if (dataNames.getEmployeeName().toLowerCase()
									.contains(constraint.toString())) {
								FilteredItemDetails.add(dataNames);
							}
						}

						results.count = FilteredItemDetails.size();
						results.values = FilteredItemDetails;
					}
					return results;
				}

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {

					itemDets = (ArrayList<Employee>) results.values;
					notifyDataSetChanged();
				}
			};
			return filter;
		}
	}

	/****************************************************************************************
	 * For text change detector
	 * **************************************************************************************/
	public void afterTextChanged(Editable arg0) {

	}

	public void beforeTextChanged(CharSequence s, int start, int before,
			int count) {

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Log.e("OnTExtChanged called", "ok fine !!");

		listItemArrAdapter.getFilter().filter(s.toString().toLowerCase());
		listItemArrAdapter.notifyDataSetChanged();
	}

}
