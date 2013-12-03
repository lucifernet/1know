package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.learn.YourKnowsFragment;

public class YourKnowledgeItem extends FragmentItem {
	public YourKnowledgeItem() {
		super.init(R.string.item_your_knowledge, R.drawable.ic_knowledge,
				DisplayStatus.LOGINED, YourKnowsFragment.class);
	}
}
