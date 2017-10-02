/**************************************************************************************
 * [Project]
 *       MyProgressDialog
 * [Package]
 *       com.lxd.widgets
 * [FileName]
 *       SensorCustomProgressDialog.java
 * [Copyright]
 *       Copyright 2012 LXD All Rights Reserved.
 * [History]
 *       Version          Date              Author                        Record
 *--------------------------------------------------------------------------------------
 *       1.0.0           2012-4-27         lxd (rohsuton@gmail.com)        Create
 **************************************************************************************/

package co.app.longtumdo.carekhun;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

public class SensorCustomProgressDialog extends Dialog {
	private Context context = null;
	private static SensorCustomProgressDialog sensorCustomProgressDialog = null;
	private static TextView syn_schedule;

	public SensorCustomProgressDialog(Context context) {
		super(context);
		this.context = context;
	}

	public SensorCustomProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	public static SensorCustomProgressDialog createDialog(Context context) {
		sensorCustomProgressDialog = new SensorCustomProgressDialog(context, R.style.CustomProgressDialog);
		sensorCustomProgressDialog.setContentView(R.layout.sensor_customprogressdialog);
		sensorCustomProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		syn_schedule = (TextView) sensorCustomProgressDialog.findViewById(R.id.syn_schedule);
		return sensorCustomProgressDialog;
	}

	public void onWindowFocusChanged(boolean hasFocus) {

		if (sensorCustomProgressDialog == null) {
			return;
		}

		ImageView imageView = (ImageView) sensorCustomProgressDialog.findViewById(R.id.loadingImageView);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
		animationDrawable.start();
	}

	public SensorCustomProgressDialog setTitile(String strTitle) {
		return sensorCustomProgressDialog;
	}

	public SensorCustomProgressDialog setMessage(String strMessage) {
		return sensorCustomProgressDialog;
	}

	public void setSchedule(int schedule) {
		if (syn_schedule != null) {
			syn_schedule.setText(schedule + "%");
		}
	}

	public void serCalibrating() {
		syn_schedule.setText("");
	}
}
