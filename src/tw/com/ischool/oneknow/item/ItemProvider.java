package tw.com.ischool.oneknow.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.learn.DisplayStatus;

public class ItemProvider {

	public static final int GROUP_NONE = -1;
	public static final int GROUP_CHANNEL = 1;
	public static final int GROUP_LEARNING = 2;
	public static final int GROUP_PROFILE = 3;

	private static ArrayList<BaseItem> sItems;

	static {
		sItems = new ArrayList<BaseItem>();

		sItems.add(new Item(R.string.item_login,
				android.R.drawable.ic_menu_agenda, DisplayStatus.UNLOGIN,
				GROUP_NONE, 0, null));
		// sItems.add(new Item(R.string.item_profile,
		// android.R.drawable.ic_input_add, DisplayStatus.LOGINED,
		// GROUP_NONE, 0, null));
		sItems.add(new Item(R.string.item_logout,
				android.R.drawable.ic_menu_myplaces, DisplayStatus.LOGINED,
				GROUP_NONE, 0, null));
		sItems.add(new Item(R.string.item_sep_learning,
				android.R.drawable.ic_menu_set_as, DisplayStatus.combine(
						DisplayStatus.SEPARATION, DisplayStatus.LOGINED),
				GROUP_LEARNING, BaseItem.SORT_NO_TAB, null));
		sItems.add(new YourKnowledgeItem());
		sItems.add(new YourNotesItem());
		sItems.add(new SubscribeItem());
		// sItems.add(new Item(R.string.item_your_activity,
		// android.R.drawable.ic_menu_myplaces, DisplayStatus.LOGINED,
		// GROUP_LEARNING, 2, null));
		// sItems.add(new Item(R.string.item_sep_channel,
		// android.R.drawable.ic_menu_myplaces, DisplayStatus.SEPARATION,
		// GROUP_NONE, 0, null));
		// sItems.add(new DiscoverItem());
		// sItems.add(new Item(R.string.item_editor_choice,
		// android.R.drawable.ic_menu_myplaces, DisplayStatus.NORMAL,
		// GROUP_CHANNEL, 1, DiscoverFragment.class));
		// sItems.add(new Item(R.string.item_your_channel,
		// android.R.drawable.ic_menu_myplaces, DisplayStatus.LOGINED,
		// GROUP_CHANNEL, 2, DiscoverFragment.class));
	}

	public static BaseItem getItem(int position) {
		return sItems.get(position);
	}

	public static List<BaseItem> getItems() {
		return sItems;
	}

	public static List<BaseItem> getTabItems(int group) {
		ArrayList<BaseItem> items = new ArrayList<BaseItem>();

		for (BaseItem item : sItems) {
			if (item.getSortInGroup() == BaseItem.SORT_NO_TAB)
				continue;

			if (item.getGroup() == group)
				items.add(item);
		}

		Collections.sort(items, new Comparator<BaseItem>() {

			@Override
			public int compare(BaseItem item1, BaseItem item2) {
				if (item1.getSortInGroup() > item2.getSortInGroup())
					return 1;
				if (item1.getSortInGroup() < item2.getSortInGroup())
					return -1;
				return 0;
			}
		});

		return items;
	}

	public static <T extends BaseItem> int findItemIndex(Class<T> type) {
		int i = 0;
		for (BaseItem item : sItems) {
			if (type.isInstance(item)) {
				return i;
			}
			i++;
		}

		return -1;
	}

	public static int getGroupTitleId(BaseItem item) {
		if (item.getStatus().isMember(DisplayStatus.SEPARATION))
			return item.getTitle();

		if(item.getSortInGroup() == BaseItem.SORT_NO_TAB)
			return item.getTitle();
		
		for (BaseItem it : sItems) {
			if (it.getGroup() != item.getGroup())
				continue;
			if (it.getStatus().isMember(DisplayStatus.SEPARATION))
				return it.getTitle();
		}
		return item.getTitle();
	}

}
