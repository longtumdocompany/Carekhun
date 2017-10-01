package co.app.longtumdo.carekhun;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by TOPPEE on 9/11/2017.
 */

public class LoginEmailAndPassword extends BaseActivity implements View.OnClickListener {
	private static final String TAG = "LoginEmailAndPassword";
	private EditText mEdtEmail, mEdtPassword;
	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
	private ImageView mImageView;
	private TextView mTextViewProfile;
	private TextInputLayout mLayoutEmail, mLayoutPassword;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emailpassword);

		mTextViewProfile = (TextView) findViewById(R.id.profile);
		mEdtEmail = (EditText) findViewById(R.id.edt_email);
		mEdtPassword = (EditText) findViewById(R.id.edt_password);
		mImageView = (ImageView) findViewById(R.id.logo);
		mLayoutEmail = (TextInputLayout) findViewById(R.id.layout_email);
		mLayoutPassword = (TextInputLayout) findViewById(R.id.layout_password);

		findViewById(R.id.email_sign_in_button).setOnClickListener(this);
		findViewById(R.id.email_create_account_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);
		findViewById(R.id.verify_button).setOnClickListener(this);

		mAuth = FirebaseAuth.getInstance();
		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null) {
					Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
				} else {
					Log.d(TAG, "onAuthStateChanged:signed_out");
				}
				updateUI(user);
			}
		};
	}

	@Override
	public void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(mAuthListener);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mAuthListener != null) {
			mAuth.removeAuthStateListener(mAuthListener);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.email_create_account_button:
				createAccount(mEdtEmail.getText().toString(), mEdtPassword.getText().toString());
				break;
			case R.id.email_sign_in_button:
				signIn(mEdtEmail.getText().toString(), mEdtPassword.getText().toString());
				break;
			case R.id.sign_out_button:
				signOut();
				break;
			case R.id.verify_button:
				findViewById(R.id.verify_button).setEnabled(false);
				final FirebaseUser firebaseUser = mAuth.getCurrentUser();
				firebaseUser.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Toast.makeText(
									LoginEmailAndPassword.this, "Verification email sent to " + firebaseUser.getEmail(), Toast.LENGTH_LONG
							).show();
						} else {
							Toast.makeText(LoginEmailAndPassword.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
						}
						findViewById(R.id.verify_button).setEnabled(true);
					}
				});
				break;
		}
	}

	private void createAccount(String email, String password) {
		if (!validateForm()) {
			return;
		}
		showProgressDialog();
		mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (!task.isSuccessful()) {
					mTextViewProfile.setTextColor(Color.RED);
					mTextViewProfile.setText(task.getException().getMessage());
				} else {
					mTextViewProfile.setTextColor(Color.DKGRAY);
				}
				hideProgressDialog();
			}
		});
	}

	private void signIn(String email, String password) {
		if (!validateForm()) {
			return;
		}
		showProgressDialog();
		mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
				if (!task.isSuccessful()) {
					mTextViewProfile.setTextColor(Color.RED);
					mTextViewProfile.setText(task.getException().getMessage());
				} else {
					mTextViewProfile.setTextColor(Color.DKGRAY);
				}
				hideProgressDialog();
			}
		});
	}

	private void signOut() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(R.string.logout);
		alert.setCancelable(false);
		alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				mAuth.signOut();
				updateUI(null);
			}
		});
		alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
		alert.show();
	}

	private boolean validateForm() {
		if (TextUtils.isEmpty(mEdtEmail.getText().toString())) {
			mLayoutEmail.setError("Required.");
			return false;
		} else if (TextUtils.isEmpty(mEdtPassword.getText().toString())) {
			mLayoutPassword.setError("Required.");
			return false;
		} else {
			mLayoutEmail.setError(null);
			mLayoutPassword.setError(null);
			return true;
		}
	}

	private void updateUI(FirebaseUser user) {
		if (user != null) {
			if (user.getPhotoUrl() != null) {
				new DownloadImageTask().execute(user.getPhotoUrl().toString());
			}
			mTextViewProfile.setText("DisplayName: " + user.getDisplayName());
			mTextViewProfile.append("\n\n");
			mTextViewProfile.append("Email: " + user.getEmail());
			mTextViewProfile.append("\n\n");
			mTextViewProfile.append("Firebase ID: " + user.getUid());
			mTextViewProfile.append("\n\n");
			mTextViewProfile.append("Email Verification: " + user.isEmailVerified());

			//*******************Gen QR Code*****************************
			String myAndroidDeviceId = "";
			TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			if (mTelephony.getDeviceId() != null){
				myAndroidDeviceId = mTelephony.getDeviceId();
			}else{
				myAndroidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
			}

			String name = user.getDisplayName();
			MainActivityRequestRepair mainActivityRequestRepair = new MainActivityRequestRepair();
			mainActivityRequestRepair.QRCodeGenerator(name,myAndroidDeviceId);
			//*******************************Save Data***************************************************
			String MY_DISPLAY_NAME = null;
			String MY_GET_EMAIL = null;

			SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
			SharedPreferences.Editor prefsEditor = myPrefs.edit();
			prefsEditor.putString(MY_DISPLAY_NAME, user.getEmail());
			prefsEditor.putString(MY_GET_EMAIL, user.getEmail());
			prefsEditor.commit();
			//********************************************************************************************

			if (user.isEmailVerified()) {
				findViewById(R.id.verify_button).setVisibility(View.GONE);
			} else {
				findViewById(R.id.verify_button).setVisibility(View.VISIBLE);
			}

			findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
			findViewById(R.id.email_password_fields).setVisibility(View.GONE);
			findViewById(R.id.signout_zone).setVisibility(View.VISIBLE);
		} else {
			mTextViewProfile.setText(null);

			findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
			findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
			findViewById(R.id.signout_zone).setVisibility(View.GONE);
		}
		hideProgressDialog();
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap mIcon = null;
			try {
				InputStream in = new URL(urls[0]).openStream();
				mIcon = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return mIcon;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				mImageView.getLayoutParams().width = (getResources().getDisplayMetrics().widthPixels / 100) * 24;
				mImageView.setImageBitmap(result);
			}
		}
	}
}