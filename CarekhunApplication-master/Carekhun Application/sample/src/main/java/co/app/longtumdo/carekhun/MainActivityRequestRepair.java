package co.app.longtumdo.carekhun;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MainActivityRequestRepair extends AppCompatActivity implements AdapterView.OnItemClickListener {

	protected static final int RESULT_SPEECH = 1;

	static Firebase myFirebaseRef;
	DatabaseReference database = FirebaseDatabase.getInstance().getReference();
	String detail = "";
	static final String TAG = "Main Acvity";

	ArrayList<String> keysArray;

	String MY_DISPLAY_NAME = null;
	String MY_GET_EMAIL = null;

	public static final String[] titles = new String[] { "Fall Detection Sensor", "Notification Alert" , "Chat Room Application", "Wearable"};

	public static final String[] descriptions = new String[] {
			"The system can not detect falls for seniors.",
			"The system can not alert for falling for seniors.",
			"The system can not talk between the elderly and the doctor.",
			"Equipment can not report the rate of heart rate, pressure and physical condition of the elderly."
		};

	public static final Integer[] images = {
			R.drawable.fall_detecticon,
			R.drawable.notification_flat,
			R.drawable.chat_icon_resize,
			R.drawable.wearable_icon,
	};

	List<RowItem> rowItems;
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_request_repair);

		rowItems = new ArrayList<RowItem>();
		for (int i = 0; i < titles.length; i++) {
			RowItem item = new RowItem(images[i], titles[i], descriptions[i]);
			rowItems.add(item);
		}

		listView = (ListView) findViewById(R.id.list);
		CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.activity_layout_request_customer_service, rowItems);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		//Main Speech Recognition-------------------------------------------------------------------
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "command");
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "th-TH");

		try {
			startActivityForResult(intent, RESULT_SPEECH);
		} catch (ActivityNotFoundException a) {
			Toast t = Toast.makeText(getApplicationContext(), "Google Speech Recognition is not Working", Toast.LENGTH_SHORT);
			t.show();
		}

		FloatingActionButton fabRepairControl = (FloatingActionButton) findViewById(R.id.fabRepairControl);
		fabRepairControl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Start Main Speech Recognition-------------------------------------------------------------------
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "command");
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "th-TH");

				try {
					startActivityForResult(intent, RESULT_SPEECH);
				} catch (ActivityNotFoundException a) {
					Toast t = Toast.makeText(getApplicationContext(), "Google Speech Recognition is not Working", Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});

		Firebase.setAndroidContext(this);
		myFirebaseRef = new Firebase("https://aboc-afe9a.firebaseio.com");
		myFirebaseRef.addChildEventListener(childEventListener);
	}

	//*************************Firebase Query Database**********************************
	ChildEventListener childEventListener = new ChildEventListener() {

		@Override
		public void onChildAdded(DataSnapshot dataSnapshot, String s) {
			Log.d(TAG, "Key: " +dataSnapshot.getKey().toString() + "\t Value: " +  dataSnapshot.getValue().toString());
			//keysArray.add(dataSnapshot.getKey().toString());
		}

		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String s) {
			Log.d(TAG, dataSnapshot.getKey() +":" + dataSnapshot.getValue().toString());
		}

		@Override
		public void onChildRemoved(DataSnapshot dataSnapshot) {
			Log.d(TAG, dataSnapshot.getKey() +":" + dataSnapshot.getValue().toString());
		}

		@Override
		public void onChildMoved(DataSnapshot dataSnapshot, String s) {
			Log.d(TAG, dataSnapshot.getKey() +":" + dataSnapshot.getValue().toString());
		}

		@Override
		public void onCancelled(FirebaseError firebaseError) {}
	};

	//Main Speech Recognition
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case RESULT_SPEECH: {
				if (resultCode == RESULT_OK && null != data) {
					ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

					//แสดงผลของ Text ที่พูด
					String spokenText = text.get(0);
					Log.i("spokenText",spokenText);

					if(spokenText.equals("หนึ่ง") || spokenText.equals("one")){
						detail = "The system can not detect falls for seniors.";
					} else if(spokenText.equals("สอง") || spokenText.equals("two")){
						detail = "The system can not alert for falling for seniors.";
					} else if(spokenText.equals("สาม") || spokenText.equals("three")){
						detail = "The system can not talk between the elderly and the doctor.";
					} else if(spokenText.equals("สี่") || spokenText.equals("four")){
						detail = "Equipment can not report the rate of heart rate, pressure and physical condition of the elderly.";
					}

					//Test
					SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
					String getEmail = myPrefs.getString(MY_GET_EMAIL, "");

					//ส่ง E-mail ไปหาเจ้าหน้าที่
					MainActivitySendEmailBackground mainActivitySendEmailBackground = new MainActivitySendEmailBackground();
					String str_to = getEmail;
					String str_subject = "แจ้งซ่อม : Customer Service";
					String str_message = "Name : Suttipong Kullawattana \n"+"Customer ID : 0001 \n"+detail+"\n";
					mainActivitySendEmailBackground.sendEmail(str_to, str_subject, str_message);

					//SET DATA TO MODEL CLASS
//					FirebaseQueryRepair s = new FirebaseQueryRepair();
//					s.setIdCustomer(database.child("0001").push().getKey());       //Key Database
//					s.setToEmail(str_to);
//					s.setSubjectEmail(str_subject);
//					s.setMessageEmail(str_message);
//					database.child(s.getIdCustomer()).child(str_to).setValue(s);

					DatabaseReference usersRef = database.child("users");
					Map<String, FirebaseQueryRepair> users = new HashMap<String, FirebaseQueryRepair>();
					users.put("customer_0001", new FirebaseQueryRepair("0001",str_to,str_subject,str_message));
					usersRef.setValue(users);
					finish();

				}
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		if(position == 0){
			detail = "The system can not detect falls for seniors.";
			//Voice synthesis between reading device and Tablet does not work.
		} else if(position == 1){
			detail = "The system can not alert for falling for seniors.";
			//The system can not detect the face on the tablet.
		} else if(position == 2){
			detail = "The system can not talk between the elderly and the doctor.";
			//Audio books can not be read aloud.
		} else if(position == 3){
			detail = "Equipment can not report the rate of heart rate, pressure and physical condition of the elderly.";
			//The system can not turn on the camera.
		}

		//Test
		SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
		String getEmail = myPrefs.getString(MY_GET_EMAIL, "");

		//ส่ง E-mail ไปหาเจ้าหน้าที่
		MainActivitySendEmailBackground mainActivitySendEmailBackground = new MainActivitySendEmailBackground();
		//String str_to = "suttipong.kull@gmail.com";
		String str_to = getEmail;
		String str_subject = "แจ้งซ่อม : Customer Service";
		String str_message = "Name : Suttipong Kullawattana \n"+"Customer ID : 0001 \n"+detail+"\n";
		mainActivitySendEmailBackground.sendEmail(str_to, str_subject, str_message);

		DatabaseReference usersRef = database.child("users");
		Map<String, FirebaseQueryRepair> users = new HashMap<String, FirebaseQueryRepair>();
		users.put("customer_0001", new FirebaseQueryRepair("0001",str_to,str_subject,str_message));
		usersRef.setValue(users);
		finish();
	}

	//#QR CODE
	public void QRCodeGenerator(String idNo, String machineId){
		try {

			File picturePath = null;
			picturePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			String filePrint = picturePath + "//" + "ConfigurationFile" + "//" +"qrCodeGenerate.jpg";

			// Create the ByteMatrix for the QR-Code
			Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

			QRCodeWriter qrCodeWriter = new QRCodeWriter();					//Lib zxing-2.1jar class QRCodeWriter

			BitMatrix byteMatrix = qrCodeWriter.encode(machineId+","+","+idNo, BarcodeFormat.QR_CODE, 500, 500, hintMap);

			try {
				QRCodeUtil.writeToFile(byteMatrix, "PNG", filePrint);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (WriterException e) {
			e.printStackTrace();
		}
	}
}