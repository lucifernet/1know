package tw.com.ischool.oneknow.login;

import tw.com.ischool.oneknow.login.ILoginHelper.OnLoginResultListener;
import tw.com.ischool.oneknow.util.StringUtil;
import android.content.Context;
import android.content.SharedPreferences;

public abstract class LoginHelper {
	public static final String TAG = "UserLoginHelper";
	public static final String ATTR_USERNAME = "username";
	public static final String ATTR_PASSWORD = "password";
	public static final String ATTR_GUEST_ID = "guestid";
	
	public static void autoLogin(Context context, OnLoginResultListener listener) {
		SharedPreferences settings = context.getSharedPreferences(TAG, 0);
		String username = settings.getString(ATTR_USERNAME, StringUtil.EMPTY);
		String password = settings.getString(ATTR_PASSWORD, StringUtil.EMPTY);
		String guestuid = settings.getString(ATTR_GUEST_ID, StringUtil.EMPTY);

		ILoginHelper helper = null;
		if (!StringUtil.isNullOrWhitespace(username)) {
			helper = new AccountLoginHelper(context, username, password);
		} else if (!StringUtil.isNullOrWhitespace(guestuid)) {
			helper = new SwitchLoginHelper(context, guestuid);
		} else {
			helper = new FirstLoginHelper(context);
		}
 
		helper.setListener(listener);
		helper.login();
	}
}
