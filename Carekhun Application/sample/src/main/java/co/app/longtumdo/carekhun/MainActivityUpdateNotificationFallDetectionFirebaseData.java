package co.app.longtumdo.carekhun;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by TOPPEE on 9/11/2017.
 */

public class MainActivityUpdateNotificationFallDetectionFirebaseData extends ActionBarActivity implements View.OnClickListener, TextToSpeech.OnInitListener{

      static Firebase myFirebaseRef;
      ProgressBar progressBar;
      static final String TAG = "Main Acvity";
      ArrayAdapter<String> valuesAdapter;
      ArrayList<String> displayArray;
      ArrayList<String> keysArray;
      ListView listView;

      private TextToSpeech tts;
//    protected static final int RESULT_SPEECH = 1;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main_firebase_notification_fall_detection_data);

          //********************************Progress & ListView*********************************************
          progressBar = (ProgressBar)findViewById(R.id.progressBar);
          listView = (ListView)findViewById(R.id.listView);

          //********************************display*********************************************
          displayArray  = new ArrayList<>();
          keysArray = new ArrayList<>();
          valuesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,displayArray);
          listView.setAdapter(valuesAdapter);
          listView.setOnItemClickListener(itemClickListener);

          //********************************Refresh Thread Time*********************************************
          doTheAutoRefresh();

          //********************************Firebase Database*********************************************
          Firebase.setAndroidContext(this);
          myFirebaseRef = new Firebase("https://carekhun-37740.firebaseio.com");
          myFirebaseRef.addChildEventListener(childEventListener);

          tts = new TextToSpeech(this, this, "com.google.android.tts");
      }

        //********************************Handle ChatMessage Time Thread***************************************
        private final Handler handler = new Handler();

        private void doTheAutoRefresh() {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //********************************Firebase Database*********************************************
                    myFirebaseRef = new Firebase("https://carekhun-37740.firebaseio.com");
                    myFirebaseRef.addChildEventListener(childEventListener);
                    //********************************Refresh Thread Time*********************************************
                    doTheAutoRefresh();
                }
            }, 20000);
        }

      //********************************ProgressBar***************************************
      private void showProgressBar(){
          InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
          progressBar.setVisibility(View.VISIBLE);
      }

      private void hideProgressBar(){
        progressBar.setVisibility(View.INVISIBLE);
      }

      @Override
      public void onClick(View v) {
          showProgressBar();
      }

      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
          getMenuInflater().inflate(R.menu.menu_main, menu);
          return true;
      }

      //*************************Firebase Query Database**********************************
      ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, dataSnapshot.getKey() + ":" + dataSnapshot.getValue().toString());
            String keyAndValue = null;

            //Check ChatMessage
            String chkKey = dataSnapshot.getValue().toString().substring(0,4);
            Log.d(TAG, chkKey);
            if(chkKey.equals("Fall")){

                  //Data
                  keyAndValue = dataSnapshot.getValue().toString().substring(5, dataSnapshot.getValue().toString().length() - 1);

                  //Show and Adapter List
                  displayArray.add(keyAndValue);
                  keysArray.add(dataSnapshot.getKey().toString());

                  //Sound
                  tts.speak(keyAndValue, TextToSpeech.QUEUE_FLUSH, null);

                  //Send Notification Every 20 Second
                  FirebaseNotificationUtil firebaseMessaging = new FirebaseNotificationUtil();
                  firebaseMessaging.pushFCMNotificationFallDetection(keyAndValue);

            } else {
                Log.d(TAG, dataSnapshot.getValue().toString());
                keyAndValue = "Alert";
                //Show and Adapter List
                displayArray.add(keyAndValue);
                keysArray.add(dataSnapshot.getKey().toString());
            }

            //Update List View
            updateListView();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String changedKey = dataSnapshot.getKey();
            int changedIndex = keysArray.indexOf(changedKey);
            String keyAndValue = "Key: " +dataSnapshot.getKey().toString() + "\t Value: " +  dataSnapshot.getValue().toString();
            displayArray.set(changedIndex,keyAndValue);
            updateListView();
          }

          @Override
          public void onChildRemoved(DataSnapshot dataSnapshot) {
                String deletedKey = dataSnapshot.getKey();
                int removedIndex = keysArray.indexOf(deletedKey);
                keysArray.remove(removedIndex);
                displayArray.remove(removedIndex);

                //********************************Remove Handle ChatMessage Time Thread After Click List And Remove List***************************************
                handler.removeCallbacksAndMessages(null);

                //Update List View
                updateListView();
          }

          @Override
          public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, dataSnapshot.getKey() +":" + dataSnapshot.getValue().toString());
          }

          @Override
          public void onCancelled(FirebaseError firebaseError) {}
      };

      //************************************************UpdateListView********************************************************
      private void updateListView(){
          valuesAdapter.notifyDataSetChanged();
          listView.invalidate();
          Log.d(TAG, "Length: " + displayArray.size());
      }

      //************************************************Setting********************************************************
      AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          String clickedKey = keysArray.get(position);
          myFirebaseRef.child(clickedKey).removeValue();
          Log.d(TAG,clickedKey);
        }
      };

      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(new Locale("th"));
            tts.speak("ระบบแจ้งเตือนการหกล้มสำหรับผู้สูงอายุผ่านสมาร์ทมินเร่อ", TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
