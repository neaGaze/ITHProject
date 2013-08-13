package com.ith.project.googlemap;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ith.project.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class PinGMapActivity extends FragmentActivity {

	private FragmentManager fragmentManager;
	double latitude = 12.972456;
	double longitude = 20.594504;
	private LatLng latlng;
	private GoogleMap gMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		setContentView(R.layout.google_map);
		
		fragmentManager = getSupportFragmentManager();
		Bundle bundle = getIntent().getExtras();
		latitude = bundle.getDouble("latitude");
		longitude = bundle.getDouble("longitude");
		latlng = new LatLng(latitude, longitude);
		setUpMapIfNeeded(); // Required to check the availability of Maps

	}

	private void setUpMapIfNeeded() {
		if (gMap == null) {
			/*
			 * I avoid Crashing, if Google_Play_Services is not Updated or
			 * Unavailable
			 */
			gMap = ((SupportMapFragment) fragmentManager
					.findFragmentById(R.id.map)).getMap();

			/*
			 * To make sure map is loaded
			 */
			if (gMap != null) {
				setUpMap();
			} else
				Log.e("Google MAp == null",
						"Maybe beacuse Map from xml couldnot be loaded");
		}
	}

	private void setUpMap() {
		/*
		 * Add a Marker Adding marker at latitude and logitude;
		 */
		gMap.addMarker(new MarkerOptions().position(latlng)

		/*
		 * Add Title when clicked on marker
		 */
		.title("Title")
		/*
		 * Add Snippet when clicked on marker
		 */
		.snippet("Success it is haha"));

		/*
		 * NormalMapView
		 */
		gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Normal MapView

		/*
		 * Move Camera to Snippet Location
		 */
		float zoom = 11;
		gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom)); // toPosition,
		// ZoomLevel

	}

}
