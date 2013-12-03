package tw.com.ischool.oneknow.util;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.main.MainActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ActivityHelper {
	private Activity mActivity;
	private ScreenHelper mScreen;

	public ActivityHelper(Activity activity) {
		mActivity = activity;
	}

	public void valid() {
		final ConnectivityManager conMgr = (ConnectivityManager) mActivity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			// notify user you are online
		} else {
			// notify user you are not online
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle(R.string.alert_title);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setMessage(R.string.alert_internet_disconnect);
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.yes, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mActivity.startActivity(new Intent(
							android.provider.Settings.ACTION_WIFI_SETTINGS));
				}
			});
			builder.setNegativeButton(R.string.no, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					exitApp();
				}
			});
			builder.show();
			return;
		}

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

	public void exitApp() {
		Intent intent = new Intent(mActivity, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(MainActivity.PARAM_EXITS, true);
		mActivity.startActivity(intent);
	}
}
