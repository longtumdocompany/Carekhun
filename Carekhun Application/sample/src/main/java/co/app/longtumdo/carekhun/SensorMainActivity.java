package co.app.longtumdo.carekhun;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yc.peddemo.sdk.BLEServiceOperate;
import com.yc.peddemo.sdk.BloodPressureChangeListener;
import com.yc.peddemo.sdk.BluetoothLeService;
import com.yc.peddemo.sdk.DataProcessing;
import com.yc.peddemo.sdk.ICallback;
import com.yc.peddemo.sdk.ICallbackStatus;
import com.yc.peddemo.sdk.OnServerCallbackListener;
import com.yc.peddemo.sdk.RateChangeListener;
import com.yc.peddemo.sdk.ServiceStatusCallback;
import com.yc.peddemo.sdk.SleepChangeListener;
import com.yc.peddemo.sdk.StepChangeListener;
import com.yc.peddemo.sdk.UTESQLOperate;
import com.yc.peddemo.sdk.WriteCommandToBLE;
import com.yc.peddemo.utils.CalendarUtils;
import com.yc.peddemo.utils.GlobalVariable;
import com.yc.pedometer.info.BPVOneDayInfo;
import com.yc.pedometer.info.RateOneDayInfo;
import com.yc.pedometer.info.SleepTimeInfo;
import com.yc.pedometer.info.SwimInfo;
import com.yc.pedometer.update.Updates;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorMainActivity extends AppCompatActivity implements OnClickListener, ICallback, ServiceStatusCallback, OnServerCallbackListener {

	private TextView connect_status, rssi_tv, tv_steps, tv_distance, tv_calorie, tv_sleep, tv_deep, tv_light, tv_awake, tv_rate, tv_lowest_rate, tv_verage_rate, tv_highest_rate;
	private EditText et_height, et_weight, et_sedentary_period;
	private Button btn_confirm, btn_sync_step, btn_sync_sleep, set_ble_time, bt_sedentary_open, bt_sedentary_close, btn_sync_rate, btn_rate_start, btn_rate_stop;

	private DataProcessing mDataProcessing;
	private SensorCustomProgressDialog mProgressDialog;
	private UTESQLOperate mySQLOperate;

	private WriteCommandToBLE mWriteCommand;
	private Context mContext;
	private SharedPreferences sp;
	private Editor editor;

	private final int UPDATE_STEP_UI_MSG = 0;
	private final int UPDATE_SLEEP_UI_MSG = 1;
	private final int DISCONNECT_MSG = 18;
	private final int CONNECTED_MSG = 19;
	private final int UPDATA_REAL_RATE_MSG = 20;
	private final int RATE_SYNC_FINISH_MSG = 21;
	private final int OPEN_CHANNEL_OK_MSG = 22;
	private final int CLOSE_CHANNEL_OK_MSG = 23;
	private final int TEST_CHANNEL_OK_MSG = 24;
	private final int OFFLINE_SWIM_SYNC_OK_MSG = 25;
	private final int UPDATA_REAL_BLOOD_PRESSURE_MSG = 29;
	private final int OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG = 30;
	private final int SERVER_CALL_BACK_OK_MSG = 31;
	private final long TIME_OUT_SERVER = 10000;
	private final long TIME_OUT = 120000;
	private boolean isUpdateSuccess = false;
	private int mSteps = 0;
	private float mDistance = 0f;
	private int mCalories = 0;

	private int mlastStepValue;
	private int stepDistance = 0;
	private int lastStepDistance = 0;

	private boolean isFirstOpenAPK = false;
	private int currentDay = 1;
	private int lastDay = 0;
	private String currentDayString = "20101202";
	private String lastDayString = "20101201";
	private static final int NEW_DAY_MSG = 3;
	protected static final String TAG = "SensorMainActivity";
	private Updates mUpdates;
	private BLEServiceOperate mBLEServiceOperate;
	private BluetoothLeService mBluetoothLeService;

	public static final String EXTRAS_DEVICE_NAME = "device_name";
	public static final String EXTRAS_DEVICE_ADDRESS = "device_address";
	private final int CONNECTED = 1;
	private final int CONNECTING = 2;
	private final int DISCONNECTED = 3;
	private int CURRENT_STATUS = DISCONNECTED;

	private String mDeviceName;
	private String mDeviceAddress;

	private int tempRate = 70;
	private int tempStatus;
	private long mExitTime = 0;
	private StringBuilder resultBuilder = new StringBuilder();

	private int high_pressure, low_pressure;
	private int tempBloodPressureStatus;

	//*******************************Insert to Firebase**********************************
	public int rssi;
	public String distance;
	public int calories;
	//***********************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor_activity_main);

		mContext = getApplicationContext();

		//SP
		sp = mContext.getSharedPreferences(GlobalVariable.SettingSP, 0);
		editor = sp.edit();

		//SQL
		mySQLOperate = new UTESQLOperate(mContext);

		//Operate Service
		mBLEServiceOperate = BLEServiceOperate.getInstance(mContext);

		//Callback
		mBLEServiceOperate.setServiceStatusCallback(this);

		//Instantiate the BLEServiceOperate
		mBluetoothLeService = mBLEServiceOperate.getBleService();
		if (mBluetoothLeService != null) {
			mBluetoothLeService.setICallback(this);
		}

		//Receiver
		mRegisterReceiver();
		mfindViewById();

		//Write Command to Weareable
		mWriteCommand = WriteCommandToBLE.getInstance(mContext);

		//Update
		mUpdates = Updates.getInstance(mContext);
		mUpdates.setHandler(mHandler);
		mUpdates.registerBroadcastReceiver();
		mUpdates.setOnServerCallbackListener(this);
		Log.d("onServerDiscorver", "MainActivity_onCreate   mUpdates  =" + mUpdates);

		Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);

		//Mac Address
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		//Connect
		mBLEServiceOperate.connect(mDeviceAddress);
		CURRENT_STATUS = CONNECTING;

		//Swim
		upDateTodaySwimData();
	}

	private void mRegisterReceiver() {
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(GlobalVariable.READ_BATTERY_ACTION);
		mFilter.addAction(GlobalVariable.READ_BLE_VERSION_ACTION);
		registerReceiver(mReceiver, mFilter);
	}

	private void mfindViewById() {

		//ความสูง/น้ำหนัก
		et_height = (EditText) findViewById(R.id.et_height);
		et_weight = (EditText) findViewById(R.id.et_weight);

		et_sedentary_period = (EditText) findViewById(R.id.et_sedentary_period);

		//Connect
		connect_status = (TextView) findViewById(R.id.connect_status);

		//Rssi
		rssi_tv = (TextView) findViewById(R.id.rssi_tv);

		//ข้อมูลการเดิน/การวิ่ง
//		tv_steps = (TextView) findViewById(R.id.tv_steps);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		tv_calorie = (TextView) findViewById(R.id.tv_calorie);

		//ข้อมูลการนอน
		tv_sleep = (TextView) findViewById(R.id.tv_sleep);
		tv_deep = (TextView) findViewById(R.id.tv_deep);
		tv_light = (TextView) findViewById(R.id.tv_light);
		tv_awake = (TextView) findViewById(R.id.tv_awake);

		//ข้อมูลอัตราการเต้นของหัวใจ
		tv_rate = (TextView) findViewById(R.id.tv_rate);
		tv_lowest_rate = (TextView) findViewById(R.id.tv_lowest_rate);
		tv_verage_rate = (TextView) findViewById(R.id.tv_verage_rate);
		tv_highest_rate = (TextView) findViewById(R.id.tv_highest_rate);

		//ปุ่ม
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		bt_sedentary_open = (Button) findViewById(R.id.bt_sedentary_open);
		bt_sedentary_close = (Button) findViewById(R.id.bt_sedentary_close);
		btn_sync_step = (Button) findViewById(R.id.btn_sync_step);
		btn_sync_sleep = (Button) findViewById(R.id.btn_sync_sleep);
		btn_sync_rate = (Button) findViewById(R.id.btn_sync_rate);
		btn_rate_start = (Button) findViewById(R.id.btn_rate_start);
		btn_rate_stop = (Button) findViewById(R.id.btn_rate_stop);

		//Listener
		btn_confirm.setOnClickListener(this);
		bt_sedentary_open.setOnClickListener(this);
		bt_sedentary_close.setOnClickListener(this);
		btn_sync_step.setOnClickListener(this);
		btn_sync_sleep.setOnClickListener(this);
		btn_sync_rate.setOnClickListener(this);
		btn_rate_start.setOnClickListener(this);
		btn_rate_stop.setOnClickListener(this);
		set_ble_time = (Button) findViewById(R.id.set_ble_time);
		set_ble_time.setOnClickListener(this);

		//ตั้งค่าคามสูงและน้ำหนัก
		et_height.setText(sp.getString(GlobalVariable.PERSONAGE_HEIGHT, "175"));
		et_weight.setText(sp.getString(GlobalVariable.PERSONAGE_WEIGHT, "60"));

		//Processing
		mDataProcessing = DataProcessing.getInstance(mContext);
		mDataProcessing.setOnStepChangeListener(mOnStepChangeListener);
		mDataProcessing.setOnSleepChangeListener(mOnSleepChangeListener);
		mDataProcessing.setOnRateListener(mOnRateListener);
		mDataProcessing.setOnBloodPressureListener(mOnBloodPressureListener);
		Log.d("onStepHandler", "main_mDataProcessing =" + mDataProcessing);

		//Open Alarm
		Button open_alarm = (Button) findViewById(R.id.open_alarm);
		open_alarm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWriteCommand.sendToSetAlarmCommand(1, GlobalVariable.EVERYDAY, 16, 25, true);
			}
		});

		//Close Alarm
		Button close_alarm = (Button) findViewById(R.id.close_alarm);
		close_alarm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWriteCommand.sendToSetAlarmCommand(1, GlobalVariable.EVERYDAY, 16, 23, false);
			}
		});

