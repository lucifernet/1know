package tw.com.ischool.oneknow.item;

import tw.com.ischool.oneknow.learn.DisplayStatus;

public abstract class BaseItem {
	protected DisplayStatus _status = DisplayStatus.NORMAL;
	protected int _title;
	protected int _icon;
	protected int _group;
	protected int _sortInGroup;
	protected Class<?> _fragmentClass;

	protected void init(int title, int icon, DisplayStatus status, int group,
			int sortInGroup, Class<?> fragmentClass) {
		_title = title;
		_icon = icon;
		_status = status;
		_group = group;
		_sortInGroup = sortInGroup;
		_fragmentClass = fragmentClass;
	}

	public int getTitle() {
		return _title;
	}

	public int getIcon() {
		return _icon;
	}

	public DisplayStatus getStatus() {
		return _status;
	}

	public int getGroup() {
		return _group;
	}

	public int getSortInGroup() {
		return _sortInGroup;
	}

	public Class<?> getFragmentClass() {
		return _fragmentClass;
	}
}
