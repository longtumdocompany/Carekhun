package co.app.longtumdo.carekhun;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;

/**
 * Created by Suttipong.k on 3/31/2017.
 */

public class FirebaseMessaging extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    public static String applicationTag = "Aboc";

    //remoteMessage = รับ ChatMessage เข้ามา
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification ChatMessage Body: " + remoteMessage.getNotification().getBody());
        String body = remoteMessage.getNotification().getBody();
        String queueNo = remoteMessage.getData().get("queueNo");
        String nameAndSurname = remoteMessage.getData().get("nameAndSurname");
        String tel = remoteMessage.getData().get("tel");
        String email = remoteMessage.getData().get("email");
        String office = remoteMessage.getData().get("office");
        String date = remoteMessage.getData().get("date");
        String imageBarcode = remoteMessage.getData().get("imageBarcode");
        sendNotification(body, queueNo, nameAndSurname, tel, email, office, date, imageBarcode);
    }

    //param messageBody FCM message body received.
    public void sendNotification(String body, String queueNo, String nameAndSurname, String tel, String email, String office, String date, String imageBarcode) {
        Intent intent = new Intent(this, MainActivityCareKhun.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        //Open Barcode or Notification Detail
        String registerId = imageBarcode;         //Aboc_01
        File picturePath = null;
        picturePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String fileIDPath = picturePath + "//" + applicationTag + "//" + registerId;
        File file = new File(fileIDPath);
        file.mkdirs();
        fileIDPath = fileIDPath + "//barcode.jpg";
        Bitmap bitmap = null;

        try {
            File tempFile = new File(fileIDPath);
            if (tempFile.exists()) {
                bitmap = ImageUtil.openJpegFile(fileIDPath);
            } else {
                bitmap = null;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        //-----------------------------------END FIREBASE NOTIFICATION INBOX STYLE-----------------------------------------------------
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String[] events =
                {"QueueNo : " + queueNo ,
                "Name and Surname : " + nameAndSurname,
                "Tel : " + tel,
                "E-mail : " + email,
                "Office : " + office,
                "Date : " + date
                //"********** Confirm Data  ***********", ""
                };
        inboxStyle.setBigContentTitle("ABOC Customer Service");
        for (int i=0; i < events.length; i++) {
            inboxStyle.addLine(events[i]);
        }
        notificationBuilder.setStyle(inboxStyle);
        //-----------------------------------END FIREBASE NOTIFICATION INBOX STYLE--------------------------------------------------------

        Intent resultIntent = new Intent(this, MainActivityCareKhun.class);

        TaskStackBuilder TSB = TaskStackBuilder.create(this);
        TSB.addParentStack(MainActivityCareKhun.class);

        // Adds the Intent that starts the Activity to the top of the stack
        TSB.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = TSB.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationBuilder.setAutoCancel(true);
        //------------------------------------------------------------------------------------------

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }
}
