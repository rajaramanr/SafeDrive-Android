package edu.cmu.MobAppsafedrive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectActivity extends Activity {

	Context context = this;
	Set<String> data = new HashSet<String>();
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private Set<String> devicesDisc = new HashSet<String>();
	private static final UUID BLUETOOTH_SPP_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805f9b34fb");
	public static final int MESSAGE_READ = 9999;
	// String path = context.getFilesDir().getAbsolutePath();

	BluetoothDevice dev = null;

	final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// Log.d(CURRENT_CLASS, "Reaching receiver" + " - action : " +
			// action);
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getName().contains("Nexus 7"))
					dev = device;
				devicesDisc.add(device.getName() + " - " + device.getAddress());
				showList(devicesDisc);

			}
		}

		private void showList(Set<String> devicesDisc) {
			// TODO Auto-generated method stub
			TextView showDev = (TextView) findViewById(R.id.textView3);
			showDev.setText("" + devicesDisc);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

	public void activateBluetooth(View view) {
		Log.d("activateBluetooth", "inside activateBluetooth");

		if (!mBluetoothAdapter.isEnabled()) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);

		} else {
			Toast.makeText(this, "Your are connected already!",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void discoverDev(View view) {
		Log.d("discoverDev", "inside discoverDev");
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister
												// during onDestroy
		mBluetoothAdapter.startDiscovery();
	}

	public void connectMe(View view) {
		Log.d("connectMe", "inside connectMe");
		new ConnectThread(dev, mBluetoothAdapter).start();
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private BluetoothAdapter adapter;
		private final BluetoothDevice mmDevice;
		private InputStream mmInStream;
		private Handler mHandler = new Handler();

		public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter) {
			BluetoothSocket tmp = null;
			this.adapter = adapter;
			mmDevice = device;
			Log.d("Found device", "" + device);
			try {
				tmp = device
						.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			adapter.cancelDiscovery();
			try {
				Log.d("Main", "Trying to connect");
				mmSocket.connect();
			} catch (IOException connectException) {
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}
			;
			try {
				data = manageConnectedSocket(mmSocket);
				mmSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}

		private Set<String> manageConnectedSocket(BluetoothSocket mmSocket) throws InterruptedException {
			Log.d("Main", "Connected Hammaya");
			File path = Environment.getExternalStorageDirectory();
			String sdPath = path.getAbsolutePath();
			File file = new File(sdPath + "/details.txt");
			InputStream tmpIn = null;
			try {
				tmpIn = mmSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mmInStream = tmpIn;
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()
			FileOutputStream stream = null;
			String allData = "";
			try {
				stream = new FileOutputStream(file, true);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			Thread.sleep(1000);
			while (true) {
				try {
					if(mmInStream.available() > 0) { 
						bytes = mmInStream.read(buffer);
						mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();
					} else {
						break;
					}
					int length = buffer.length;
					byte[] temp = new byte[length + 1];
					int i = buffer.length - 1;
					while (i >= 0 && buffer[i] == 0)
					{
						--i;
					}
					temp = Arrays.copyOf(buffer, i+1);
					allData = new String(temp);
					stream.write(allData.getBytes());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				// data.add(new String(buffer));
				
			}
			((Activity)context).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, "Connection Succesful", Toast.LENGTH_SHORT).show();
					
				}
			});

			try {
				
				stream.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				mmSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return data;

		}
		
	}

}
