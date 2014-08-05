package edu.cmu.MobAppsafedrive;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import edu.cmu.Model.DashBoardModel;
import edu.cmu.utility.Constants;
import edu.cmu.utility.SafeDrivePreferences;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DashboardFragment extends ListFragment {

	static JSONObject jsonObject = null;
	DashBoardModel dashboardBean;
	Thread fragmentDisplay;
	// DashBoardModel dashboardBean;
	static final String[] DASHBOARD_LIST_KEYS = new String[] {
			Constants.SAFE_CURRENT_SPEED, Constants.SAFE_SPEED_LIMIT };

	View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = super.onCreateView(inflater, container, savedInstanceState);
		refreshView();

		return view;
		// return rootView;
	}

	/*
	 * public void displaySpeedLimit() {
	 * 
	 * asyncJson();
	 * 
	 * }
	 */

	public void refreshView() {

		final NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(2);

		fragmentDisplay = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				while (true) {

					/*
					 * Log.d("Inside Dash fragment thread",
					 * SafeDrivePreferences.preferences .getString(
					 * "currentSpeed", Constants.SAFE_CURRENT_SPEED_VALUE +
					 * SafeDrivePreferences.preferences .getString(
					 * "SpeedLimit",
					 * Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE)));
					 */
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// This code will always run on the UI thread,
							// therefore
							// is safe to modify UI elements.
							boolean isItAccidentProne = SafeDrivePreferences.preferences
									.getBoolean("isItAccidentProne", false);
							String getUnit = "MPH";

							if (SafeDrivePreferences.preferences
									.contains("Unit")) {
								getUnit = SafeDrivePreferences.preferences
										.getString("Unit", "MPH");
							}

							double currentSpeed;
							double speedLimit = 0;

							if (isItAccidentProne) {
								view.setBackgroundColor(Color.rgb(220, 70, 70));
								Toast.makeText(getActivity(),
										"Drive Safe !! Accident Prone Area !!",
										Toast.LENGTH_SHORT).show();
							} else {
								view.setBackgroundColor(Color.WHITE);
							}

							currentSpeed = Double.valueOf(SafeDrivePreferences.preferences
									.getString("currentSpeed",
											Constants.SAFE_CURRENT_SPEED_VALUE));

							speedLimit = Double.valueOf(SafeDrivePreferences.preferences
									.getString(
											"SpeedLimit",
											Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE));

							if (SafeDrivePreferences.preferences
									.contains("LocationBased")) {
								if (SafeDrivePreferences.preferences.getString(
										"LocationBased", "false").equals(
										"false")) {
									
									String temp = SafeDrivePreferences.preferences
											.getString("SpeedValue", "0");
									
									if(!temp.equals("")){
										speedLimit = Double.valueOf(temp);
									}	
									
								}
							}

							String formattedCurrentSpeed;

							String formattedSpeedLimit;

							if (getUnit.equalsIgnoreCase("MPH")) {

								formattedCurrentSpeed = formatter
										.format(currentSpeed);
								formattedSpeedLimit = formatter
										.format(speedLimit);

								formattedCurrentSpeed = formattedCurrentSpeed
										+ "\t MPH";
								formattedSpeedLimit = formattedSpeedLimit
										+ "\t MPH";
							} else {
								currentSpeed = currentSpeed
										* Constants.SAFE_MILES_KMS_CONVERTOR;
								speedLimit = speedLimit
										* Constants.SAFE_MILES_KMS_CONVERTOR;

								formattedCurrentSpeed = formatter
										.format(currentSpeed);
								formattedSpeedLimit = formatter
										.format(speedLimit);

								formattedSpeedLimit = formattedSpeedLimit
										+ "\t KmPH";
								formattedCurrentSpeed = formattedCurrentSpeed
										+ "\t KmPH";
							}

							dashboardBean = new DashBoardModel(
									formattedCurrentSpeed, formattedSpeedLimit);

							setListAdapter(new DashboardListAdapter(
									getActivity(), DASHBOARD_LIST_KEYS));

						}
					});

					try {
						Thread.sleep(Constants.jsonParseRate);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// new RefreshDashboardView().execute();

			}
		});

		fragmentDisplay.start();
	}

	class RefreshDashboardView extends AsyncTask<Void, Void, String> {

		protected void onPostExecute(String result) {
			if (result != "Success") {
				Toast.makeText(getActivity(), "Sorry!! Not displayed",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub

			try {
				dashboardBean = new DashBoardModel(
						SafeDrivePreferences.preferences.getString(
								"currentSpeed",
								Constants.SAFE_CURRENT_SPEED_VALUE),
						SafeDrivePreferences.preferences.getString(
								"SpeedLimit",
								Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE));

				setListAdapter(new DashboardListAdapter(getActivity(),
						DASHBOARD_LIST_KEYS));

				return "Success";

			} catch (Exception e) {
				e.printStackTrace();
				return "Failure";
			}

		}
	}

	public class DashboardListAdapter extends ArrayAdapter<String> {

		private final Context context;
		private final String[] dashKeys;

		public DashboardListAdapter(Context context, String[] dashKeys) {

			super(context, R.layout.fragment_dashboard, dashKeys);
			this.context = context;
			this.dashKeys = dashKeys;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			View rowView = convertView;
			// reuse views
			if (rowView == null) {
				rowView = LayoutInflater.from(context).inflate(
						R.layout.fragment_dashboard, null);

				// configure view holder
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.text1 = (TextView) rowView
						.findViewById(R.id.displayTextId);
				viewHolder.text2 = (TextView) rowView
						.findViewById(R.id.speedTextId);
				rowView.setTag(viewHolder);
			}

			// fill data
			ViewHolder holder = (ViewHolder) rowView.getTag();
			String dashKey = dashKeys[position];
			holder.text1.setText(dashKey);

			if (dashboardBean != null) {
				if (dashKey.equals(Constants.SAFE_CURRENT_SPEED)) {
					holder.text2.setText(dashboardBean.getCurrentSpeed());
				} else {
					holder.text2.setText(dashboardBean.getSpeedLimit());
				}
			}

			return rowView;

		}
	}

	static class ViewHolder {
		public TextView text1;
		public TextView text2;
	}

}
