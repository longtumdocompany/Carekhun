package co.app.longtumdo.carekhun;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivityUpdateAndDeleteFirebaseData extends ActionBarActivity implements View.OnClickListener, TextToSpeech.OnInitListener{

      Button save;
      static Firebase myFirebaseRef;
      EditText nameEditText;
      EditText messageEditText;
      ProgressBar progressBar;
      static final String TAG = "Main Acvity";
      ArrayAdapter<String> valuesAdapter;
      ArrayList<String> displayArray;
      ArrayList<String> keysArray;
      ListView listView;

      private TextToSpeech tts;
      protected static final int RESULT_SPEECH = 1;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main_firebase_update_and_delete_data);

          save = (Button)findViewById(R.id.save);
          nameEditText = (EditText)findViewById(R.id.name);
          messageEditText= (EditText)findViewById(R.id.txtMessage);
          progressBar = (ProgressBar)findViewById(R.id.progressBar);
          listView = (ListView)findViewById(R.id.listView);

          displayArray  = new ArrayList<>();
          keysArray = new ArrayList<>();
          valuesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,displayArray);
          listView.setAdapter(valuesAdapter);
          listView.setOnItemClickListener(itemClickListener);

          //********************************Firebase Database*********************************************
          Firebase.setAndroidContext(this);
          myFirebaseRef = new Firebase("https://carekhun-37740.firebaseio.com");
          myFirebaseRef.addChildEventListener(childEventListener);

          save.setOnClickListener(this);

          //Initial Text to Speech
          tts = new TextToSpeech(this, this, "com.google.android.tts");
      }

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
          switch (v.getId()){
            case R.id.save:
              String nameString = nameEditText.getText().toString();
              String messageString = messageEditText.getText().toString();
              save(nameString,messageString);
              break;
          }
      }

      private void save(String name, String message){
          myFirebaseRef.child(name).setValue(message, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
              nameEditText.setText("");
              messageEditText.setText("");
              hideProgressBar();
            }
          });
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
            String chkKey = dataSnapshot.getValue().toString().substring(0,4);
            Log.d(TAG, chkKey);
            if(chkKey.equals("Fall")){
                Log.d(TAG, dataSnapshot.getValue().toString().substring(6, dataSnapshot.getValue().toString().length() - 1));
            } else if(dataSnapshot.getKey().toString().equals("hearthRate")){
                Log.d(TAG, dataSnapshot.getValue().toString());
            } else {
                if(!dataSnapshot.getValue().toString().equals(null)) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        FirebaseQueryProfile post = postSnapshot.getValue(FirebaseQueryProfile.class);
                        keyAndValue = "Name : " + post.getMyname() + " " + post.getSurname() + " | Address : " + post.getAddress() + " | Tel : " + post.getTel() + " | E-mail : " + post.getEmailAddress() + " | Take Care Type : " + post.getTakeCareType() + " | Time of Fall : " + post.getTimeFallDetection();
                    }
                    displayArray.add(keyAndValue);
                    keysArray.add(dataSnapshot.getKey().toString());
                } else {
                    Log.d(TAG, "Nothings");
                    keyAndValue = "Nothings";
                    displayArray.add(keyAndValue);
                    keysArray.add(dataSnapshot.getKey().toString());
                }
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

            //Update List View
            updateListView();
          }

          @Override
          public void onChildRemoved(DataSnapshot dataSnapshot) {
                String deletedKey = dataSnapshot.getKey();
                int removedIndex = keysArray.indexOf(deletedKey);
                keysArray.remove(removedIndex);
                displayArray.remove(removedIndex);

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
              int id = item.getItemId();
              if (id == R.id.action_settings) {
                return true;
              }
              return super.onOptionsItemSelected(item);
          }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(new Locale("th"));
            tts.speak("ระบบแก้ไขข้อมูลผู้สูงอายุสำหรับผู้ดูแล", TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
