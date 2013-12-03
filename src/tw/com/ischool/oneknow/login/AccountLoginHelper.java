package tw.com.ischool.oneknow.login;

import java.util.HashMap;

import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.item.DisplayStatus;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class AccountLoginHelper extends AsyncTask<String, Void, JSONObject>
		implements ILoginHelper {

	private String mUserName;
	private String mPassword;
	private OnLoginResultListener listener;
	private Exception exception;
	private Context context;
	private DisplayStatus status;

	// public AccountLoginHelper(Context context) {
	// this.context = context;
	//
	// status = DisplayStatus.LOGINED;
	// SharedPreferences settings =
	// context.getSharedPreferences(LoginHelper.TAG, 0);
	// mUserName = settings.getString(LoginHelper.ATTR_USERNAME,
	// StringUtil.EMPTY);
	// mPassword = settings.getString(LoginHelper.ATTR_PASSWORD,
	// StringUtil.EMPTY);
	// }

	public AccountLoginHelper(Context context, String username, String password) {
		this.context = context;

		status = DisplayStatus.LOGINED;

		mUserName = username;
		mPassword = password;
	}

	@Override
	public void setListener(OnLoginResultListener listener) {
		this.listener = listener;
	}

	@Override
	public void login() {
		execute();
	}

	@Override
	public JSONObject doInBackground(String... params) {
		try {
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("uid", mUserName);
			p.put("pwd", mPassword);

			OneKnow.getAndSyncCookie(OneKnow.SERVICE_LOGIN, p, JSONObject.class);

			return OneKnow.getFrom(OneKnow.SERVICE_ACCOUNT_USER, null,
					JSONObject.class);
		} catch (Exception e) {
			this.exception = e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		if (this.exception == null) {
			SharedPreferences settings = context.getSharedPreferences(
					LoginHelper.TAG, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(LoginHelper.ATTR_USERNAME, mUserName);
			editor.putString(LoginHelper.ATTR_PASSWORD, mPassword);
			editor.commit();
		}
		
		if (this.exception == null && !result.has("error")) {
			if (this.listener != null)
				this.listener.onSucceed(result, status);
		} else if (exception != null) {
			this.listener.onFail(
					context.getString(R.string.invalid_password_characters),
					exception);
		} else {
			this.listener.onFail(
					context.getString(R.string.error_incorrect_password), null);
		}		
	}

}
