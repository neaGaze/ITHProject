package com.ith.project.menu;

import java.util.ArrayList;
import java.util.List;

import com.ith.project.Bulletin;
import com.ith.project.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomMenuListAdapter extends ArrayAdapter<CustomMenu> {

	private ArrayList<CustomMenu> listDets;
	private Context cntxt;

	public CustomMenuListAdapter(Context context, int textViewResourceId,
			ArrayList<CustomMenu> itemDetails) {
		super(context, textViewResourceId);
		this.listDets = itemDetails;
		this.cntxt = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) cntxt
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view;
		if (convertView == null) {
			view = new View(this.cntxt);

			view = inflater.inflate(R.layout.custom_menu_2, parent, false);

			parent.setBackgroundColor(Color.rgb(221, 221, 221));

			/** For Text of Custom Menu **/
			TextView textView = (TextView) view
					.findViewById(R.id.textViewCustomMenu_2);
			String tempText= this.listDets.get(position).getText().toString();
			textView.setText(tempText);
			textView.setFocusable(false);

			/** For Image of Custom Menu **/
			ImageView imageView = (ImageView) view
					.findViewById(R.id.imageViewCustomMenu_2);
			int id = this.cntxt.getResources().getIdentifier(
					this.listDets.get(position).getImage(),
					// "employee1",
					"drawable",
					this.cntxt.getApplicationContext().getPackageName());
			imageView.setImageResource(id);
			imageView.setFocusable(false);

			inflater = null;
		} else
			view = (View) convertView;

		return view;
	}

	public int getCount() {

		return listDets.size();
	}

	public CustomMenu getItem(int arg0) {

		return listDets.get(arg0);
	}

	public long getItemId(int position) {

		return position;
	}
}
