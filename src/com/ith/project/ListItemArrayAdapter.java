package com.ith.project;

import java.util.ArrayList;

import com.ith.project.EntityClasses.Bulletin;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/******************************************************************************************
 * A new ArrayAdapter to handle the List View
 * *****************************************************************************************/
public class ListItemArrayAdapter extends ArrayAdapter<Bulletin> {

	private Context cntxt;
	private ArrayList<Bulletin> itemDets;

	public ListItemArrayAdapter(Context context, int textViewResourceId,ArrayList<Bulletin> itemDetails) {
		super(context, textViewResourceId);
		this.cntxt = context;
		this.itemDets = itemDetails;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) cntxt
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view;
	//	if (convertView == null) 
		{
			view = new View(this.cntxt);

			view = inflater.inflate(R.layout.list_items, parent, false);

			parent.setBackgroundColor(Color.rgb(221, 221, 221));

			TextView textView = (TextView) view
					.findViewById(R.id.textViewListDesc);
			textView.setText(this.itemDets.get(position).getTitle());
			textView.setFocusable(false);

			TextView date = (TextView) view.findViewById(R.id.textViewListDate);
			date.setText("Date: "+this.itemDets.get(position).getDate());
			textView.setFocusable(false);

			TextView time = (TextView) view.findViewById(R.id.textViewListTime);
			time.setText("Time: "+this.itemDets.get(position).getTime());
			textView.setFocusable(false);
			inflater = null;
		} //else
			//view = (View) convertView;

		return view;
	}

	public int getCount() {

		return itemDets.size();
	}

	public Bulletin getItem(int arg0) {

		return itemDets.get(arg0);
	}

	public long getItemId(int position) {

		return position;
	}
}