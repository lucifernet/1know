package tw.com.ischool.oneknow.login;

import org.json.JSONObject;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.item.DisplayStatus;
import tw.com.ischool.oneknow.login.ILoginHelper.OnLoginResultListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class LoginActivity extends Activity {

	public static final int RESULT_CODE_SUCCEED = 2;
	public static final int RESULT_CODE_FAILURE = 1;

	public static final String BUNDLE_LOGIN_INFO = "LoginInfo";

	private EditText mTxtUserName;
	private EditText mTxtPassword;
	private ProgressBar mProgress;
	private Button mLoginButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		this.getActionBar().hide();

		mTxtUserName = (EditText) this.findViewById(R.id.editUserName);
		mTxtPassword = (EditText) this.findViewById(R.id.editPassword);
		mProgress = (ProgressBar) this.findViewById(R.id.progressLogin);

		// final UserLoginHelper helper = new UserLoginHelper(this);
		//
		// mTxtUserName.setText(helper.getUserName());
		// mTxtPassword.setText(helper.getPassword());
		// final AccountLoginHelper helper = new
		// AccountLoginHelper(LoginActivity.this);

		mLoginButton = (Button) this.findViewById(R.id.btnLogin);
		mLoginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mTxtUserName.setError(null);
				mTxtPassword.setError(null);

				if (mTxtUserName.getText().toString().isEmpty()) {
					mTxtUserName
							.setError(getString(R.string.login_username_empty));
					return;
				}

				if (mTxtPassword.getText().toString().isEmpty()) {
					mTxtPassword
							.setError(getString(R.string.login_password_empty));
					return;
				}

				mLoginButton.setEnabled(false);
				showProgress(true);

				AccountLoginHelper helper = new AccountLoginHelper(
						LoginActivity.this, mTxtUserName.getText().toString(),
						mTxtPassword.getText().toString());
				helper.setListener(new OnLoginResultListener() {

					@Override
					public void onSucceed(JSONObject json, DisplayStatus status) {
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putString(BUNDLE_LOGIN_INFO, json.toString());
						intent.putExtras(bundle);

						setResult(RESULT_CODE_SUCCEED, intent);
						finish();
					}

					@Override
					public void onFail(String message, Exception e) {
						mLoginButton.setEnabled(true);
						mTxtPassword.setError(message);
						mTxtPassword.requestFocus();
					}
				});

				helper.execute(mTxtUserName.getText().toString(), mTxtPassword
						.getText().toString());
			}
		});

	}

	private void showProgress(boolean show) {
		mProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);

	}
}
