package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.learn.DisplayStatus;
import tw.com.ischool.oneknow.learn.YourKnowFragment;

public class YourKnowledgeItem extends BaseItem {
	public YourKnowledgeItem() {
		super.init(R.string.item_your_knowledge,
				android.R.drawable.ic_menu_report_image, DisplayStatus.LOGINED,
				ItemProvider.GROUP_LEARNING, 0, YourKnowFragment.class);
	}
}
