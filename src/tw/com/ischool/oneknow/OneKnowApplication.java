package tw.com.ischool.oneknow;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

public class OneKnowApplication extends Application {

	public static final String TAG = "OneKnowApplication";
	public static boolean LOGIN_COMPLETED = false;

	public static void toLoginActivity(Activity activity) {
		Intent i = activity
				.getBaseContext()
				.getPackageManager()
				.getLaunchIntentForPackage(
						activity.getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(i);
	}

}
