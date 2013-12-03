package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.learn.YourNoteFragment;

public class YourNotesItem extends FragmentItem {
	public YourNotesItem() {
		super.init(R.string.item_your_notes, R.drawable.your_note,
				DisplayStatus.LOGINED, YourNoteFragment.class);
	}
}
