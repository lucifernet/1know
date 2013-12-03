package tw.com.ischool.oneknow.login;

import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.item.DisplayStatus;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class SwitchLoginHelper extends AsyncTask<String, Void, JSONObject>
		implements ILoginHelper {

	private String mEmail;
	private OnLoginResultListener listener;
	private Exception exception;
	private Context context;
	private DisplayStatus status;

	public SwitchLoginHelper(Context context, String email) {
		this.context = context;

		mEmail = email;
		status = DisplayStatus.GUEST;
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
	protected JSONObject doInBackground(String... params) {
		JSONObject request = new JSONObject();
		try {
			request.put("email", mEmail);
			OneKnow.postAndSyncCookie(OneKnow.SERVICE_ACCOUNT_SWITCH, request,
					Void.class);

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
			editor.putString(LoginHelper.ATTR_GUEST_ID, mEmail);
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
