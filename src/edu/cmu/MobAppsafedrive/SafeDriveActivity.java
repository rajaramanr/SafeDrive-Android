package edu.cmu.MobAppsafedrive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;
import edu.cmu.Model.CarDataModel;
import edu.cmu.Model.ViolationsModel;
import edu.cmu.utility.Constants;
import edu.cmu.utility.SafeSQLiteHelper;
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
	public static final int MESSAGE_READ = 9999;	
	Set<String> data = new HashSet<String>();

	private UiLifecycleHelper uiHelper;
	BluetoothDevice dev = null;
	Thread carDataParse;
	private AQuery aquery;
	Thread accidentProneDisplay;
	SafeSQLiteHelper db;
	public static List<ViolationsModel> violationsList = new ArrayList<ViolationsModel>();

	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;

	public List<CarDataModel> carDataList = new ArrayList<CarDataModel>();
	
	MediaPlayer mp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		uiHelper = new UiLifecycleHelper(this, null);
	    uiHelper.onCreate(savedInstanceState);
	    mp = MediaPlayer.create(getApplicationContext(), R.raw.digital);

 if (savedInstanceState != null) {
	        pendingPublishReauthorization = 
	            savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
	    }
		db = new SafeSQLiteHelper(this, Constants.DATABASE_NAME, null,
				Constants.DATABASE_VERSION);

		SafeDrivePreferences.preferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		SafeDrivePreferences.setPreferences("SpeedLimit",
				Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE);
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
		checkAccidentProne();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		bluetoothItem = menu.findItem(R.id.item1);

		return true;
	}

	public void checkAccidentProne() {

		accidentProneDisplay = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				boolean isItAccidentProne = false;
				while (true) {
					if (db != null) {

						isItAccidentProne = db.isItAccidentProne();
						
						if(isItAccidentProne){
							SafeDrivePreferences.setBooleanPreferences("isItAccidentProne", true);
						}else{
							SafeDrivePreferences.setBooleanPreferences("isItAccidentProne", false);
						}
						
					}

					try {
						Thread.sleep(Constants.jsonParseRate);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
		accidentProneDisplay.start();
	}

	public void parseJsonData() {

		carDataParse = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				Iterator it;
				CarDataModel carDataModel;				
				double speedLimit;
				double currentSpeed;

				while (true) {

					it = carDataList.iterator();
			
					while (it.hasNext()) {

						carDataModel = (CarDataModel) it.next();

						SafeDrivePreferences.setPreferences("latitude",
								String.valueOf(carDataModel.getLatitude()));
						SafeDrivePreferences.setPreferences("longitude",
								String.valueOf(carDataModel.getLongitude()));
						SafeDrivePreferences.setPreferences("currentSpeed",
								String.valueOf(carDataModel.getCurrentSpeed()));
						
						asyncJson();

						currentSpeed = carDataModel.getCurrentSpeed();
						speedLimit = Double.valueOf(SafeDrivePreferences.preferences
								.getString(
										"speedLimit",
										Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE));

						db.getViolationsFromUserInfo();
						
						if((currentSpeed - 1 >= speedLimit)){
							db.updateUserInfo();
															
							mp.start();
							
							SafeDrivePreferences.setBooleanPreferences("violation", true);

						}
						
						try {
							Thread.sleep(Constants.jsonParseRate);
							
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

	
	public void readJsonFromJsonFile() {

		String json;
		double timeStamp = 0;
		boolean isFirstDataCrossed = false;
		double prevTimeStamp = 0;
		JSONObject jsonObject;
		BufferedReader reader;
		String name;
		double vehicleSpeed = 0.0;
		double vehicleLatitude = 0.0;
		double vehicleLongitude = 0.0;

		NumberFormat numberFormat = NumberFormat.getInstance();

		numberFormat.setMinimumFractionDigits(1);
		numberFormat.setMaximumFractionDigits(1);

		try {
			reader = new BufferedReader(new InputStreamReader(getAssets().open(
					"Final.json"), "UTF-8"));
			while ((json = reader.readLine()) != null) {

				// Instantiate a JSON object from the request response

				jsonObject = new JSONObject(json);

				name = jsonObject.getString("name");
				String temp = (jsonObject.getString("timestamp"));

				timeStamp = Double.valueOf(temp);

				if (name.equals("vehicle_speed")) {

					vehicleSpeed = jsonObject.getDouble("value");
				} else if (name.equals("latitude")) {

					vehicleLatitude = jsonObject.getDouble("value");
				} else if (name.equals("longitude")) {

					vehicleLongitude = jsonObject.getDouble("value");
				}

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

		

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		} else if (id == R.id.item1) {
Intent intent = new Intent(this,ConnectActivity.class);
			startActivity(intent);


		}else if(id == R.id.item2){
			
			
			publishStory();
			}
		return super.onOptionsItemSelected(item);
	}
	
	private void publishStory() {
		
	    Session session = Session.getActiveSession();

	    if (session != null){

	        // Check for publish permissions    
	        List<String> permissions = session.getPermissions();
	        if (!isSubsetOf(PERMISSIONS, permissions)) {
	            pendingPublishReauthorization = true;
	            Session.NewPermissionsRequest newPermissionsRequest = new Session
	                    .NewPermissionsRequest(this, PERMISSIONS);
	        session.requestNewPublishPermissions(newPermissionsRequest);
	            return;
	        }

	        Bundle postParams = new Bundle();
	        //postParams.putString("name", "Facebook SDK for Android");
	       // postParams.putString("caption", "Build great social apps and get more installs.");
	     //   postParams.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
	        postParams.putString("message", "SafeDrive");
	      //  postParams.putString("link", "https://developers.facebook.com/android");
	     //   postParams.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

	        Request.Callback callback= new Request.Callback() {
	            public void onCompleted(Response response) {
	            /*    JSONObject graphResponse = response
	                                           .getGraphObject()
	                                           .getInnerJSONObject();
	                String postId = null;
	                try {
	                    postId = graphResponse.getString("id");
	                } catch (JSONException e) {
	                    Log.i(TAG,
	                        "JSON error "+ e.getMessage());
	                }
	                FacebookRequestError error = response.getError();
	                if (error != null) {
	                    Toast.makeText(
	                         getApplicationContext(),
	                         error.getErrorMessage(),
	                         Toast.LENGTH_SHORT).show();
	                    } else {
	                        Toast.makeText(getApplicationContext(), 
	                             postId,
	                             Toast.LENGTH_LONG).show();
	                }
*/	            }
	        };

	        Request request = new Request(session, "me/feed", postParams, 
	                              HttpMethod.POST, callback);

	        RequestAsyncTask task = new RequestAsyncTask(request);
	        task.execute();
	    }

	}
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    return true;
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	       // shareButton.setVisibility(View.VISIBLE);
	    	if (pendingPublishReauthorization && 
	    	        state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
	    	    pendingPublishReauthorization = false;
	    	    publishStory();
	    	}
	    } else if (state.isClosed()) {
	       // shareButton.setVisibility(View.INVISIBLE);
	    }
	    
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	/*@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
*/
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
			// asyncJson();
			// dashBoardFragment.refreshView();
		} else if (position == 1) {
			// mapFragment.getCurrentAddressTaskObject().execute();
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
		url = url
				+ SafeDrivePreferences.preferences.getString("latitude",
						Constants.SAFE_SPEED_LAT)
				+ ","
				+ SafeDrivePreferences.preferences.getString("longitude",
						Constants.SAFE_SPEED_LONG);
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
				Toast.makeText(getApplicationContext(),
						"Sorry!! Not displayed", Toast.LENGTH_SHORT).show();
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
				JSONObject addressObj;
				String county, state;

				if (temp.has("Address")) {

					addressObj = temp.getJSONObject("Address");
					county = addressObj.getString("County");
					state = addressObj.getString("State");
					SafeDrivePreferences.setPreferences("county", county);
					SafeDrivePreferences.setPreferences("state", state);
				}

				if (temp.has("SpeedLimit")) {
					speedLimit = temp.getDouble("SpeedLimit");
					speedLimit = speedLimit
							* Constants.SAFE_SPEED_LIMIT_CALCULATION_FACTOR;

					SafeDrivePreferences.setPreferences("SpeedLimit",
							String.valueOf(speedLimit));
					
				} else {
					if ((SafeDrivePreferences.preferences != null)
							&& (SafeDrivePreferences.preferences
									.contains("SpeedLimit"))) {

						speedLimit = Double
								.valueOf(SafeDrivePreferences.preferences
										.getString(
												"SpeedLimit",
												Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE));
						
					} else {
						SafeDrivePreferences.setPreferences("SpeedLimit",
								Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE);
						
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
