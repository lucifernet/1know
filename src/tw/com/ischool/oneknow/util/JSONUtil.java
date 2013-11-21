package tw.com.ischool.oneknow.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtil {

	public static String getString(JSONObject json, String name) {
		try {
			return json.getString(name);
		} catch (JSONException e) {
			return StringUtil.EMPTY;
		}
	}

	public static int getInt(JSONObject json, String name) {
		if (json == null)
			return 0;

		try {
			return json.getInt(name);
		} catch (JSONException e) {
			return 0;
		}
	}

	public static CharSequence getDateString(JSONObject json, String name) {
		String str = getString(json, name);
		if (StringUtil.isNullOrWhitespace(str))
			return StringUtil.EMPTY;

		String[] array = str.split("T");
		return array[0];
	}

	public static boolean getBoolean(JSONObject json, String name) {
		try {
			return json.getBoolean(name);
		} catch (JSONException e) {
			return false;
		}
	}

	public static int getBoolInt(JSONObject json, String name) {
		return getBoolean(json, name) ? 1 : 0;
	}

	public static JSONArray parseToJSONArray(String jsonArrayString) {
		if (StringUtil.isNullOrWhitespace(jsonArrayString))
			return new JSONArray();

		try {
			return new JSONArray(jsonArrayString);
		} catch (JSONException e) {
			return new JSONArray();
		}
	}

	public static List<JSONObject> toJSONObjects(JSONArray jsonArray) {
		ArrayList<JSONObject> jsons = new ArrayList<JSONObject>();
		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				Object object = jsonArray.get(i);

				if (object instanceof JSONObject) {
					jsons.add((JSONObject) object);
				}
			} catch (JSONException e) {
			}
		}
		return jsons;
	}

	public static JSONArray getJSONArray(JSONObject json, String name) {
		try {
			return json.getJSONArray(name);
		} catch (JSONException e) {
			return new JSONArray();
		}
	}

	public static double getDouble(JSONObject json, String name) {
		try {
			return json.getDouble(name);
		} catch (JSONException e) {
			return 0;
		}
	}

	public static List<JSONObject> getJSONObjects(JSONArray jsonArray) {
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();

		for (int i = 0; i < jsonArray.length(); i++) {			
			try {
				Object obj = jsonArray.get(i);
				if (obj instanceof JSONObject) {
					list.add((JSONObject) obj);
				}
			} catch (JSONException e) {				
			}

		}

		return list;
	}

	public static JSONObject getJSONObject(JSONObject json, String name) {
		try {
			return json.getJSONObject(name);
		} catch (JSONException e) {
			return new JSONObject();
		}
	}

	public static JSONObject getJSONObject(JSONArray jsonArray, int index) {
		try {
			return jsonArray.getJSONObject(index);
		} catch (JSONException e) {
			return null;
		}
	}

	public static JSONObject parseToJSONObject(String jsonString) {
		try {
			return new JSONObject(jsonString);
		} catch (JSONException e) {
			return new JSONObject();
		}
	}
}
