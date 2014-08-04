package edu.cmu.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.internal.db;

import edu.cmu.MobAppsafedrive.SafeDriveActivity;
import edu.cmu.Model.ViolationsModel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class SafeSQLiteHelper extends SQLiteOpenHelper {

	private Context context;
	private static String dbPath;
	public static SQLiteDatabase database;

	public SafeSQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, Environment.getExternalStorageDirectory()

		+ File.separator + name, factory, version);

		this.context = context;
		this.dbPath = Environment.getExternalStorageDirectory()
				+ File.separator + name;
		boolean dbExists = checkIfDatabaseExists();

		if (!dbExists) {

			this.getReadableDatabase();

			try {
				copyDatabaseForUse();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		opendatabase();

		createUserInfoTable();
		
		// super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	private void copyDatabaseForUse() throws IOException {

		InputStream inputStream = context.getAssets().open(
				Constants.DATABASE_NAME);

		// Open the empty db as the output stream
		OutputStream outputStream = new FileOutputStream(dbPath);

		// transfer byte to inputfile to outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, length);
		}

		// Close the streams
		outputStream.flush();
		outputStream.close();
		inputStream.close();

	}

	public boolean checkIfDatabaseExists() {

		boolean ifDatabaseExists = false;

		try {

			File dbFile = new File(dbPath);

			ifDatabaseExists = dbFile.exists();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ifDatabaseExists;

	}

	public void opendatabase() throws SQLException {
		// Open the database
		database = SQLiteDatabase.openDatabase(dbPath, null,
				SQLiteDatabase.OPEN_READWRITE);
	}	

	public synchronized void close() {
		if (database != null) {
			database.close();
		}
		super.close();
	}
	
	private void createUserInfoTable(){
		
		String CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ Constants.TABLE_USERINFO + " ( " + Constants.KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Constants.KEY_DATE
				+ " DATE, " + Constants.KEY_NOV + " INTEGER );";

		database.execSQL(CREATE_HISTORY_TABLE);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public boolean isItAccidentProne() {

		boolean accidentProne = false;
		//database = this.getReadableDatabase();
		opendatabase();

		String countyName = SafeDrivePreferences.preferences.getString(
				"county", Constants.SAFE_DEFAULT_COUNTY).toLowerCase();
		String stateName = SafeDrivePreferences.preferences.getString("state",
				Constants.SAFE_DEFAULT_STATE).toLowerCase();
		String streetName = SafeDrivePreferences.preferences.getString(
				"street", Constants.SAFE_DEFAULT_STREET).toLowerCase();

		String query = "SELECT  * FROM " + Constants.TABLE_ACCIDENT
				+ " WHERE LOWER(" + Constants.KEY_REGION + ") LIKE '%"
				+ streetName + "%'" + " AND " + Constants.KEY_COUNTY_ID + " = "
				+ "(SELECT " + Constants.KEY_ID + " FROM "
				+ Constants.TABLE_COUNTY + " WHERE " + "LOWER("
				+ Constants.KEY_COUNTY + ") LIKE '%" + countyName + "%')"
				+ " AND " + Constants.KEY_STATE_ID + " = " + "(SELECT "
				+ Constants.KEY_ID + " FROM " + Constants.TABLE_STATE
				+ " WHERE " + "LOWER(" + Constants.KEY_STATE + ") LIKE '%"
				+ stateName + "%');";

		Cursor cursor = database.rawQuery(query, null);

		if ((cursor != null) && (cursor.getCount() > 0)) {
			accidentProne = true;
		}

		cursor.close();
		close();

		return accidentProne;
	}

	public void updateUserInfo() {

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate = dateFormat.format(currentDate);
		int noOfViolations = 0;

		//database = this.getWritableDatabase();
		opendatabase();
		String query = "SELECT * FROM " + Constants.TABLE_USERINFO + " WHERE "
				+ Constants.KEY_DATE + " = '" + formattedDate + "';";

		Cursor cursor = database.rawQuery(query, null);

		if (cursor != null) {

			if (cursor.getCount() > 0) {

				cursor.moveToFirst();
				noOfViolations = cursor.getInt(2) + 1;

				query = "UPDATE " + Constants.TABLE_USERINFO + " SET "
						+ Constants.KEY_NOV + " = " + noOfViolations
						+ " WHERE " + Constants.KEY_DATE + " = " + "'"
						+ formattedDate + "';";
				database.execSQL(query);

			} else {

				ContentValues values = new ContentValues();
				values.put(Constants.KEY_DATE, formattedDate);
				values.put(Constants.KEY_NOV, noOfViolations + 1);

				database.insert(Constants.TABLE_USERINFO, null, values);

			}
		}
		cursor.close();		
		//database.close();
		close();
	}
	
	public void getViolationsFromUserInfo(){
		
		int n=6;
		Date currentDate = new Date();
		Date sevenDaysBfrDate = new Date(currentDate.getTime() - n * 24 * 3600 * 1000 );
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		String formattedDate = dateFormat.format(currentDate);
		String formattedPrevDate = dateFormat.format(sevenDaysBfrDate);
		
		//database = this.getWritableDatabase();
		opendatabase();
		String query = "SELECT * FROM " + Constants.TABLE_USERINFO + " WHERE " + Constants.KEY_DATE + " BETWEEN "
				+ "'" + formattedPrevDate +"' AND" + " '" + formattedDate + "';" ;
		
		Cursor cursor = database.rawQuery(query, null);
		
		if((cursor != null) &&(cursor.getCount() > 0)){
								
			cursor.moveToFirst();
			SafeDriveActivity.violationsList.clear();
			
			do{
				
				SafeDriveActivity.violationsList.add(new ViolationsModel(cursor.getString(1), cursor.getString(2)));
			}while(cursor.moveToNext());			
									
		}
		cursor.close();
		//database.close();
		close();
		
	}
}
