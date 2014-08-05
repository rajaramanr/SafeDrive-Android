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
	public static final double SAFE_MILES_KMS_CONVERTOR = 1.60934;
	public static final String SAFE_HELP_MESSAGE = "SafeDrive alerts you when your car crosses the speed limit of a certain road and when your car is navigating in an accident prone zone. You can view your current location on the map and post status of a location on Facebook \n \n"
			+ "Settings \n\n"
			+ "Dashboard :\n "
			+ "Click on the bluetooth icon to connect to your car \n"
			+ "View the current speed and speed limit here. If you cross the speed limit the alarm will ring. If you are navigating in an accident prone zone the screen would turn red.\n \n"
			+ "Location: \n"
			+ "It gives you the current location\n \n"
			+ "History: \n"
			+ "It shows you the list of violations made\n \n"
			+ "Settings:\n"
			+ "Speed Limit: You can set your threshold speed limit between 1-10 Enable LocationBased to get speed limit automatically or disable to set the speed limit manually. \n"
			+ "You can set the units of your speed and speed limits. \n \n"
			+ "Alert Configuration: Enable to receive alert.\n";

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
