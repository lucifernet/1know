package tw.com.ischool.oneknow.study;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.ischool.oneknow.util.JSONUtil;

public class UnitItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int MODE_UNIT = 1;
	public static final int MODE_CHAPTER = 2;

	private int _mode;
	private int _time;
	private String _name;
	private String _jsonString;

	public UnitItem(JSONObject json) {
		_mode = MODE_UNIT;
		_jsonString = json.toString();
		_name = JSONUtil.getString(json, "name");
		_time = JSONUtil.getInt(json, "content_time");
	}

	public UnitItem(String chapterName) {
		_mode = MODE_CHAPTER;
		_time = 0;
		_name = chapterName;
	}

	public int getMode() {
		return _mode;
	}

	public int getTime() {
		return _time;
	}

	public JSONObject getJSON() {

		try {
			return new JSONObject(_jsonString);
		} catch (JSONException e) {
			return null;
		}
	}

	public String getName() {
		return _name;
	}

	public void addChapterTime(int time) {
		_time += time;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof UnitItem) {
			UnitItem item = (UnitItem) o;
			if (item.getMode() != _mode)
				return false;
			if (!item.getName().equals(_name))
				return false;

			String id1 = JSONUtil.getString(item.getJSON(), "uqid");
			String id2 = JSONUtil.getString(getJSON(), "uqid");
			return id1.equals(id2);
		}
		return super.equals(o);
	}
}
