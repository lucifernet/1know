package tw.com.ischool.oneknow.login;

import java.util.HashMap;

import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.OneKnowApplication;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.util.StringUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class UserLoginHelper extends AsyncTask<String, Void, Exception> {
	private static final String TAG = "UserLoginHelper";
	private static final String ATTR_USERNAME = "username";
	private static final String ATTR_PASSWORD = "password";

	private OnLoginResultListener mListener;
	private JSONObject mLoginJSON;
	private String mUserName;
	private String mPassword;
	private Context mContext;

	public UserLoginHelper(Context context) {
		mContext = context;

		SharedPreferences settings = mContext.getSharedPreferences(TAG, 0);
		mUserName = settings.getString(ATTR_USERNAME, StringUtil.EMPTY);
		mPassword = settings.getString(ATTR_PASSWORD, StringUtil.EMPTY);
	}

	public void setOnLoginResultListener(OnLoginResultListener listener) {
		mListener = listener;
	}

	public String getUserName() {
		return mUserName;
	}

	public String getPassword() {
		return mPassword;
	}

	@Override
	protected Exception doInBackground(String... params) {
		// TODO: attempt authentication against a network service.
		if (params.length > 0) {
			mUserName = params[0];
			mPassword = params[1];
		}

		try {
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("uid", mUserName);
			p.put("pwd", mPassword);

			mLoginJSON = OneKnow.getAndSyncCookie(OneKnow.SERVICE_LOGIN, p,
					JSONObject.class);
		} catch (Exception e) {
			return e;
		}

		return null;
	}

	@Override
	protected void onPostExecute(Exception exception) {

		if (exception == null && !mLoginJSON.has("error")) {
			SharedPreferences settings = mContext.getSharedPreferences(TAG, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("username", mUserName);
			editor.putString("password", mPassword);

			// Commit the edits!
			editor.commit();

			OneKnowApplication.LOGIN_COMPLETED = true;

			if (mListener != null)
				mListener.onSucceed(mLoginJSON);
		} else if (exception != null) {
			mListener.onFail(
					mContext.getString(R.string.invalid_password_characters),
					exception);
		} else {
			mListener
					.onFail(mContext
							.getString(R.string.error_incorrect_password), null);

		}
	}

	public interface OnLoginResultListener {
		void onSucceed(JSONObject json);

		void onFail(String message, Exception e);
	}
}
