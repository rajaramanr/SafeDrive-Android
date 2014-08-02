package edu.cmu.MobAppsafedrive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.lg;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import edu.cmu.Model.CarDataModel;
import edu.cmu.utility.Constants;
import edu.cmu.utility.MySQLiteHelper;
import edu.cmu.utility.SafeDrivePreferences;
import android.support.v7.app.ActionBar;

//import android.support.v7.app.ActionBarActivity;

public class SafeDriveActivity extends ActionBarActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	DashboardFragment dashBoardFragment;
	MapDisplayFragment mapFragment;
	MenuItem bluetoothItem;
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private Set<String> devicesDisc = new HashSet<String>();
	private static final UUID BLUETOOTH_SPP_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805f9b34fb");
	public static final int MESSAGE_READ = 9999;
	Set<String> data = new HashSet<String>();

	BluetoothDevice dev = null;
	Thread carDataParse;
	private AQuery aquery;

	public List<CarDataModel> carDataList = new ArrayList<CarDataModel>();

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// Log.d(CURRENT_CLASS, "Reaching receiver" + " - action : " +
			// action);
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getName().contains("Nexus 7"))
					dev = device;
				devicesDisc.add(device.getName() + " - " + device.getAddress());
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		MySQLiteHelper db = new MySQLiteHelper(this, Constants.DATABASE_NAME,
				null, Constants.DATABASE_VERSION);

		SafeDrivePreferences.preferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		SafeDrivePreferences.setPreferences("SpeedLimit",Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE);
		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		dashBoardFragment = new DashboardFragment();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						if (dashBoardFragment != null) {
							refreshDashboardView(position);
						}

					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		readJsonFromJsonFile();
		parseJsonData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		bluetoothItem = menu.findItem(R.id.item1);

		return true;
	}

	public void parseJsonData() {

		carDataParse = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				Iterator it;
				CarDataModel carDataModel;
				boolean dashThreadStart = false;

				while (true) {

					it = carDataList.iterator();

					Log.d("Inside car thread", "Values over");
					Log.d("First value", "42.291595 " + "-83.237617");
					while (it.hasNext()) {

						carDataModel = (CarDataModel) it.next();

						SafeDrivePreferences.setPreferences("latitude",
								String.valueOf(carDataModel.getLatitude()));
						SafeDrivePreferences.setPreferences("longitude",
								String.valueOf(carDataModel.getLongitude()));
						SafeDrivePreferences.setPreferences("currentSpeed",
								String.valueOf(carDataModel.getCurrentSpeed()));
						Log.d("Inside car thread",
								String.valueOf(carDataModel.getLatitude())
										+ String.valueOf(carDataModel
												.getLongitude()
												+ String.valueOf(carDataModel
														.getCurrentSpeed())));
						asyncJson();						
						
						try {
							Thread.sleep(Constants.jsonParseRate);
							
							/*if(!dashThreadStart){
								dashBoardFragment.refreshView();
								dashThreadStart = true;
							}*/
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			}
		});
		carDataParse.start();
	}

	public void activateBluetooth() {
		Log.d("activateBluetooth", "inside activateBluetooth");

		if (!mBluetoothAdapter.isEnabled()) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		} else {
			Toast.makeText(this, "Looks like you are already connected",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void readJsonFromJsonFile() {

		String json;
		int timeStamp = 0;
		boolean isFirstDataCrossed = false;
		int prevTimeStamp = 0;
		JSONObject jsonObject;
		BufferedReader reader;
		String name;
		double vehicleSpeed = 0.0;
		double vehicleLatitude = 0.0;
		double vehicleLongitude = 0.0;

		try {
			reader = new BufferedReader(new InputStreamReader(getAssets().open(
					"changedLtLg.json"), "UTF-8"));
			while ((json = reader.readLine()) != null) {

				// Instantiate a JSON object from the request response

				jsonObject = new JSONObject(json);

				name = jsonObject.getString("name");
				timeStamp = jsonObject.getInt("timestamp");

				if (name.equals("vehicle_speed")) {

					vehicleSpeed = jsonObject.getDouble("value");
				} else if (name.equals("latitude")) {

					vehicleLatitude = jsonObject.getDouble("value");
				} else if (name.equals("longitude")) {

					vehicleLongitude = jsonObject.getDouble("value");
				}
				Log.d("JSON Data", name);

				if ((timeStamp != prevTimeStamp) && (isFirstDataCrossed)) {
					carDataList.add(new CarDataModel(vehicleSpeed,
							vehicleLatitude, vehicleLongitude));
				}

				prevTimeStamp = timeStamp;
				isFirstDataCrossed = true;

			}

			Log.d("JSON File read", "true");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void discoverDev() {
		Log.d("discoverDev", "inside discoverDev");
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister
												// during onDestroy
		mBluetoothAdapter.startDiscovery();
	}

	public void connectMe() {
		Log.d("connectMe", "inside connectMe");
		new ConnectThread(dev, mBluetoothAdapter).start();
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private BluetoothAdapter adapter;
		private final BluetoothDevice mmDevice;
		private InputStream mmInStream;
		private Handler mHandler = new Handler();

		public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter) {
			BluetoothSocket tmp = null;
			this.adapter = adapter;
			mmDevice = device;

			Log.d("Found device", "" + device);
			try {
				tmp = device
						.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			adapter.cancelDiscovery();
			try {
				Log.d("Main", "Trying to connect");
				mmSocket.connect();
			} catch (IOException connectException) {
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}
			data = manageConnectedSocket(mmSocket);
			try {
				mmSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}

		private Set<String> manageConnectedSocket(BluetoothSocket mmSocket) {
			Log.d("Main", "Connected Hammaya");
			InputStream tmpIn = null;
			try {
				tmpIn = mmSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mmInStream = tmpIn;
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()
			while (true) {
				try {
					bytes = mmInStream.read(buffer);
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();
				} catch (IOException e) {
					break;
				}
				Log.d("Main", "Bytes Received - " + new String(buffer));
				data.add(new String(buffer));

			}
			return data;

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.item1) {
			activateBluetooth();
			discoverDev();

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			connectMe();

			bluetoothItem.setIcon(R.drawable.bluetooth_blue);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		/*
		 * if(tab.getPosition() == 0){
		 * 
		 * dashBoardFragment.displaySpeedLimit(); }
		 */

	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public void refreshDashboardView(int position) {
		if (position == 0) {
			//asyncJson();
			//dashBoardFragment.refreshView();
		} else if (position == 1) {
//			mapFragment.getCurrentAddressTaskObject().execute();
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				// dashBoardFragment = new DashboardFragment();
				return dashBoardFragment;
			case 1:
				mapFragment = new MapDisplayFragment();
				return mapFragment;
			case 2:
				return new HistoryFragment();

			}

			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}
	
	public void asyncJson() {

		aquery = new AQuery(this);
		
		String url = Constants.SAFE_SPEED_LIMIT_PRELINK;
		url = url + SafeDrivePreferences.preferences.getString("latitude", Constants.SAFE_SPEED_LAT) + "," + SafeDrivePreferences.preferences.getString("longitude", Constants.SAFE_SPEED_LONG);
		url = url + Constants.SAFE_SPEED_LIMIT_POSTLINK;

		// aquery.ajax(url, JSONObject.class, this, "jsonCallback");
		aquery.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

			@Override
			public void callback(String url, JSONObject json, AjaxStatus status) {

				if (json != null) {

					// readSpeedByParsingJson(json);
					// successful ajax call, show status code and json content
					new ReadSpeedLimitParsingJson().execute(json);
					/*
					 * Toast.makeText(aquery.getContext(), status.getCode() +
					 * ":" + json.toString(), Toast.LENGTH_LONG).show();
					 */

				} else {

					// ajax error, show error code
					Toast.makeText(aquery.getContext(),
							"Error:" + status.getCode(), Toast.LENGTH_LONG)
							.show();
				}
			}
		});

	}	


	class ReadSpeedLimitParsingJson extends AsyncTask<JSONObject, Void, String> {

		protected void onPostExecute(String result) {
			if (result != "Success") {
				Toast.makeText(getApplicationContext(), "Sorry!! Not displayed",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected String doInBackground(JSONObject... json) {
			// TODO Auto-generated method stub

			try {
				double speedLimit;
				JSONObject responseVal = json[0].getJSONObject("Response");

				JSONArray jsonArray = (JSONArray) (responseVal.get("Link"));

				JSONObject temp = jsonArray.getJSONObject(0);
				if (temp.has("SpeedLimit")) {
					speedLimit = temp.getDouble("SpeedLimit");
					speedLimit = speedLimit * Constants.SAFE_SPEED_LIMIT_CALCULATION_FACTOR;

					
					SafeDrivePreferences.setPreferences("SpeedLimit",
							String.valueOf(speedLimit));
					Log.d("speed limit from web service", String.valueOf(speedLimit));
				} else {
					if ((SafeDrivePreferences.preferences != null)
							&& (SafeDrivePreferences.preferences
									.contains("SpeedLimit"))) {

						speedLimit = Double
								.valueOf(SafeDrivePreferences.preferences
										.getString("SpeedLimit", Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE));
						Log.d("speed limit sp", String.valueOf(speedLimit));
					} else {
						SafeDrivePreferences.setPreferences("SpeedLimit", Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE);
						Log.d("speed limit nh", String.valueOf(Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE));
					}

				}
				return "Success";
			} catch (JSONException e) {
				e.printStackTrace();
				return "Failure";
			}

		}
	}
}
