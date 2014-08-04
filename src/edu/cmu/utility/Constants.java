package edu.cmu.utility;

public class Constants {

	public static String SAFE_SPEED_LIMIT_PRELINK = "http://route.st.nlp.nokia.com/routing/6.2/getlinkinfo.json?waypoint=";
	public static String SAFE_SPEED_LIMIT_POSTLINK = "&app_id=PBdVjN6jd3Q54PcHqBvQ&app_code=tWTo4JvU225hezjvCPBiog";
	public static String SAFE_SPEED_LAT = "39.434014";
	public static String SAFE_SPEED_LONG = "-79.994728";
	public static String SAFE_SPEED_LIMIT = "Speed Limit:";
	public static String SAFE_CURRENT_SPEED = "Current Speed:";
	public static String SAFE_CURRENT_SPEED_VALUE = "60";
	public static String SAFE_NATIONAL_SPEED_LIMIT_VALUE = "60";
	public static double SAFE_SPEED_LIMIT_CALCULATION_FACTOR = 3.6;
	public static final long jsonParseRate = 2000;
	public static final long violationsParseRate = 5000;
	public static final String SAFE_DEFAULT_COUNTY = "Allegheny";
	public static final String SAFE_DEFAULT_STATE = "Pennsylvania";
	public static final String SAFE_DEFAULT_STREET = "Penn Lincoln Pkwy";	
	
	// Database Version
	public static final int DATABASE_VERSION = 1;
	// Database Name
	public static final String DATABASE_NAME = "AccidentDB.sqlite";
	
	public static final String TABLE_ACCIDENT = "accidentInfo";
	public static final String TABLE_COUNTY = "countyInfo";
	public static final String TABLE_STATE = "stateInfo";
	public static final String TABLE_USERINFO = "userInfo";		

	// Table Columns names
	public static final String KEY_ID = "id";
	public static final String KEY_REGION = "region";
	public static final String KEY_COUNTY = "county";
	public static final String KEY_STATE = "state";
	public static final String KEY_STATE_ID = "stateid";
	public static final String KEY_COUNTY_ID = "countyid";
	public static final String KEY_DATE = "date";
	public static final String KEY_NOV = "noOfVls";
	 
}
