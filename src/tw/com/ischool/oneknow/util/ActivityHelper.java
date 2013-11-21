package tw.com.ischool.oneknow.util;

import tw.com.ischool.oneknow.main.MainActivity;
import android.app.Activity;
import android.content.Intent;

public class ActivityHelper {
	private Activity mActivity;
	private ScreenHelper mScreen;

	public ActivityHelper(Activity activity) {
		mActivity = activity;
	}

	public void valid() {
		if (MainActivity.getInitFlag() == -1) {
			Intent intent = new Intent(mActivity, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mActivity.startActivity(intent);
		}
	}

	public ScreenHelper getScreen() {
		if (mScreen == null)
			mScreen = new ScreenHelper(mActivity);
		return mScreen;
	}
}
