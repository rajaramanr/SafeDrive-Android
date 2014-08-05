package edu.cmu.MobAppsafedrive;

import com.google.android.gms.internal.en;

import edu.cmu.utility.SafeDrivePreferences;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends ActionBarActivity {

	SeekBar seekBar;
	Spinner spinner;
	Switch switchValue;
	TextView enterSpeed;
	Switch location;
	EditText speedValue;
	TextView thresholdValue;
	TextView custom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		location = (Switch) findViewById(R.id.switch2);
		thresholdValue = (TextView) findViewById(R.id.textView8);
		speedValue = (EditText) findViewById(R.id.editText2);
		custom = (TextView) findViewById(R.id.textView9);
		seekBar = (SeekBar) findViewById(R.id.seekBar1);				
		spinner = (Spinner) findViewById(R.id.spinner1);
		switchValue = (Switch) findViewById(R.id.switch1);	
		
		if(SafeDrivePreferences.preferences.contains("Threshold")){
			seekBar.setProgress(Integer.parseInt(SafeDrivePreferences.preferences.getString("Threshold", "0")));
			thresholdValue.setText(SafeDrivePreferences.preferences.getString("Threshold", "0"));
		}
		
		if(SafeDrivePreferences.preferences.contains("LocationBased")){			
			location.setChecked(Boolean.valueOf(SafeDrivePreferences.preferences.getString("LocationBased", "false")));
		}						
		
		if(location.isChecked()){
									
			speedValue.setVisibility(View.INVISIBLE);
			custom.setVisibility(View.INVISIBLE);
			
		}else{
			
			speedValue.setVisibility(View.VISIBLE);
			custom.setVisibility(View.VISIBLE);
		}
		
		if(SafeDrivePreferences.preferences.contains("Unit")){
			if(SafeDrivePreferences.preferences.getString("Unit", "0").equalsIgnoreCase("MPH")){
				spinner.setSelection(0);
			}else{
				spinner.setSelection(1);
			}
						
		}
		
		if(SafeDrivePreferences.preferences.contains("SpeedValue")){
			speedValue.setText(SafeDrivePreferences.preferences.getString("SpeedValue", "0"));
		}
		
		if(SafeDrivePreferences.preferences.contains("AlertMe")){
			switchValue.setChecked(Boolean.valueOf(SafeDrivePreferences.preferences.getString("AlertMe", "false")));
		}		
				
		
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub

				thresholdValue.setText(String.valueOf(progress));
			}
		});

		location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub

				if (isChecked) {

					custom.setVisibility(View.INVISIBLE);
					speedValue.setVisibility(View.INVISIBLE);
				} else {					
					
					custom.setVisibility(View.VISIBLE);
					speedValue.setVisibility(View.VISIBLE);
				}

			}
		});

		speedValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

				int no = 0;

				if (!speedValue.getText().toString().equals("")) {
					String t = speedValue.getText().toString();
					no = Integer.valueOf(t);
				}

				if ((no < 10) || (no > 150)) {

					speedValue.setError("Make sure you enter speed Value between 0 and 150");

				}

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	public void onPause() {

		super.onPause();
		System.out.println("Inside settings");		

		String s1 = Integer.toString((seekBar).getProgress());

		String value = spinner.getSelectedItem().toString();
		String sV = Boolean.toString(switchValue.isChecked());
		String speed = speedValue.getText().toString();

		if (location.isChecked()) {
			if (!speed.equals("")) {

				if ((Integer.parseInt(speed) < 10)
						&& (Integer.parseInt(speed) > 150)) {

					speedValue.setError("Make sure you enter speed Value between 0 and 150");
					
				}
			} else {

				speedValue.setError("Make sure you enter speed Value");
				
			}

		}

		SafeDrivePreferences.setPreferences("Threshold", s1);
		SafeDrivePreferences.setPreferences("Unit", value);
		SafeDrivePreferences.setPreferences("AlertMe", sV);		
		SafeDrivePreferences.setPreferences("LocationBased",
				Boolean.toString(location.isChecked()));
		if(!speed.equals("")){
			SafeDrivePreferences.setPreferences("SpeedValue", speed);
		}
				

		System.out.println(s1 + " " + value + " " + sV);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
