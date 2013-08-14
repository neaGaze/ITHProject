package com.ith.project.googlemap;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ith.project.EventAddActivity;
import com.ith.project.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

public class GoogleMapActivity extends FragmentActivity implements
		OnMapClickListener {

	private FragmentManager fragmentManager;
	private GoogleMap googleMap;
	private String latitude, longitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.google_map);

		fragmentManager = getSupportFragmentManager();
		SupportMapFragment mySupportMapFragment = (SupportMapFragment) fragmentManager
				.findFragmentById(R.id.map);
		googleMap = mySupportMapFragment.getMap();

		googleMap.setMyLocationEnabled(true);

		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

		googleMap.setOnMapClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());

		if (resultCode == ConnectionResult.SUCCESS) {
			Toast.makeText(getApplicationContext(),
					"isGooglePlayServicesAvailable SUCCESS", Toast.LENGTH_LONG)
					.show();
		} else {
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
		}

	}

	public void onMapClick(LatLng point) {

		MarkerOptions markerOptions = new MarkerOptions()
				.position(point)
				.title("Event is here")
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_RED));

		googleMap.addMarker(markerOptions);

		Toast.makeText(getApplicationContext(),
				"New marker added @" + point.toString(), Toast.LENGTH_SHORT)
				.show();
		Log.e("Marker pinned @", "" + point.toString());
		Log.e("Latitude:", "" + point.toString());

		latitude = Double.toString(point.latitude);
		longitude = Double.toString(point.longitude);

		Log.e("lat/Log before return", "is: " + latitude + " & " + longitude);

		EventAddActivity.latitude = latitude;
		EventAddActivity.longitude = longitude;

		GoogleMapActivity.this.finish();

	}
}
