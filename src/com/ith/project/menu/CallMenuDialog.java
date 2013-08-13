package com.ith.project.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ith.project.BulletinAddActivity;
import com.ith.project.BulletinViewActivity;
import com.ith.project.EmployeeAddActivity;
import com.ith.project.EmployeeEditActivity;
import com.ith.project.EmployeeListActivity;
import com.ith.project.EmployeeViewActivity;
import com.ith.project.EventAddActivity;
import com.ith.project.LeaveFormActivity;
import com.ith.project.ListItemActivity;
import com.ith.project.MessageAddActivity;
import com.ith.project.MessageListActivity;
import com.ith.project.R;
import com.ith.project.EntityClasses.LoginAuthentication;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CallMenuDialog {

	public static boolean Exit;

	private Context context;
	private LinearLayout linLayoutMenu;
	private Dialog dialog;
	private ListView menuListView;
	private CustomMenuListAdapter menuAdapter;
	private ProgressDialog pdialog;
	private HashMap<String, String> menuItems;

	public CallMenuDialog(Context context,/* ProgressDialog pdialog, */
			Dialog dialog, HashMap<String, String> menuItems) {
		this.context = context;
		// this.pdialog = pdialog;
		// this.pdialog = new ProgressDialog(context);
		this.dialog = dialog;
		this.menuItems = menuItems;
		init();
	}

	public CallMenuDialog(Context context, ProgressDialog pdialog,
			Dialog dialog, HashMap<String, String> menuItems) {
		this.context = context;
		// this.pdialog = pdialog;
		this.pdialog = pdialog;
		this.dialog = dialog;
		this.menuItems = menuItems;
		init();
	}

	private void init() {

		Exit = false;
		LayoutInflater menuInflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		linLayoutMenu = (LinearLayout) ((Activity) this.context)
				.findViewById(R.id.linearLayoutCustomMenu_2);
		// LinearLayout linLayoutMenu = new LinearLayout(this);
		menuInflater.inflate(R.layout.menu_list_view, linLayoutMenu, false);

		/** To bring front the Dialog box **/
		dialog = new Dialog(this.context, R.style.mydialogstyle);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);

		/** To set the alignment of the Dialog box in the screen **/
		WindowManager.LayoutParams WMLP = dialog.getWindow().getAttributes();
		WMLP.x = ((Activity) this.context).getWindowManager()
				.getDefaultDisplay().getWidth();
		WMLP.gravity = Gravity.TOP;
		WMLP.verticalMargin = 0.08f; // To put it below header
		dialog.getWindow().setAttributes(WMLP);

		/** To set the dialog box with the List layout in the android xml **/
		dialog.setContentView(R.layout.menu_list_view);

		menuListView = (ListView) dialog.findViewById(R.id.listView2);

		/** make an arrayList of items to display at the CustomMenu **/
		ArrayList<CustomMenu> tempArrList = new ArrayList<CustomMenu>();

		/** To remove add Bulletin for normal users **/
		Iterator<Entry<String, String>> iterator = menuItems.entrySet()
				.iterator();
		while (iterator.hasNext()) {

			Map.Entry<String, String> mapValues = iterator.next();
			if (LoginAuthentication.UserRolesId == 2) {

				String menuTitle = mapValues.getKey();

				if (menuTitle.equals("Add Message")
						|| menuTitle.equals("Add Bulletin")
						|| menuTitle.equals("Add Employee")
						|| menuTitle.equals("Delete Employee")
						|| menuTitle.equals("Edit Contents")) {

					// tempArrList.remove(menuTitle);
					continue;
				}
				tempArrList.add(setMenuItems(mapValues.getKey(),
						mapValues.getValue()));
			} else
				tempArrList.add(setMenuItems(mapValues.getKey(),
						mapValues.getValue()));

		}

		menuAdapter = new CustomMenuListAdapter(this.context,
				R.layout.custom_menu_2, tempArrList);
		menuListView.setAdapter(menuAdapter);
		dialog.show();
		menuListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {

				TextView c = (TextView) view
						.findViewById(R.id.textViewCustomMenu_2);
				String keyword = c.getText().toString();

				/** When "Add Message" menu item is pressed **/
				if (keyword.equals("Add Message")) {
					if (!EmployeeListActivity.getConnFlag()) {
						/* pdialog.show(); */
						((Activity) CallMenuDialog.this.context).finish();
						Intent intent = new Intent(CallMenuDialog.this.context,
								BulletinAddActivity.class);
						CallMenuDialog.this.context.startActivity(intent);
					} else {
						Toast.makeText(CallMenuDialog.this.context,
								"Oops ! Can't add while you're Offline",
								Toast.LENGTH_SHORT).show();
						Log.e("Attempted Message Add While Offline",
								"Go Online and then try");
					}
				}
				/** When "Add Bulletin" menu item is pressed **/
				if (keyword.equals("Add Bulletin")) {
					if (!ListItemActivity.getConnFlag()) {
						/* pdialog.show(); */
						((Activity) CallMenuDialog.this.context).finish();
						Intent intent = new Intent(CallMenuDialog.this.context,
								BulletinAddActivity.class);
						CallMenuDialog.this.context.startActivity(intent);
					} else {
						Toast.makeText(CallMenuDialog.this.context,
								"Oops ! Can't add while you're Offline",
								Toast.LENGTH_SHORT).show();
						Log.e("Attempted Bulletin Add While Offline",
								"Go Online and then try");
					}
				}
				/** When "Add Employee" menu item is pressed **/
				if (keyword.equals("Add Employee")) {
					if (!EmployeeListActivity.getConnFlag()) {
						/* pdialog.show(); */
						Intent intent = new Intent(CallMenuDialog.this.context,
								EmployeeAddActivity.class);
						CallMenuDialog.this.context.startActivity(intent);
					} else {
						Toast.makeText(CallMenuDialog.this.context,
								"Oops ! Can't add while you're Offline",
								Toast.LENGTH_SHORT).show();
						Log.e("Attempted Employee Add While Offline",
								"Go Online and then try");
					}
				}
				/** When "Edit Employee" menu item is pressed **/
				else if (keyword.equals("Edit Contents")) {
					if (!EmployeeListActivity.getConnFlag()) {
						/* pdialog.show(); */
						Intent intent = new Intent(CallMenuDialog.this.context,
								EmployeeEditActivity.class);
						intent.putExtra("PositionOfEmployeeEdit",
								EmployeeViewActivity.getPosition());
						CallMenuDialog.this.context.startActivity(intent);
						// CallMenuDialog.this.context.finish();
					} else {
						Toast.makeText(CallMenuDialog.this.context,
								"Oops ! Can't edit while you're Offline",
								Toast.LENGTH_SHORT).show();
						Log.e("Attempted Employee Edit While Offline",
								"Go Online and then try");
					}
				} else if (keyword.equals("Delete Employee")) {

					// ((EmployeeListActivity)
					// CallMenuDialog.this.context).deleteEmployee();
					((EmployeeListActivity) CallMenuDialog.this.context)
							.deleteDialog();

				} else if (keyword.equals("Delete Messages")) {

					// ((EmployeeListActivity)
					// CallMenuDialog.this.context).deleteEmployee();
					((MessageListActivity) CallMenuDialog.this.context)
							.deleteMessages();

				} else if (keyword.equals("Send Web Message")) {
					// if (!EmployeeListActivity.getConnFlag()) {
					/* pdialog.show(); */
					Intent intent = new Intent(CallMenuDialog.this.context,
							MessageAddActivity.class);
					CallMenuDialog.this.context.startActivity(intent);
					// } else {

					// Log.e("Attempted Web message send While Offline",
					// "Go Online and then try");
					// }
				} else if (keyword.equals("Send SMS")) {

					/* pdialog.show(); */
					int empPos = EmployeeViewActivity.getPosition();
					Log.e("Position of emp: ", "" + empPos);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.putExtra("address", EmployeeListActivity
							.getEmployeeArrayList().get(empPos).getMobile());
					intent.setType("vnd.android-dir/mms-sms");
					CallMenuDialog.this.context.startActivity(intent);

				} else if (keyword.equals("Phone Call")) {
					/* pdialog.show(); */
				} else if (keyword.equals("Add Events")) {
					// if (!EmployeeListActivity.getConnFlag()) {
					/* pdialog.show(); */
					Intent intent = new Intent(CallMenuDialog.this.context,
							EventAddActivity.class);
					CallMenuDialog.this.context.startActivity(intent);
					// } else {

					// Log.e("Attempted Web message send While Offline",
					// "Go Online and then try");
					// }
				} else if (keyword.equals("Fill Form")) {
					Intent intent = new Intent(CallMenuDialog.this.context,
							LeaveFormActivity.class);
					CallMenuDialog.this.context.startActivity(intent);

				} else if (keyword.equals("Preferences")) {
					/* pdialog.show(); */
					((MessageListActivity) CallMenuDialog.this.context)
							.showPreferencesDialog();
				}
				/** When "Exit" menu item is pressed **/
				else if (keyword.equals("Exit")) {
					/* pdialog.show(); */
					((Activity) CallMenuDialog.this.context).finish();
					Intent intent = new Intent(CallMenuDialog.this.context,
							ListItemActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("Exit", "exit_now");
					Exit = true;
					CallMenuDialog.this.context.startActivity(intent);
					// ListItemActivity.getListItemActivityInstance().finish();
				}
				dialog.dismiss();
			}
		});

		/* dialog.show(); */

	}

	/****************************************************************************
	 * When we have to set Menu Items in the ArrayList
	 *************************************************************************/
	public CustomMenu setMenuItems(String menuString, String menuIcon) {

		CustomMenu menu = new CustomMenu(menuString, menuIcon);
		menu.setValues(menuString, menuIcon);

		return menu;
	}

}