//		unit = (Button) findViewById(R.id.unit);
//		unit.setOnClickListener(this);

	}

	/**
	 * Step monitor การเดินและการวิ่ง
	 */
	private StepChangeListener mOnStepChangeListener = new StepChangeListener() {

		@Override
		public void onStepChange(int steps, float distance, int calories) {
			Log.d("onStepHandler", "steps =" + steps + ",distance =" + distance + ",calories =" + calories);
			mSteps = steps;
			mDistance = distance;
			mCalories = calories;
			mHandler.sendEmptyMessage(UPDATE_STEP_UI_MSG);
		}

	};

	/**
	 * Sleep monitor การนอน
	 */
	private SleepChangeListener mOnSleepChangeListener = new SleepChangeListener() {

		@Override
		public void onSleepChange() {
			mHandler.sendEmptyMessage(UPDATE_SLEEP_UI_MSG);
		}

	};

	/**
	 * Sleep monitor การเต้นของหัวใจ
	 */
	private RateChangeListener mOnRateListener = new RateChangeListener() {

		@Override
		public void onRateChange(int rate, int status) {
			tempRate = rate;
			tempStatus = status;
			Log.i("BluetoothLeService", "Rate_tempRate =" + tempRate);
			mHandler.sendEmptyMessage(UPDATA_REAL_RATE_MSG);
		}
	};

	/**
	 * Sleep monitor ความดัน
	 */
	private BloodPressureChangeListener mOnBloodPressureListener = new BloodPressureChangeListener() {

		@Override
		public void onBloodPressureChange(int hightPressure, int lowPressure,
				int status) {
			tempBloodPressureStatus = status;
			high_pressure = hightPressure;
			low_pressure = lowPressure;
			mHandler.sendEmptyMessage(UPDATA_REAL_BLOOD_PRESSURE_MSG);

		}

	};

	//*******************************Handler Update Process and Message**************************************
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			//Sync Weareable
			case RATE_SYNC_FINISH_MSG:
				UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0));
				Toast.makeText(mContext, "Rate sync finish", Toast.LENGTH_LONG).show();
				break;

			//Update Data Heart Rate
			case UPDATA_REAL_RATE_MSG:
				tv_rate.setText(tempRate + "");
				if (tempStatus == GlobalVariable.RATE_TEST_FINISH) {
					UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0));
					Toast.makeText(mContext, "Rate test finish", Toast.LENGTH_LONG).show();
				}
				break;

			//Get Rssi
			case GlobalVariable.GET_RSSI_MSG:
				Bundle bundle = msg.getData();
				rssi_tv.setText(bundle.getInt(GlobalVariable.EXTRA_RSSI) + "");
				rssi = bundle.getInt(GlobalVariable.EXTRA_RSSI);
				break;

			//Update Step
			case UPDATE_STEP_UI_MSG:
				updateSteps(mSteps);
				updateCalories(mCalories);
				updateDistance(mDistance);
				Log.d("onStepHandler", "mSteps =" + mSteps + ",mDistance =" + mDistance + ",mCalories =" + mCalories);
				break;

			//Update Sleep Data
			case UPDATE_SLEEP_UI_MSG:
				querySleepInfo();
				Log.d("getSleepInfo", "UPDATE_SLEEP_UI_MSG");
				break;

			//Update SQL Data
			case NEW_DAY_MSG:
				mySQLOperate.updateStepSQL();
				mySQLOperate.updateSleepSQL();
				mySQLOperate.updateRateSQL();
				mySQLOperate.isDeleteRefreshTable();
				resetValues();
				break;

			//Start Progress
			case GlobalVariable.START_PROGRESS_MSG:
				Log.i(TAG, "(Boolean) msg.obj=" + (Boolean) msg.obj);
				isUpdateSuccess = (Boolean) msg.obj;
				Log.i(TAG, "BisUpdateSuccess=" + isUpdateSuccess);
				startProgressDialog();
				mHandler.postDelayed(mDialogRunnable, TIME_OUT);
				break;

			//Download Image
			case GlobalVariable.DOWNLOAD_IMG_FAIL_MSG:
				Toast.makeText(SensorMainActivity.this, R.string.download_fail, Toast.LENGTH_LONG).show();
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (mDialogRunnable != null)
					mHandler.removeCallbacks(mDialogRunnable);
				break;

			//Dismiss Update Weareable
			case GlobalVariable.DISMISS_UPDATE_BLE_DIALOG_MSG:
				Log.i(TAG, "(Boolean) msg.obj=" + (Boolean) msg.obj);
				isUpdateSuccess = (Boolean) msg.obj;
				Log.i(TAG, "BisUpdateSuccess=" + isUpdateSuccess);

				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}

				if (mDialogRunnable != null) {
					mHandler.removeCallbacks(mDialogRunnable);
				}

				if (isUpdateSuccess) {
					Toast.makeText(mContext, getResources().getString(
									R.string.ble_update_successful), Toast.LENGTH_LONG).show();
				}
				break;

			//Server Busy
			case GlobalVariable.SERVER_IS_BUSY_MSG:
				Toast.makeText(mContext, getResources().getString(R.string.server_is_busy), Toast.LENGTH_LONG).show();
				break;

			//Disconnect Weareable
			case DISCONNECT_MSG:
				connect_status.setText(getString(R.string.disconnect));
				CURRENT_STATUS = DISCONNECTED;
				Toast.makeText(mContext, "disconnect or connect falie", Toast.LENGTH_LONG).show();

				//Set 00:00:00:00:00:00
				String lastConnectAddr0 = sp.getString(GlobalVariable.LAST_CONNECT_DEVICE_ADDRESS_SP, "00:00:00:00:00:00");
				boolean connectResute0 = mBLEServiceOperate.connect(lastConnectAddr0);
				Log.i(TAG, "connectResute0=" + connectResute0);

				break;

			//Connect Weareable
			case CONNECTED_MSG:
				connect_status.setText(getString(R.string.connected));
				mBluetoothLeService.setRssiHandler(mHandler);
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (!Thread.interrupted()) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (mBluetoothLeService != null) {
								mBluetoothLeService.readRssi();
							}
						}
					}
				}).start();
				CURRENT_STATUS = CONNECTED;
				Toast.makeText(mContext, "connected", Toast.LENGTH_LONG).show();
				break;

			//Firmware upgrade progress
			case GlobalVariable.UPDATE_BLE_PROGRESS_MSG:
				int schedule = msg.arg1;
				Log.i("zznkey", "schedule =" + schedule);
				if (mProgressDialog == null) {
					startProgressDialog();
				}
				mProgressDialog.setSchedule(schedule);
				break;

			//Open
			case OPEN_CHANNEL_OK_MSG:
				resultBuilder.append(getResources().getString(
						R.string.open_channel_ok) + ",");

				//Command >> APDU เลข Hardware
				mWriteCommand.sendAPDUToBLE(WriteCommandToBLE.hexString2Bytes(testKey1));
				break;

			//Close Channel
			case CLOSE_CHANNEL_OK_MSG:
				resultBuilder.append(getResources().getString(
						R.string.close_channel_ok) + ",");
				break;

			//Test Channel
			case TEST_CHANNEL_OK_MSG:
				resultBuilder.append(getResources().getString(
						R.string.test_channel_ok) + ",");
				mWriteCommand.closeBLEchannel();
				break;

			//Set Password
			case SHOW_SET_PASSWORD_MSG:
				showPasswordDialog(GlobalVariable.PASSWORD_TYPE_SET);
				break;

			//Input Password
			case SHOW_INPUT_PASSWORD_MSG:
				showPasswordDialog(GlobalVariable.PASSWORD_TYPE_INPUT);
				break;

			//Input Password Again
			case SHOW_INPUT_PASSWORD_AGAIN_MSG:
				showPasswordDialog(GlobalVariable.PASSWORD_TYPE_INPUT_AGAIN);
				break;

			//Update Swim Data Offline
			case OFFLINE_SWIM_SYNC_OK_MSG:
				upDateTodaySwimData();
				Toast.makeText(SensorMainActivity.this, getResources().getString(R.string.sync_swim_finish), Toast.LENGTH_LONG).show();
				break;

			//Update Blood Data Online
			case UPDATA_REAL_BLOOD_PRESSURE_MSG:
				if (tempBloodPressureStatus == GlobalVariable.BLOOD_PRESSURE_TEST_FINISH) {
					UpdateBloodPressureMainUI(CalendarUtils.getCalendar(0));
					Toast.makeText(mContext, getResources().getString(R.string.test_pressure_ok), Toast.LENGTH_LONG).show();
				}
				break;

			//Update Blood Data Offline
			case OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG:
				UpdateBloodPressureMainUI(CalendarUtils.getCalendar(0));
				Toast.makeText(SensorMainActivity.this, getResources().getString(R.string.sync_pressure_ok), Toast.LENGTH_LONG).show();
				break;

			//Server Callback
			case SERVER_CALL_BACK_OK_MSG:
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (mDialogServerRunnable != null) {
					mHandler.removeCallbacks(mDialogServerRunnable);
				}
				String localVersion = sp.getString(GlobalVariable.IMG_LOCAL_VERSION_NAME_SP, "0");
				int status = mUpdates.getBLEVersionStatus(localVersion);
				Log.d(TAG, "Firmware upgrade VersionStatus =" + status);
				if (status == GlobalVariable.OLD_VERSION_STATUS) {
					updateBleDialog();
				} else if (status == GlobalVariable.NEWEST_VERSION_STATUS) {
					Toast.makeText(mContext, getResources().getString(R.string.ble_is_newest), Toast.LENGTH_LONG).show();
				}
				break;
			default:
				break;
			}
		}
	};

	/*
	 * อัตราการเต้นของหัวใจ
	 */
	private void UpdateUpdataRateMainUI(String calendar) {
		UTESQLOperate mySQLOperate = new UTESQLOperate(mContext);
		RateOneDayInfo mRateOneDayInfo = mySQLOperate.queryRateOneDayMainInfo(calendar);
		if (mRateOneDayInfo != null) {
			int currentRate = mRateOneDayInfo.getCurrentRate();
			int lowestValue = mRateOneDayInfo.getLowestRate();
			int averageValue = mRateOneDayInfo.getVerageRate();
			int highestValue = mRateOneDayInfo.getHighestRate();

			//***************************Initial Firebase Database*********************************
			DatabaseReference database = FirebaseDatabase.getInstance().getReference();

			// current_rate.setText(currentRate + "");
			if (currentRate == 0) {
				tv_rate.setText("--");
			} else {
				tv_rate.setText(currentRate + "");

				//***************************Start Firebase Database*********************************
				int height = 175;
				int weight = 60;
				int sedentaryRemind = 60;

				//INSERT DATA TO FIREBASE
				DatabaseReference usersRef = database.child("hearthRate");
				Map<String, CareData> users = new HashMap<String, CareData>();
				users.put("heart_rate", new CareData(height,weight,sedentaryRemind,distance,calories,currentRate,rssi));
				usersRef.setValue(users);
				//***************************End Firebase Database*********************************
			}
			if (lowestValue == 0) {
				tv_lowest_rate.setText("--");
			} else {
				tv_lowest_rate.setText(lowestValue + "");
			}
			if (averageValue == 0) {
				tv_verage_rate.setText("--");
			} else {
				tv_verage_rate.setText(averageValue + "");
			}
			if (highestValue == 0) {
				tv_highest_rate.setText("--");
			} else {
				tv_highest_rate.setText(highestValue + "");
			}
		} else {
			tv_rate.setText("--");
		}
	}

	/*
	 * เรียกดูข้อมูลใน 1 วัน
	 */
	private void getOneDayRateinfo(String calendar) {
		UTESQLOperate mySQLOperate = new UTESQLOperate(mContext);
		List<RateOneDayInfo> mRateOneDayInfoList = mySQLOperate.queryRateOneDayDetailInfo(calendar);
		if (mRateOneDayInfoList != null && mRateOneDayInfoList.size() > 0) {
			int size = mRateOneDayInfoList.size();
			int[] rateValue = new int[size];
			int[] timeArray = new int[size];
			for (int i = 0; i < size; i++) {
				rateValue[i] = mRateOneDayInfoList.get(i).getRate();
				timeArray[i] = mRateOneDayInfoList.get(i).getTime();
				Log.d(TAG, "rateValue[" + i + "]=" + rateValue[i] + "timeArray[" + i + "]=" + timeArray[i]);
			}
		}
	}

	//*************************************Start Progress Bar******************************************
	private void startProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = SensorCustomProgressDialog.createDialog(SensorMainActivity.this);
			mProgressDialog.setMessage(getResources().getString(
					R.string.ble_updating));
			mProgressDialog.setCancelable(false);
			mProgressDialog.setCanceledOnTouchOutside(false);
		}
		mProgressDialog.show();
	}

	private Runnable mDialogRunnable = new Runnable() {

		@Override
		public void run() {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mHandler.removeCallbacks(mDialogRunnable);
			if (!isUpdateSuccess) {
				Toast.makeText(SensorMainActivity.this, getResources().getString(R.string.ble_fail_update), Toast.LENGTH_LONG).show();
				mUpdates.clearUpdateSetting();
			} else {
				isUpdateSuccess = false;
				Toast.makeText(SensorMainActivity.this, getResources()
								.getString(R.string.ble_update_successful), Toast.LENGTH_LONG).show();
			}

		}
	};
	private Runnable mDialogServerRunnable = new Runnable() {

		@Override
		public void run() {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mHandler.removeCallbacks(mDialogServerRunnable);
			Toast.makeText(SensorMainActivity.this, getResources().getString(R.string.server_is_busy), Toast.LENGTH_LONG).show();
		}
	};

	//*************************************Update Step การเดินการวิ่ง******************************************
	private void updateSteps(int steps) {
		stepDistance = steps - mlastStepValue;
		Log.d("upDateSteps", "stepDistance =" + stepDistance + ",lastStepDistance=" + lastStepDistance + ",steps =" + steps);
		if (stepDistance > 3 || stepDistance < 0) {			//STEP < 0 หรือ > 3
			if (tv_distance != null) {
				if (steps <= 0) {
//					tv_steps.setText("0");
					Log.d("upDateSteps","0");
				} else {
//					tv_steps.setText("" + steps);
					Log.d("upDateSteps","steps :"+steps);
				}
			}
		}

		//STEP 0 - 3
		else {
			switch (stepDistance) {
			case 0:
				switch (lastStepDistance) {
				case 0:
//					if (tv_steps != null) {
//						if (steps <= 0) {
//							tv_steps.setText("0");
//						} else {
//							try {
//								Thread.sleep(400);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//							tv_steps.setText("" + steps);
//						}
//					}
					break;
				case 1:
//					if (tv_steps != null) {
//						if (steps <= 0) {
//							tv_steps.setText("0");
//						} else {
//							tv_steps.setText("" + steps);
//						}
//					}
					break;
				case 2:
//					if (tv_steps != null) {
//						if (steps <= 0) {
//							tv_steps.setText("0");
//						} else {
//							try {
//								Thread.sleep(400);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//							tv_steps.setText("" + steps);
//						}
//					}
					break;
				case 3:
//					if (tv_steps != null) {
//						if (steps <= 0) {
//							tv_steps.setText("0");
//						} else {
//							try {
//								Thread.sleep(200);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//							tv_steps.setText("" + (steps - 1));
//						}
//					}
					break;
				default:
					break;
				}
				break;
			case 1:
//				if (tv_steps != null) {
//					if (steps <= 0) {
//						tv_steps.setText("0");
//					} else {
//						tv_steps.setText("" + steps);
//					}
//				}
				break;
			case 2:
//				if (tv_steps != null) {
//					if (steps <= 0) {
//						tv_steps.setText("0");
//					} else {
//						tv_steps.setText("" + (steps - 1));
//					}
//				}
				break;
			case 3:
//				if (tv_steps != null) {
//					if (steps <= 0) {
//						tv_steps.setText("0");
//					} else {
//
//						tv_steps.setText("" + (steps - 2));
//					}
//				}
				break;
			default:
				break;
			}
		}
		mlastStepValue = steps;
		lastStepDistance = stepDistance;

	}

	private void updateCalories(int mCalories) {
		if (mCalories <= 0) {
			tv_calorie.setText(mContext.getResources().getString(
					R.string.zero_kilocalorie));
		} else {
			tv_calorie.setText("" + (int) mCalories + " " + mContext.getResources().getString(R.string.kilocalorie));

			//Save ลง Firebase
			calories = (int) mCalories;
		}

	}

	private void updateDistance(float mDistance) {
		if (mDistance < 0.01) {
			tv_distance.setText(mContext.getResources().getString(
					R.string.zero_kilometers));

			//Save ลง Firebase
			distance = "0 km";

		} else if (mDistance >= 100) {
			tv_distance.setText(("" + mDistance).substring(0, 3) + " " + mContext.getResources().getString(R.string.kilometers));

			//Save ลง Firebase
			distance = ("" + mDistance).substring(0, 3) + " " + "km";

		} else {
			tv_distance.setText(("" + (mDistance + 0.000001f)).substring(0, 4) + " " + mContext.getResources().getString(R.string.kilometers));

			//Save ลง Firebase
			distance = ("" + (mDistance + 0.000001f)).substring(0, 4) + " " + mContext.getResources().getString(R.string.kilometers);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		//เชื่อมต่อและหยุดการเชื่อมต่อ
		boolean ble_connecte = sp.getBoolean(GlobalVariable.BLE_CONNECTED_SP, false);
		if (ble_connecte) {
			connect_status.setText(getString(R.string.connected));
		} else {
			connect_status.setText(getString(R.string.disconnect));
		}

		//Insert Data
		JudgeNewDayWhenResume();

	}

	private void JudgeNewDayWhenResume() {
		isFirstOpenAPK = sp.getBoolean(GlobalVariable.FIRST_OPEN_APK, true);

		editor.putBoolean(GlobalVariable.FIRST_OPEN_APK, false);
		editor.commit();

		lastDay = sp.getInt(GlobalVariable.LAST_DAY_NUMBER_SP, 0);
		lastDayString = sp.getString(GlobalVariable.LAST_DAY_CALLENDAR_SP, "20101201");
		Calendar c = Calendar.getInstance();
		currentDay = c.get(Calendar.DAY_OF_YEAR);
		currentDayString = CalendarUtils.getCalendar(0);

		if (isFirstOpenAPK) {
			lastDay = currentDay;
			lastDayString = currentDayString;
			editor = sp.edit();

			editor.putInt(GlobalVariable.LAST_DAY_NUMBER_SP, lastDay);
			editor.putString(GlobalVariable.LAST_DAY_CALLENDAR_SP, lastDayString);
			editor.commit();

		} else {
			if (currentDay != lastDay) {
				if ((lastDay + 1) == currentDay || currentDay == 1) { 		// Continuous date
					mHandler.sendEmptyMessage(NEW_DAY_MSG);
				} else {
					mySQLOperate.insertLastDayStepSQL(lastDayString);
					mySQLOperate.updateSleepSQL();
					resetValues();
				}
				lastDay = currentDay;
				lastDayString = currentDayString;
				editor.putInt(GlobalVariable.LAST_DAY_NUMBER_SP, lastDay);
				editor.putString(GlobalVariable.LAST_DAY_CALLENDAR_SP, lastDayString);
				editor.commit();
			} else {
				Log.d("b1offline", "currentDay == lastDay");
			}
		}

	}

	//************************************Reset Value********************************************
	private void resetValues() {
		editor.putInt(GlobalVariable.YC_PED_UNFINISH_HOUR_STEP_SP, 0);
		editor.putInt(GlobalVariable.YC_PED_UNFINISH_HOUR_VALUE_SP, 0);
		editor.putInt(GlobalVariable.YC_PED_LAST_HOUR_STEP_SP, 0);
		editor.commit();

		//***************Set Text*****************
//		tv_steps.setText("0");
		tv_calorie.setText(mContext.getResources().getString(
				R.string.zero_kilocalorie));
		tv_distance.setText(mContext.getResources().getString(
				R.string.zero_kilometers));
		tv_sleep.setText("0");
		tv_deep.setText(mContext.getResources().getString(
				R.string.zero_hour_zero_minute));
		tv_light.setText(mContext.getResources().getString(
				R.string.zero_hour_zero_minute));
		tv_awake.setText(mContext.getResources().getString(R.string.zero_count));
		tv_rate.setText("--");
		tv_lowest_rate.setText("--");
		tv_verage_rate.setText("--");
		tv_highest_rate.setText("--");
	}


	//*******************************************ON CLICK***********************************************
	@Override
	public void onClick(View v) {
		boolean ble_connecte = sp.getBoolean(GlobalVariable.BLE_CONNECTED_SP, false);
		switch (v.getId()) {

		case R.id.btn_confirm:
			if (ble_connecte) {
				String height = et_height.getText().toString();
				String weight = et_weight.getText().toString();

				//Validate
				if (height.equals("") || weight.equals("")) {
					Toast.makeText(mContext, "Height or weight can not be empty", Toast.LENGTH_LONG).show();
				} else {

					//ความสูงและน้ำหนัก
					int Height = Integer.valueOf(height);
					int Weight = Integer.valueOf(weight);

					//Write Command to Weareable
					mWriteCommand.sendStepLenAndWeightToBLE(Height, Weight, 5, 10000, true, true, 150);
				}
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.bt_sedentary_open:
			String period = et_sedentary_period.getText().toString();
			if (period.equals("")) {
				Toast.makeText(mContext, "Please input remind peroid", Toast.LENGTH_LONG).show();
			} else {
				int period_time = Integer.valueOf(period);
				if (period_time < 30) {
					Toast.makeText(mContext, "Please make sure period_time more than 30 minutes", Toast.LENGTH_LONG).show();
				} else {
					if (ble_connecte) {
						mWriteCommand.sendSedentaryRemindCommand(GlobalVariable.OPEN_SEDENTARY_REMIND, period_time);
					} else {
						Toast.makeText(mContext, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
					}
				}
			}
			break;
		case R.id.bt_sedentary_close:
			if (ble_connecte) {
				mWriteCommand.sendSedentaryRemindCommand(GlobalVariable.CLOSE_SEDENTARY_REMIND, 0);
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_sync_step:
			if (ble_connecte) {
				mWriteCommand.syncAllStepData();
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_sync_sleep:
			if (ble_connecte) {
				mWriteCommand.syncAllSleepData();
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_sync_rate:
			if (ble_connecte) {
				mWriteCommand.syncAllRateData();
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_rate_start:
			if (ble_connecte) {
				mWriteCommand.sendRateTestCommand(GlobalVariable.RATE_TEST_START);
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_rate_stop:
			if (ble_connecte) {
				mWriteCommand.sendRateTestCommand(GlobalVariable.RATE_TEST_STOP);
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.set_ble_time:
			if (ble_connecte) {
				mWriteCommand.syncBLETime();
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
			}
			break;
//		case R.id.unit:
//			boolean ble_connected3 = sp.getBoolean(GlobalVariable.BLE_CONNECTED_SP, false);
//			if (ble_connected3) {
//				if (unit.getText().toString().equals(getResources()
//								.getString(R.string.metric_system))) {
//					editor.putBoolean(GlobalVariable.IS_METRIC_UNIT_SP, true);
//					editor.commit();
//					mWriteCommand.sendUnitToBLE();
//					unit.setText(getResources().getString(R.string.inch_system));
//				} else {
//					editor.putBoolean(GlobalVariable.IS_METRIC_UNIT_SP, false);
//					editor.commit();
//					mWriteCommand.sendUnitToBLE();
//					unit.setText(getResources().getString(
//							R.string.metric_system));
//				}
//			} else {
//				Toast.makeText(mContext, getResources().getString(
//								R.string.please_connect_bracelet), Toast.LENGTH_LONG).show();
//			}
//			break;
		default:
			break;
		}
	}


	//*******************************************KEY DOWN*****************************************
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		//BLUETOOTH CONNECT
		if (CURRENT_STATUS == CONNECTING) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage("Device connection, forced exit will turn off Bluetooth, confirm?");
			builder.setTitle(mContext.getResources().getString(R.string.tip));
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
							if (mBluetoothAdapter == null) {
								finish();
							}
							if (mBluetoothAdapter.isEnabled()) {
								mBluetoothAdapter.disable();			// Turn off Bluetooth
							}
							finish();
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	//**********************************************UPDATE VERSION*****************************************
	private boolean updateBleDialog() {

		final AlertDialog alert = new AlertDialog.Builder(this).setCancelable(false).create();
		alert.show();
		window = alert.getWindow();
		window.setContentView(R.layout.sensor_update_dialog_layout);
		Button btn_yes = (Button) window.findViewById(R.id.btn_yes);
		Button btn_no = (Button) window.findViewById(R.id.btn_no);
		TextView update_warn_tv = (TextView) window.findViewById(R.id.update_warn_tv);
		update_warn_tv.setText(getResources().getString(R.string.find_new_version_ble));

		btn_yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isNetworkAvailable(mContext)) {
					mUpdates.startUpdateBLE();
				} else {
					Toast.makeText(mContext, getResources().getString(
									R.string.confire_is_network_available), Toast.LENGTH_LONG).show();
				}
				alert.dismiss();
			}
		});

		btn_no.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mUpdates.clearUpdateSetting();
				alert.dismiss();
			}
		});

		return false;
	}

	//เรียกข้อมูลการนอน
	private void querySleepInfo() {
		SleepTimeInfo sleepTimeInfo = mySQLOperate.querySleepInfo(CalendarUtils.getCalendar(-1), CalendarUtils.getCalendar(0));
		int deepTime, lightTime, awakeCount, sleepTotalTime;
		if (sleepTimeInfo != null) {
			deepTime = sleepTimeInfo.getDeepTime();
			lightTime = sleepTimeInfo.getLightTime();
			awakeCount = sleepTimeInfo.getAwakeCount();
			sleepTotalTime = sleepTimeInfo.getSleepTotalTime();

			int[] colorArray = sleepTimeInfo.getSleepStatueArray();				// Different sleep states in the drawing can be expressed in different colors, and the colors are customizable
			int[] timeArray = sleepTimeInfo.getDurationTimeArray();
			int[] timePointArray = sleepTimeInfo.getTimePointArray();

			Log.d("getSleepInfo", "Calendar=" + CalendarUtils.getCalendar(0)
					+ ",timeArray =" + timeArray + ",timeArray.length ="
					+ timeArray.length + ",colorArray =" + colorArray
					+ ",colorArray.length =" + colorArray.length
					+ ",timePointArray =" + timePointArray
					+ ",timePointArray.length =" + timePointArray.length);

			//ชั่วโมง
			double total_hour = ((float) sleepTotalTime / 60f);
			DecimalFormat df1 = new DecimalFormat("0.0"); 						// Reserved 1 decimal, with leading zero

			int deep_hour = deepTime / 60;
			int deep_minute = (deepTime - deep_hour * 60);

			int light_hour = lightTime / 60;
			int light_minute = (lightTime - light_hour * 60);

			int active_count = awakeCount;
			String total_hour_str = df1.format(total_hour);

			if (total_hour_str.equals("0.0")) {
				total_hour_str = "0";
			}

			tv_sleep.setText(total_hour_str);
			tv_deep.setText(deep_hour + " " + mContext.getResources().getString(R.string.hour) + " " + deep_minute + " " + mContext.getResources().getString(R.string.minute));
			tv_light.setText(light_hour + " " + mContext.getResources().getString(R.string.hour) + " " + light_minute + " " + mContext.getResources().getString(R.string.minute));
			tv_awake.setText(active_count + " " + mContext.getResources().getString(R.string.count));

		} else {
			Log.d("getSleepInfo", "sleepTimeInfo =" + sleepTimeInfo);
			tv_sleep.setText("0");
			tv_deep.setText(mContext.getResources().getString(
					R.string.zero_hour_zero_minute));
			tv_light.setText(mContext.getResources().getString(
					R.string.zero_hour_zero_minute));
			tv_awake.setText(mContext.getResources().getString(
					R.string.zero_count));
		}
	}

	//**************************************ฺBLUETOOTH BroadcastReceiver******************************************
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(GlobalVariable.READ_BLE_VERSION_ACTION)) {
				String version = intent.getStringExtra(GlobalVariable.INTENT_BLE_VERSION_EXTRA);
//				if (sp.getBoolean(BluetoothLeService.IS_RK_PLATFORM_SP, false)) {
//					show_result.setText("version=" + version + "," + sp.getString(GlobalVariable.PATH_LOCAL_VERSION_NAME_SP, ""));
//				} else {
//					show_result.setText("version=" + version);
//				}
			} else if (action.equals(GlobalVariable.READ_BATTERY_ACTION)) {
				int battery = intent.getIntExtra(GlobalVariable.INTENT_BLE_BATTERY_EXTRA, -1);
			}
		}
	};

	private Window window;

	private boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else {
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("onServerDiscorver", "MainActivity_onDestroy");
		GlobalVariable.BLE_UPDATE = false;
		mUpdates.unRegisterBroadcastReceiver();
		try {
			unregisterReceiver(mReceiver);
		} catch (Exception e) { e.printStackTrace(); }

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		if (mDialogRunnable != null)
			mHandler.removeCallbacks(mDialogRunnable);

		mBLEServiceOperate.disConnect();
	}

	@Override
	public void OnResult(boolean result, int status) {
		// TODO Auto-generated method stub
		Log.i(TAG, "result=" + result + ",status=" + status);
		if (status == ICallbackStatus.OFFLINE_STEP_SYNC_OK) {
			// step snyc complete
		} else if (status == ICallbackStatus.OFFLINE_SLEEP_SYNC_OK) {
			// sleep snyc complete
		} else if (status == ICallbackStatus.SYNC_TIME_OK) {// after set time finish, then(or delay 20ms) send to read localBleVersion
															// mWriteCommand.sendToReadBLEVersion();
		} else if (status == ICallbackStatus.GET_BLE_VERSION_OK) {	// after read localBleVersion finish, then sync step
																	// mWriteCommand.syncAllStepData();
		} else if (status == ICallbackStatus.DISCONNECT_STATUS) {
			mHandler.sendEmptyMessage(DISCONNECT_MSG);
		} else if (status == ICallbackStatus.CONNECTED_STATUS) {
			mHandler.sendEmptyMessage(CONNECTED_MSG);
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					mWriteCommand.sendToQueryPasswardStatus();

				}
			}, 150);
		} else if (status == ICallbackStatus.DISCOVERY_DEVICE_SHAKE) {
		} else if (status == ICallbackStatus.OFFLINE_RATE_SYNC_OK) {
			mHandler.sendEmptyMessage(RATE_SYNC_FINISH_MSG);
		} else if (status == ICallbackStatus.SET_METRICE_OK) {					// Set metric units to success

		} else if (status == ICallbackStatus.SET_METRICE_OK) {					// Set the imperial unit to be successful

		} else if (status == ICallbackStatus.SET_FIRST_ALARM_CLOCK_OK) {		// Set the first alarm OK

		} else if (status == ICallbackStatus.SET_SECOND_ALARM_CLOCK_OK) {		// Set the second alarm OK

		} else if (status == ICallbackStatus.SET_THIRD_ALARM_CLOCK_OK) {		// Set the third alarm OK

		} else if (status == ICallbackStatus.SEND_PHONE_NAME_NUMBER_OK) {
			mWriteCommand.sendQQWeChatVibrationCommand(5);
		} else if (status == ICallbackStatus.SEND_QQ_WHAT_SMS_CONTENT_OK) {
			mWriteCommand.sendQQWeChatVibrationCommand(1);
		} else if (status == ICallbackStatus.PASSWORD_SET) {
			Log.d(TAG, "If you have not set a password, set a 4-digit password");
			mHandler.sendEmptyMessage(SHOW_SET_PASSWORD_MSG);
		} else if (status == ICallbackStatus.PASSWORD_INPUT) {
			Log.d(TAG, "A password has been set. Please enter a 4-digit password");
			mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_MSG);
		} else if (status == ICallbackStatus.PASSWORD_AUTHENTICATION_OK) {
			Log.d(TAG, "Verify successful or set password successfully");
		} else if (status == ICallbackStatus.PASSWORD_INPUT_AGAIN) {
			Log.d(TAG, "Verification failed or set password failed, please re-enter the 4-digit password, if you have set a password, please enter the password has been set");
			mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_AGAIN_MSG);
		} else if (status == ICallbackStatus.OFFLINE_SWIM_SYNCING) {
			Log.d(TAG, "Swimming data in sync");
		} else if (status == ICallbackStatus.OFFLINE_SWIM_SYNC_OK) {
			Log.d(TAG, "Swimming data is synchronized");
			mHandler.sendEmptyMessage(OFFLINE_SWIM_SYNC_OK_MSG);
		} else if (status == ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNCING) {
			Log.d(TAG, "Blood pressure data synchronization");
		} else if (status == ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK) {
			Log.d(TAG, "Blood pressure data is synchronized");
			mHandler.sendEmptyMessage(OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG);
		}
	}

	private final String testKey1 = "00a4040008A000000333010101";

	@Override
	public void OnDataResult(boolean result, int status, byte[] data) {
		StringBuilder stringBuilder = null;
		if (data != null && data.length > 0) {
			stringBuilder = new StringBuilder(data.length);
			for (byte byteChar : data) {
				stringBuilder.append(String.format("%02X", byteChar));
			}
			Log.i("testChannel", "BLE---->APK data =" + stringBuilder.toString());
		}
		if (status == ICallbackStatus.OPEN_CHANNEL_OK) {					// Open the channel OK
			mHandler.sendEmptyMessage(OPEN_CHANNEL_OK_MSG);
		} else if (status == ICallbackStatus.CLOSE_CHANNEL_OK) {			// close the channel OK
			mHandler.sendEmptyMessage(CLOSE_CHANNEL_OK_MSG);
		} else if (status == ICallbackStatus.BLE_DATA_BACK_OK) {			// Test channel OK, channel is normal
			mHandler.sendEmptyMessage(TEST_CHANNEL_OK_MSG);
		}
	}

	@Override
	public void onCharacteristicWriteCallback(int status) {
		Log.d(TAG, "Write System callback status = " + status);
	}

	@Override
	public void OnServerCallback(int status) {
		Log.d(TAG, "Server callback OnServerCallback status =" + status);
		mHandler.sendEmptyMessage(SERVER_CALL_BACK_OK_MSG);
	}

	@Override
	public void OnServiceStatuslt(int status) {
		if (status == ICallbackStatus.BLE_SERVICE_START_OK) {
			if (mBluetoothLeService == null) {
				mBluetoothLeService = mBLEServiceOperate.getBleService();
				mBluetoothLeService.setICallback(this);
			}
		}
	}

	private static final int SHOW_SET_PASSWORD_MSG = 26;
	private static final int SHOW_INPUT_PASSWORD_MSG = 27;
	private static final int SHOW_INPUT_PASSWORD_AGAIN_MSG = 28;

	private boolean isPasswordDialogShowing = false;
	private String password = "";

	private void showPasswordDialog(final int type) {
		if (isPasswordDialogShowing) {
			return;
		}
		SensorCustomPasswordDialog.Builder builder = new SensorCustomPasswordDialog.Builder(SensorMainActivity.this, mTextWatcher);
		builder.setPositiveButton(getResources().getString(R.string.confirm),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (password.length() == 4) {
							dialog.dismiss();
							isPasswordDialogShowing = false;
							mWriteCommand.sendToSetOrInputPassward(password, type);
						}
					}
				});
		builder.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						isPasswordDialogShowing = false;
					}
				});
		builder.create().show();

		if (type == GlobalVariable.PASSWORD_TYPE_SET) {
			builder.setTittle(mContext.getResources().getString(R.string.set_password_for_band));
		} else if (type == GlobalVariable.PASSWORD_TYPE_INPUT_AGAIN) {
			builder.setTittle(mContext.getResources().getString(R.string.input_password_for_band_again));
		} else {
			builder.setTittle(mContext.getResources().getString(R.string.input_password_for_band));
		}
		isPasswordDialogShowing = true;
	}

	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			password = s.toString();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void afterTextChanged(Editable s) {}
	};

	private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

	/**
	 * Activate device management rights
	 * 
	 * @return
	 */
	private boolean isEnabled() {
		String pkgName = getPackageName();
		Log.w("ellison", "---->pkgName = " + pkgName);
		final String flat = Settings.Secure.getString(getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (int i = 0; i < names.length; i++) {
				final ComponentName cn = ComponentName.unflattenFromString(names[i]);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void upDateTodaySwimData() {
		// TODO Auto-generated method stub
		SwimInfo mSwimInfo = mySQLOperate.querySwimData(CalendarUtils.getCalendar(0));
	};

	/*
	 * Get one day
	 * heart rate, maximum, minimum, average heart rate
	 */
	private void UpdateBloodPressureMainUI(String calendar) {
		UTESQLOperate mySQLOperate = new UTESQLOperate(mContext);
		List<BPVOneDayInfo> mBPVOneDayListInfo = mySQLOperate.queryBloodPressureOneDayInfo(calendar);
		if (mBPVOneDayListInfo != null) {
			int highPressure = 0;
			int lowPressure = 0;
			int time = 0;
			for (int i = 0; i < mBPVOneDayListInfo.size(); i++) {
				highPressure = mBPVOneDayListInfo.get(i).getHightBloodPressure();
				lowPressure = mBPVOneDayListInfo.get(i).getLowBloodPressure();
				time = mBPVOneDayListInfo.get(i).getBloodPressureTime();
			}
			Log.d("MySQLOperate", "highPressure =" + highPressure + ",lowPressure =" + lowPressure);
		}
	}
}