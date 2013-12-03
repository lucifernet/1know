package tw.com.ischool.oneknow.study;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.ischool.oneknow.util.JSONUtil;

public class UnitItemHandler {

	private ArrayList<UnitItem> _items;

	public UnitItemHandler() {
		_items = new ArrayList<UnitItem>();
	}

	public void putUnit(JSONObject json) {
		JSONObject chapterObject = null;
		try {
			chapterObject = json.getJSONObject("chapter");
		} catch (JSONException e) {

		}

		UnitItem itemUnit = new UnitItem(json);

		if (chapterObject != null) {
			String chName = JSONUtil.getString(chapterObject, "name");
			UnitItem itemChapter = this.findChapter(chName);

			if (itemChapter == null) {
				itemChapter = new UnitItem(chName);
				_items.add(itemChapter);
			}
			itemChapter.addChapterTime(itemUnit.getTime());
		}
		_items.add(itemUnit);
	}

	public ArrayList<UnitItem> getItems() {
		return _items;
	}

	private UnitItem findChapter(String chapterName) {
		for (UnitItem item : _items) {
			if (item.getName().equals(chapterName)
					&& item.getMode() == UnitItem.MODE_CHAPTER)
				return item;
		}
		return null;
	}

	public int findUnitIndex(String unitUqid) {
		for (int i = 0; i < _items.size(); i++) {
			UnitItem item = _items.get(i);
			if (item.getMode() == UnitItem.MODE_CHAPTER)
				continue;

			JSONObject json = item.getJSON();
			String uqid = JSONUtil.getString(json, "uqid");
			if (uqid.equals(unitUqid))
				return i;
		}
		return -1;
	}

	public ArrayList<UnitItem> getSelectableItems() {
		ArrayList<UnitItem> items = new ArrayList<UnitItem>();

		for (UnitItem item : _items) {
			if (item.getMode() == UnitItem.MODE_CHAPTER)
				continue;
			items.add(item);
		}

		return items;
	}

	public int getSelectableIndex(UnitItem item) {
		int index = -1;
		for (UnitItem it : _items) {
			if (item.getMode() == UnitItem.MODE_CHAPTER)
				continue;

			index += 1;
			if (item == it)
				break;
		}
		return index;
	}
}
