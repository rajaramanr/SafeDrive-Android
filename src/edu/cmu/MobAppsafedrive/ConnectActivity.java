package edu.cmu.MobAppsafedrive;

import java.io.IOException;
import java.io.InputStream;
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
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectActivity extends Activity {

	Set<String> data = new HashSet<String>();
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private Set<String> devicesDisc = new HashSet<String>();
	private static final UUID BLUETOOTH_SPP_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805f9b34fb");
	public static final int MESSAGE_READ = 9999;

	BluetoothDevice dev = null;
	MenuItem bluetoothItem;
	
	int REQUEST_ENABLE_BT = 1;

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

	public void activateBluetooth(View view) {
		Log.d("activateBluetooth", "inside activateBluetooth");

		if (!mBluetoothAdapter.isEnabled()) {			
					    
		    Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		    
			bluetoothItem.setIcon(R.drawable.bluetooth_blue);
		} else {
			Toast.makeText(this, "Already Connected", Toast.LENGTH_SHORT)
					.show();
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
			data = manageConnectedSocket(mmSocket);
			try {
				mmSocket.close();
			} catch (IOException e) {
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

		private Set<String> manageConnectedSocket(BluetoothSocket mmSocket) {
			Log.d("Main", "Connected Hammaya");
			InputStream tmpIn = null;
			try {
				tmpIn = mmSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mmInStream = tmpIn;
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()
			while (true) {
				try {
					bytes = mmInStream.read(buffer);
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();
				} catch (IOException e) {
					break;
				}
				Log.d("Main", "Bytes Received - " + new String(buffer));
				data.add(new String(buffer));

			}
			return data;

		}
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
