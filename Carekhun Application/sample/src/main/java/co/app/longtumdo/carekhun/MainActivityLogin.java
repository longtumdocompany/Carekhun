package co.app.longtumdo.carekhun;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by TOPPEE on 9/11/2017.
 */

public class MainActivityLogin extends AppCompatActivity implements AdapterView.OnItemClickListener, TextToSpeech.OnInitListener {

	//Initial Text to Speech
	private TextToSpeech tts;

	protected static final int RESULT_SPEECH = 1;

	private static final Class[] CLASSES = new Class[]{
			LoginEmailAndPassword.class,
			LoginGoogle.class,
			LoginFacebook.class,
			LoginOtherAccount.class,
			LoginWithPhone.class,
			ManageUserActivity.class,
			MainActivityRequestRepair.class			//Customer Service
	};

	public static final String[] titles = new String[] { "Email and Password",
			"Google", "Facebook", "Other Account", "Phone", "User Management", "Customer Service" };

	public static final String[] descriptions = new String[] {
			"Login with Email and Password",
			"Login with Google",
			"Login with Facebook",
			"Login with Other Account",
			"Login with Phone",
			"Manage User Account",
			"Service and Repair System"				//Customer Service
	};

	public static final Integer[] images = {
			R.drawable.usernameandpasswordicon,
			R.drawable.googleicon,
			R.drawable.facebookicon,
			R.drawable.otheraccounticon,
			R.drawable.mobileicon,
			R.drawable.manageicon,
			R.drawable.customerserviceicon			//Customer Service
	};

	List<RowItem> rowItems;
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_login);

		//Initial Text to Speech
		tts = new TextToSpeech(this, this, "com.google.android.tts");

		rowItems = new ArrayList<RowItem>();
		for (int i = 0; i < titles.length; i++) {
			RowItem item = new RowItem(images[i], titles[i], descriptions[i]);
			rowItems.add(item);
		}

		listView = (ListView) findViewById(R.id.list);
		CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.login_list_item, rowItems);
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

		FloatingActionButton fabLoginControl = (FloatingActionButton) findViewById(R.id.fabLoginControl);
		fabLoginControl.setOnClickListener(new View.OnClickListener() {
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
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Class clicked = CLASSES[position];					//index class name
		startActivity(new Intent(this, clicked));			//Set Start Activity
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tts.setLanguage(new Locale("th"));
			tts.speak("ให้ผู้ใช้งานล็อกอินเข้าสู่ระบบก่อนค่ะ", TextToSpeech.QUEUE_FLUSH, null);
		}
	}

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

					if(spokenText.equals("อีเมลล์") || spokenText.equals("email")){
						Log.i("spokenText","LoginEmailAndPassword.class");
						Intent loginEmailAndPassword = new Intent(getApplicationContext(),LoginEmailAndPassword.class);
						startActivity(loginEmailAndPassword);
					} else if(spokenText.equals("กูเกิ้ล") || spokenText.equals("google")){
						Log.i("spokenText","LoginGoogle.class");
						Intent loginGoogle = new Intent(getApplicationContext(),LoginGoogle.class);
						startActivity(loginGoogle);
					} else if(spokenText.equals("เฟสบุ๊ค") || spokenText.equals("facebook")){
						Log.i("spokenText","LoginFacebook.class");
						Intent loginFacebook = new Intent(getApplicationContext(),LoginFacebook.class);
						startActivity(loginFacebook);
					} else if(spokenText.equals("แอ็คเค้าอื่น") || spokenText.equals("other account")){
						Log.i("spokenText","LoginOtherAccount.class");
						Intent loginOtherAccount = new Intent(getApplicationContext(),LoginOtherAccount.class);
						startActivity(loginOtherAccount);
					} else if(spokenText.equals("โฟน") || spokenText.equals("phone")){
						Log.i("spokenText","LoginWithPhone.class");
						Intent loginWithPhone = new Intent(getApplicationContext(),LoginWithPhone.class);
						startActivity(loginWithPhone);
					} else if(spokenText.equals("จัดการผู้ใช้") || spokenText.equals("user management")){
						Log.i("spokenText","ManageUserActivity.class");
						Intent manageUserActivity = new Intent(getApplicationContext(),ManageUserActivity.class);
						startActivity(manageUserActivity);
					} else if(spokenText.equals("เรียกช่าง") || spokenText.equals("repair")){
						Log.i("spokenText","MainActivityRequestRepair.class");
						Intent mainActivityRequestRepair = new Intent(getApplicationContext(),MainActivityRequestRepair.class);
						startActivity(mainActivityRequestRepair);
					}
				}
				break;
			}
		}
	}
}