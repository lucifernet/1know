package tw.com.ischool.oneknow.model.parser;

import org.json.JSONObject;

public interface IJSONParser<T> {
	T parse(JSONObject json);
}
