package edu.cmu.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class MySQLiteHelper extends SQLiteOpenHelper{

	private Context context;
	private static String dbPath;	
	public static SQLiteDatabase database;
	
	public MySQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, Environment.getExternalStorageDirectory()
		           
	            + File.separator + name, factory, version);
		
		this.context = context;
		this.dbPath = Environment.getExternalStorageDirectory()+ File.separator + name;
		boolean dbExists = checkIfDatabaseExists();
		
		if(!dbExists){
			
			this.getReadableDatabase();
			
			try{
				copyDatabaseForUse();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		
		opendatabase();
				        	           
		//super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	private void copyDatabaseForUse() throws IOException{
	
		InputStream inputStream = context.getAssets().open(Constants.DATABASE_NAME);

        //Open the empty db as the output stream
        OutputStream outputStream = new FileOutputStream(dbPath);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer))>0) {
        	outputStream.write(buffer,0,length);
        }

        //Close the streams
        outputStream.flush();
        outputStream.close();
        inputStream.close();

	}
	
	public boolean checkIfDatabaseExists(){
		
		boolean ifDatabaseExists = false;
		
		try {
			
			File dbFile = new File(dbPath);
			
			ifDatabaseExists = dbFile.exists();
			
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return ifDatabaseExists;
		
	}
	
	public void opendatabase() throws SQLException {
        //Open the database        
		database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void close() {
        if(database != null) {
            database.close();
        }
        super.close();
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
