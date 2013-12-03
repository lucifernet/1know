package tw.com.ischool.oneknow.login;

import org.json.JSONObject;

import tw.com.ischool.oneknow.item.DisplayStatus;

public interface ILoginHelper {
	void login();

	void setListener(OnLoginResultListener listener);
	
	public interface OnLoginResultListener {
		void onSucceed(JSONObject json, DisplayStatus status);

		void onFail(String message, Exception e);
	}
}
