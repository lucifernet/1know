package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.login.LoginHelper;
import tw.com.ischool.oneknow.main.MainActivity;
import tw.com.ischool.oneknow.util.StringUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class LogoutItem extends ActionItem {
	public LogoutItem() {
		super.init(R.string.item_logout, android.R.drawable.ic_menu_agenda,
				DisplayStatus.LOGINED, new PaddingTask() {

					@Override
					public void invoke(final Activity activity) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(
								activity);
						dialog.setTitle(R.string.confirm);
						dialog.setMessage(R.string.logout_confirm);
						dialog.setIcon(android.R.drawable.ic_dialog_alert);
						dialog.setCancelable(false);
						dialog.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										OneKnow.logout();
										SharedPreferences settings = activity
												.getSharedPreferences(
														LoginHelper.TAG, 0);
										SharedPreferences.Editor editor = settings
												.edit();
										editor.putString(
												LoginHelper.ATTR_USERNAME,
												StringUtil.EMPTY);
										editor.putString(
												LoginHelper.ATTR_PASSWORD,
												StringUtil.EMPTY);
										editor.commit();

										MainActivity main = (MainActivity) activity;
										main.autoLogin();
									}
								});
						dialog.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						dialog.show();

					}
				});
	}
}
