/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.app.longtumdo.carekhun;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yc.peddemo.sdk.BLEServiceOperate;
import com.yc.peddemo.sdk.DeviceScanInterfacer;

import java.util.ArrayList;
import java.util.Locale;

public class SensorDeviceScanActivity extends ListActivity implements DeviceScanInterfacer, TextToSpeech.OnInitListener {
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private boolean mScanning;
	private Handler mHandler;
	private TextToSpeech tts;
	private final int REQUEST_ENABLE_BT = 1;

	// Stops scanning after 10 seconds.
	private final long SCAN_PERIOD = 10000;

	private BLEServiceOperate mBLEServiceOperate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getActionBar().setTitle(R.string.app_name);
		mHandler = new Handler();

		mBLEServiceOperate = BLEServiceOperate.getInstance(getApplicationContext());				// Ready for BluetoothLeService instantiation, must

		// Checks if Bluetooth is supported on the device.
		if (!mBLEServiceOperate.isSupportBle4_0()) {
			Toast.makeText(this, R.string.not_support_ble, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		mBLEServiceOperate.setDeviceScanListener(this);

		//**********************************************************************************************
		tts = new TextToSpeech(this, this, "com.google.android.tts");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(R.layout.sensor_actionbar_indeterminate_progress);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			mLeDeviceListAdapter.clear();
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Ensures Bluetooth is enabled on the device. If Bluetooth is not // permission to enable it.
		if (!mBLEServiceOperate.isBleEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		setListAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mBLEServiceOperate.unBindService();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final SensorBleDevices device = mLeDeviceListAdapter.getDevice(position);
		if (device == null)
			return;
		final Intent intent = new Intent(this, SensorMainActivity.class);
		intent.putExtra(SensorMainActivity.EXTRAS_DEVICE_NAME, device.getName());
		intent.putExtra(SensorMainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
		if (mScanning) {
			mBLEServiceOperate.stopLeScan();
			mScanning = false;
		}
		startActivity(intent);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBLEServiceOperate.stopLeScan();
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBLEServiceOperate.startLeScan();
		} else {
			mScanning = false;
			mBLEServiceOperate.stopLeScan();
		}
		invalidateOptionsMenu();
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tts.setLanguage(new Locale("th"));
			tts.speak("ระบบตรวจจับอัตราการเต้นของหัวใจ การเดิน การวิ่ง และการนอนหลับของผู้สูงอายุ", TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	// Adapter for holding devices found through scanning.
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<SensorBleDevices> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<SensorBleDevices>();
			mInflator = SensorDeviceScanActivity.this.getLayoutInflater();
		}

		public void addDevice(SensorBleDevices device) {
			boolean repeat = false;
			for (int i = 0; i < mLeDevices.size(); i++) {
				if (mLeDevices.get(i).getAddress().equals(device.getAddress())) {
					mLeDevices.remove(i);
					repeat = true;
					mLeDevices.add(i, device);
				}
			}
			if (!repeat) {
				mLeDevices.add(device);
			}
		}

		public SensorBleDevices getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			if (view == null) {
				view = mInflator.inflate(R.layout.sensor_listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
				viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
				viewHolder.deviceRssi = (TextView) view.findViewById(R.id.device_rssi);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			SensorBleDevices device = mLeDevices.get(i);
			final String deviceName = device.getName();
			final int rssi = device.getRssi();
			if (deviceName != null && deviceName.length() > 0) {
				viewHolder.deviceName.setText(deviceName);
			} else {
				viewHolder.deviceName.setText(R.string.unknown_device);
			}
			viewHolder.deviceAddress.setText(device.getAddress());
			viewHolder.deviceRssi.setText(rssi+"");
			return view;
		}
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView deviceRssi;
	}

	@Override
	public void LeScanCallback(final BluetoothDevice device, final int rssi) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (device != null) {
					SensorBleDevices mSensorBleDevices = new SensorBleDevices(device.getName(), device.getAddress(), rssi);
					mLeDeviceListAdapter.addDevice(mSensorBleDevices);
					mLeDeviceListAdapter.notifyDataSetChanged();
				}
			}
		});
	}
}