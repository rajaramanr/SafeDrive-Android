package edu.cmu.utility;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SafeDrivePreferences {
	
	public static SharedPreferences preferences;
	
	public static void setPreferences(String key,String value){
		
		Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static void setBooleanPreferences(String key, boolean value){
		
		Editor editor = preferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

}
