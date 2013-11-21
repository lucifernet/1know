package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.channel.DiscoverFragment;
import tw.com.ischool.oneknow.learn.DisplayStatus;

public class DiscoverItem extends BaseItem {
	public DiscoverItem() {
		super.init(R.string.item_discover, android.R.drawable.ic_menu_myplaces,
				DisplayStatus.NORMAL, ItemProvider.GROUP_CHANNEL, 0,
				DiscoverFragment.class);
	}
}
