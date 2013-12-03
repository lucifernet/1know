package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.login.LoginActivity;
import tw.com.ischool.oneknow.main.MainActivity;
import android.app.Activity;
import android.content.Intent;

public class LoginItem extends ActionItem {
	public LoginItem() {
		super.init(R.string.item_login, android.R.drawable.ic_menu_agenda,
				DisplayStatus.GUEST, new PaddingTask() {

					@Override
					public void invoke(Activity activity) {
						Intent intent = new Intent(activity,
								LoginActivity.class);
						activity.startActivityForResult(intent,
								MainActivity.REQUEST_CODE_LOGIN);

					}
				});
	}
}
