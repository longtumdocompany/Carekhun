package co.app.longtumdo.carekhun;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import co.app.longtumdo.FitGridAdapter;

class GridViewAdapter extends FitGridAdapter {

    private int[] drawables = {
            R.drawable.qrcodeicon, R.drawable.elderly_icon, R.drawable.add_database_resize, R.drawable.update_icon_resize,
            R.drawable.notification_flat, R.drawable.googleicon, R.drawable.warning_icon_resize, R.drawable.warning_icon_resize,
            R.drawable.warning_icon_resize, R.drawable.warning_icon_resize, R.drawable.warning_icon_resize, R.drawable.warning_icon_resize};

    private Context context;

    GridViewAdapter(Context context) {
        super(context, R.layout.grid_item);
        this.context = context;
    }

    @Override
    public void onBindView(final int position, View itemView) {
        ImageView iv = (ImageView) itemView.findViewById(R.id.grid_item_iv);
        iv.setImageResource(drawables[position]);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "Position: " + position, Toast.LENGTH_SHORT).show();
                switch (position) {
                    case 0:     //QR Code Application
                        Intent mainActivityQRCodeScanner = new Intent(context.getApplicationContext(), MainActivityQRCodeScanner.class);
                        mainActivityQRCodeScanner.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.getApplicationContext().startActivity(mainActivityQRCodeScanner);
                        break;
                    case 1:     //Elder Care Application
                        Intent sensorDeviceScanActivity = new Intent(context.getApplicationContext(), SensorDeviceScanActivity.class);
                        sensorDeviceScanActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.getApplicationContext().startActivity(sensorDeviceScanActivity);
                        break;
                    case 2:     //Add Elderly Care Firebase Data Application
                        Intent mainActivityAddFirebaseData = new Intent(context.getApplicationContext(), MainActivityAddFirebaseData.class);
                        mainActivityAddFirebaseData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.getApplicationContext().startActivity(mainActivityAddFirebaseData);
                        break;
                    case 3:     //Searching and Update Elderly Care Firebase Data Application
                        Intent mainActivityUpdateAndDeleteFirebaseData = new Intent(context.getApplicationContext(), MainActivityUpdateAndDeleteFirebaseData.class);
                        mainActivityUpdateAndDeleteFirebaseData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.getApplicationContext().startActivity(mainActivityUpdateAndDeleteFirebaseData);
                        break;
                    case 4:     //Notification Elderly Care Firebase Data Application
                        Intent mainActivityUpdateNotificationFallDetectionFirebaseData = new Intent(context.getApplicationContext(), MainActivityUpdateNotificationFallDetectionFirebaseData.class);
                        mainActivityUpdateNotificationFallDetectionFirebaseData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.getApplicationContext().startActivity(mainActivityUpdateNotificationFallDetectionFirebaseData);
                        break;
                    case 5:     //Send Email
                        Intent mainActivitySendEmailBackground = new Intent(context.getApplicationContext(), MainActivitySendEmailBackground.class);
                        mainActivitySendEmailBackground.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.getApplicationContext().startActivity(mainActivitySendEmailBackground);
                        break;
                }
            }
        });
    }
}