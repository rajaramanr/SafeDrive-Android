package edu.cmu.MobAppsafedrive;



import edu.cmu.utility.SafeDrivePreferences;
import android.support.v7.app.ActionBarActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

public class SettingsActivity extends ActionBarActivity {

	private SeekBar seekBar1;
	private SeekBar seekBar2;
	Spinner spinner;
	Switch switchValue;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings1);		
		
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	//	getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	//public void settingDetails(View view){
	public void onPause(){
		
		super.onPause();
		System.out.println("Inside settings");
		
		seekBar1 = (SeekBar)findViewById(R.id.seekBar1);
		seekBar2 = (SeekBar)findViewById(R.id.seekBar2);
		spinner = (Spinner) findViewById(R.id.spinner1);
		switchValue = (Switch) findViewById(R.id.switch1);
		
		String s1 = Integer.toString((seekBar1).getProgress());
		String s2 = Integer.toString((seekBar2).getProgress());
		String value = spinner.getSelectedItem().toString();
		String sV =	Boolean.toString(switchValue.isChecked());
		
		
		SafeDrivePreferences.setPreferences("Threshold", s1);
		SafeDrivePreferences.setPreferences("Unit", value);
		SafeDrivePreferences.setPreferences("AlertMe", sV);
		SafeDrivePreferences.setPreferences("Volume", s2);
		
		
		System.out.println(s1 +" "+s2+" "+value+" "+sV);
		
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
