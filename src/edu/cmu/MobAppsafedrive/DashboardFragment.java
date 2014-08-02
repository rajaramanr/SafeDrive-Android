package edu.cmu.MobAppsafedrive;

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
	private AQuery aquery;
	DashBoardModel dashboardBean;

	// DashBoardModel dashboardBean;
	static final String[] DASHBOARD_LIST_KEYS = new String[] {
			Constants.SAFE_CURRENT_SPEED, Constants.SAFE_SPEED_LIMIT };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//View rootView = inflater.inflate(R.layout.fragment_dashboard,
		//container, false);

		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			//	inflater.getContext(), R.layout.fragment_dashboard,
				//DASHBOARD_LIST_KEYS);		
		dashboardBean = new DashBoardModel("100", "60");
		setListAdapter(new DashboardListAdapter(getActivity(), DASHBOARD_LIST_KEYS));

		//
		// rootView.setListAdapter(new DashboardListAdapter<Arra>);
		aquery = new AQuery(getActivity());

		// new SpeedLimit().execute();
		displaySpeedLimit();

		return super.onCreateView(inflater, container, savedInstanceState);
		//return rootView;
	}

	public void displaySpeedLimit() {

		asyncJson();

	}

	public void refreshView() {
		dashboardBean = new DashBoardModel("100", "60");
		setListAdapter(new DashboardListAdapter(getActivity(), DASHBOARD_LIST_KEYS));
	}

	public void asyncJson() {

		String url = Constants.SAFE_SPEED_LIMIT_PRELINK;
		url = url + Constants.SAFE_SPEED_LAT + "," + Constants.SAFE_SPEED_LONG;
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
				Toast.makeText(getActivity(), "Sorry!! Not displayed",
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
					speedLimit = speedLimit * 3.6;

					SafeDrivePreferences.setPreferences("SpeedLimit",
							String.valueOf(speedLimit));
					System.out.println(speedLimit);
				} else {
					if ((SafeDrivePreferences.preferences != null)
							&& (SafeDrivePreferences.preferences
									.contains("SpeedLimit"))) {

						speedLimit = Double
								.valueOf(SafeDrivePreferences.preferences
										.getString("SpeedLimit", "60"));
						System.out.println(speedLimit);
					} else {
						SafeDrivePreferences.setPreferences("SpeedLimit", "60");
					}

				}
				return "Success";
			} catch (JSONException e) {
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
