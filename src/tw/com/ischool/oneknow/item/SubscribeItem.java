package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.learn.DisplayStatus;
import tw.com.ischool.oneknow.learn.SubscribeFragment;

public class SubscribeItem extends BaseItem{
	public SubscribeItem() {
		super.init(R.string.item_subscribe,
				android.R.drawable.ic_menu_report_image, DisplayStatus.LOGINED,
				ItemProvider.GROUP_LEARNING, SORT_NO_TAB, SubscribeFragment.class);
	}
}
