package edu.cmu.MobAppsafedrive;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.drive.internal.GetMetadataRequest;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
//import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import edu.cmu.utility.Constants;
import edu.cmu.utility.SafeDrivePreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MapDisplayFragment extends SupportMapFragment {

	Location mCurrentLocation;
	private GoogleMap mMap;
	Geocoder geoCoder;
	static String addressText = null;
	Thread mapDisplay;
	Marker dynamicMarker;

	public MapDisplayFragment() {

		SafeDrivePreferences.setPreferences("latitude", "40.434014");
		SafeDrivePreferences.setPreferences("longitude", "-79.994728");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater,container,savedInstanceState);
		//View rootView = inflater.inflate(R.layout.fragment_map, container,
			//	false);

		refreshMapView();
		
		return rootView;
	}

	public void refreshMapView(){
		
		mapDisplay = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				while(true){
					
					new GetCurrentAddressTask(getActivity()).execute();
					
					try {
						Thread.sleep(Constants.jsonParseRate);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});
		
		mapDisplay.start();
	}
	
	public class GetCurrentAddressTask extends AsyncTask<Void, Void, String> {
		Context mContext;		
		
		public GetCurrentAddressTask(Context context) {
			super();
			mContext = context;		
		}		 
		    
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub

			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			double latitude = Double.valueOf(Constants.SAFE_SPEED_LAT);
			double longitude = Double.valueOf(Constants.SAFE_SPEED_LONG);					

			if (SafeDrivePreferences.preferences.contains("latitude")) {
				latitude = Double.valueOf(SafeDrivePreferences.preferences
						.getString("latitude", Constants.SAFE_SPEED_LAT));
			}

			if (SafeDrivePreferences.preferences.contains("longitude")) {
				longitude = Double.valueOf(SafeDrivePreferences.preferences
						.getString("longitude", Constants.SAFE_SPEED_LONG));
			}
			// Get the current location from the input parameter list
			//
			// Create a list to contain the result address
			List<Address> addresses = null;
			try {
				/*
				 * Return 1 address.
				 */				

				addresses = geocoder.getFromLocation(latitude, longitude, 1);
			} catch (IOException e1) {
				Log.e("LocationSampleActivity",
						"IO Exception in getFromLocation()");
				e1.printStackTrace();
				return ("IO Exception trying to get address");
			} catch (IllegalArgumentException e2) {
				// Error message to post in the log
				String errorString = "Illegal arguments "
						+ Double.toString(latitude) + " , "
						+ Double.toString(longitude)
						+ " passed to address service";
				Log.e("LocationSampleActivity", errorString);
				e2.printStackTrace();
				return errorString;
			}

			if (addresses != null && addresses.size() > 0) {
				// Get the first address
				Address address = addresses.get(0);
				/*
				 * Format the first line of address (if available), city, and
				 * country name.
				 */
				String addressText = String.format(
						"%s, %s, %s",
						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "",
						// Locality is usually a city
						address.getLocality(),
						// The country of the address
						address.getCountryName());
				// Return the text
				return addressText;
			} else {
				return "Failure";
			}
		}

		public void onPostExecute(String address) {
			// Set activity indicator visibility to "gone"

			if (!address.equals("Failure")) {
				
				addressText = address;
				displayCurrentLocation();				
			}

		}
		
		
	}
	
	public void displayCurrentLocation(){				

		double lat = Double.valueOf(SafeDrivePreferences.preferences
				.getString("latitude", Constants.SAFE_SPEED_LAT));
		double longt = Double.valueOf(SafeDrivePreferences.preferences
				.getString("longitude",
						Constants.SAFE_SPEED_LONG));
		
		LatLng latLng = new LatLng(lat,longt);		
		
		if(mMap != null){
			mMap.clear();
		}
		
		mMap = getMap();		
				
		dynamicMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(
				"Current Location"));

		CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(
				latLng, 15);
		mMap.animateCamera(yourLocation);		
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				// TODO Auto-generated method stub
				
				marker.setSnippet(addressText);
				marker.showInfoWindow();
				return true;
			}
		});			

	}
	
	public GetCurrentAddressTask getCurrentAddressTaskObject(){
		
		return new GetCurrentAddressTask(getActivity());
	}	
}
