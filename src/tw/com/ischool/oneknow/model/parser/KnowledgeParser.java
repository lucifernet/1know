package tw.com.ischool.oneknow.model.parser;

import org.json.JSONObject;

import tw.com.ischool.oneknow.model.Knowledge;

public abstract class KnowledgeParser implements IJSONParser<Knowledge> {

	@Override
	public abstract Knowledge parse(JSONObject json);
}
