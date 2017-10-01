package co.app.longtumdo.carekhun;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class SensorCustomPasswordDialog extends Dialog {

	public SensorCustomPasswordDialog(Context context) {
		super(context);
	}

	public SensorCustomPasswordDialog(Context context, int theme) {
		super(context, theme);
	}

	public static class Builder {
		private Context context;
		private TextView tv_title;
		private String positiveButtonText;
		private String negativeButtonText;
		private DialogInterface.OnClickListener positiveButtonClickListener;
		private DialogInterface.OnClickListener negativeButtonClickListener;

		private SensorPasswordInputView sensorPasswordInputView;
		private TextWatcher mTextWatcher ;
		public Builder(Context context, TextWatcher watcher) {
			this.context = context;
			this.mTextWatcher = watcher;
		}

		public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		SensorCustomPasswordDialog mDialog;

		public SensorCustomPasswordDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mDialog = new SensorCustomPasswordDialog(context, R.style.passwordDialog);

			View window = inflater.inflate(R.layout.sensor_custom_password_dialog, null);
			mDialog.addContentView(window, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			tv_title= (TextView) window.findViewById(R.id.title) ;
			sensorPasswordInputView = (SensorPasswordInputView) window.findViewById(R.id.passwordInputView);
			sensorPasswordInputView.setPasswordLength(4);
			sensorPasswordInputView.addTextChangedListener(mTextWatcher);
			if (negativeButtonText != null) {
				((Button) window.findViewById(R.id.cancel))
						.setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					((Button) window.findViewById(R.id.cancel))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									negativeButtonClickListener.onClick(mDialog,
											DialogInterface.BUTTON_NEGATIVE);
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				window.findViewById(R.id.cancel).setVisibility(View.GONE);
			}
			
			if (positiveButtonText != null) {
				((Button) window.findViewById(R.id.confirm))
						.setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					((Button) window.findViewById(R.id.confirm))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									positiveButtonClickListener.onClick(mDialog,
											DialogInterface.BUTTON_POSITIVE);
								}
							});
				}
			} else {
				window.findViewById(R.id.confirm).setVisibility(View.GONE);
			}
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.setCancelable(false);
			return mDialog;
		}

		public boolean isShowing() {
			return mDialog.isShowing();
		}

		 public void setTittle(String tittle) {
		 if (tv_title != null) {
		 	tv_title.setText(tittle);
		 }
		 }
	}
}
