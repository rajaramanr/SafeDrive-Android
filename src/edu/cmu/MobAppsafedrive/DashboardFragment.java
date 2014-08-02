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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// View rootView = inflater.inflate(R.layout.fragment_dashboard,
		// container, false);

		// ArrayAdapter<String> adapter = new ArrayAdapter<String>(
		// inflater.getContext(), R.layout.fragment_dashboard,
		// DASHBOARD_LIST_KEYS);
		refreshView();
		/*
		 * dashboardBean = new DashBoardModel(
		 * SafeDrivePreferences.preferences.getString("currentSpeed",
		 * Constants.SAFE_CURRENT_SPEED_VALUE),
		 * SafeDrivePreferences.preferences.getString("SpeedLimit",
		 * Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE));
		 * 
		 * setListAdapter(new DashboardListAdapter(getActivity(),
		 * DASHBOARD_LIST_KEYS));
		 */

		//
		// rootView.setListAdapter(new DashboardListAdapter<Arra>);
		// aquery = new AQuery(getActivity());

		// new SpeedLimit().execute();
		// displaySpeedLimit();

		return super.onCreateView(inflater, container, savedInstanceState);
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

					Log.d("Inside Dash fragment thread",
							SafeDrivePreferences.preferences
									.getString(
											"currentSpeed",
											Constants.SAFE_CURRENT_SPEED_VALUE
													+ SafeDrivePreferences.preferences
															.getString(
																	"SpeedLimit",
																	Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE)));

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// This code will always run on the UI thread,
							// therefore
							// is safe to modify UI elements.

							String formattedCurrentSpeed = formatter.format(Double.valueOf(SafeDrivePreferences.preferences
									.getString("currentSpeed",
											Constants.SAFE_CURRENT_SPEED_VALUE)));

							String formattedSpeedLimit = formatter.format(Double.valueOf(SafeDrivePreferences.preferences
									.getString(
											"SpeedLimit",
											Constants.SAFE_NATIONAL_SPEED_LIMIT_VALUE)));

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
