package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.R;

public class LearningItem extends TabsItem {
	public LearningItem() {
		super.init(R.string.item_learning,
				android.R.drawable.ic_menu_report_image, DisplayStatus.NORMAL
				);
		
		super.add(new YourKnowledgeItem());
		super.add(new YourNotesItem());
	}
}
