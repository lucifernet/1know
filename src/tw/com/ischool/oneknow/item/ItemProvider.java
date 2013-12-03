package tw.com.ischool.oneknow.item;

import java.util.ArrayList;
import java.util.List;

public class ItemProvider {

	public static final int GROUP_NONE = -1;
	public static final int GROUP_CHANNEL = 1;
	public static final int GROUP_LEARNING = 2;
	public static final int GROUP_PROFILE = 3;

	private static ArrayList<BaseItem> sItems;

	static {
		sItems = new ArrayList<BaseItem>();
		sItems.add(new ProfilerItem());
		sItems.add(new LoginItem());
		sItems.add(new SwitchGuestItem());
		sItems.add(new LogoutItem());
		sItems.add(new LearningItem());
		sItems.add(new SubscribeItem());
	}

	public static BaseItem getItem(int position) {
		return sItems.get(position);
	}

	// public static List<BaseItem> getItems() {
	// return sItems;
	// }

	public static List<BaseItem> getItems(DisplayStatus... condition) {
		ArrayList<BaseItem> items = new ArrayList<BaseItem>();

		for (BaseItem item : sItems) {
			boolean match = true;

			if (condition != null) {
				for (DisplayStatus status : condition) {
					if (item.getStatus() == DisplayStatus.NORMAL)
						continue;

					if (!item.getStatus().isMember(status)) {
						match = false;
						continue;
					}
				}
			}
			if (match)
				items.add(item);
		}

		return items;
	}

	public static int findIndex(List<BaseItem> itemList, Class<?> class1) {

		for (int i = 0; i < itemList.size(); i++) {
			BaseItem item = itemList.get(i);
			if (class1.isInstance(item))
				return i;
		}

		return -1;
	}
}
