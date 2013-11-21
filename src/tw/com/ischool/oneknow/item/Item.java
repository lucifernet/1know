package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.learn.DisplayStatus;

public class Item extends BaseItem {

	public Item(int title, int icon, DisplayStatus status, int group,
			int sortInGroup, Class<?> fragmentClass) {
		super.init(title, icon, status, group, sortInGroup, fragmentClass);
	}
}
