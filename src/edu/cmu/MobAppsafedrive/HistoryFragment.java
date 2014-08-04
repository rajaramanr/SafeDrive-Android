package edu.cmu.MobAppsafedrive;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.MobAppsafedrive.DashboardFragment.DashboardListAdapter;
import edu.cmu.Model.DashBoardModel;
import edu.cmu.Model.ViolationsModel;
import edu.cmu.utility.Constants;
import edu.cmu.utility.SafeDrivePreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryFragment extends Fragment implements Cloneable {

	Thread displayUserViolationsThread;
	TextView textView;
	TableLayout table;
	TableRow tableRow;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_history, container,
				false);

		table = (TableLayout) rootView.findViewById(R.id.tabLay);

		displayViolations();
		return rootView;
	}

	public void displayViolations() {

		displayUserViolationsThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean violationStatus = false;

				while (true) {
					violationStatus = SafeDrivePreferences.preferences
							.getBoolean("violation", false);

					if (violationStatus) {

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// This code will always run on the UI thread,
								// therefore
								// is safe to modify UI elements.

								refreshPage();

							}
						});

					}

					try {
						Thread.sleep(Constants.violationsParseRate);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
		displayUserViolationsThread.start();
	}

	public void refreshPage() {

		List<ViolationsModel> refreshList = new ArrayList<ViolationsModel>();

		refreshList = SafeDriveActivity.violationsList;

		if (refreshList.size() > 0) {

			for (int i = 1; i < table.getChildCount(); i++) {

				tableRow = (TableRow) table.getChildAt(i);

				for (int j = 0; j < tableRow.getChildCount(); j++) {

					textView = (TextView) tableRow.getChildAt(j);

					if (j == 0) {
						textView.setText(refreshList.get(i - 1).getDate());
					} else {
						textView.setText(refreshList.get(i - 1)
								.getNoOfViolations());
					}
				}
			}

			SafeDrivePreferences.setBooleanPreferences("violation", false);
		}

	}
}
