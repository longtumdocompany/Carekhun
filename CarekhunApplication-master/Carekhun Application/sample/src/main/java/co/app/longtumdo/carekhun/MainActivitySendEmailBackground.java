package co.app.longtumdo.carekhun;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by suttipong on 9/22/2017.
 */

public class MainActivitySendEmailBackground extends AppCompatActivity implements TextToSpeech.OnInitListener {
    EditText et_to, et_message, et_subject;
    Button btn_send;
    Context c;
    String GMail = "suttipong.kull@gmail.com";                  //replace with you GMail
    String GMailPass = "top_ef01";                              // replace with you GMail Password
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);          // for hiding title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email_background);

        c = this;

        et_to = (EditText) findViewById(R.id.et_to);
        et_message = (EditText) findViewById(R.id.et_message);
        et_subject = (EditText) findViewById(R.id.et_subject);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String str_to = et_to.getText().toString();
                String str_message = et_message.getText().toString();
                String str_subject = et_subject.getText().toString();
                //String str_subject = "test";

                // Check if there are empty fields
                if (!str_to.equals("") && !str_message.equals("") && !str_subject.equals("")){

                    //Check if 'To:' field is a valid email
                    if (isValidEmail(str_to)){
                        et_to.setError(null);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(c, "Sending... Please wait", Toast.LENGTH_LONG).show();
                            }
                        });
                        sendEmail(str_to, str_subject, str_message);
                    }else{
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                et_to.setError("Not a valid email");
                            }
                        });
                    }
                }else{
                    Toast.makeText(c, "There are empty fields.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //**********************************************************************************************
        tts = new TextToSpeech(this, this, "com.google.android.tts");
    }

    public void sendEmail(final String to, final String subject, final String message) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender(GMail,
                            GMailPass);
                    sender.sendMail(subject,
                            message,
                            GMail,
                            to);
                    Log.w("sendEmail","Email successfully sent!");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(c, "Email successfully sent!", Toast.LENGTH_LONG).show();
                            et_to.setText("");
                            et_message.setText("");
                            et_subject.setText("");
                        }
                    });
                } catch (final Exception e) {
                    Log.e("sendEmail", e.getMessage(), e);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(c, "Email not sent. \n\n Error: " + e.getMessage().toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }


    // Check if parameter 'emailAddress' is a valid email
    public final static boolean isValidEmail(CharSequence emailAddress) {
        if (emailAddress == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(new Locale("th"));
            tts.speak("ระบบส่งอีเมล์ให้กับคุณหมอ", TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
