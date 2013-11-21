package tw.com.ischool.oneknow.model.parser;

import org.json.JSONObject;

import tw.com.ischool.oneknow.model.Knowledge;
import tw.com.ischool.oneknow.util.JSONUtil;

public class DiscoverKnowledgeParser extends KnowledgeParser {

	@Override
	public Knowledge parse(JSONObject json) {
		Knowledge k = new Knowledge();
		k.setDiscover(true);
		k.setUqid(JSONUtil.getString(json, "uqid"));
		k.setName(JSONUtil.getString(json, "name"));
		k.setLastUpdate(JSONUtil.getString(json, "last_update"));
		k.setRating(JSONUtil.getInt(json, "rating"));
		k.setReader(JSONUtil.getInt(json, "reader"));
		k.setPublic(JSONUtil.getBoolean(json, "is_public"));
		k.setSubscribed(JSONUtil.getBoolean(json, "subscribed"));
		k.setLogo(JSONUtil.getString(json, "logo"));
		k.setPage(JSONUtil.getString(json, "page"));
		return k;
	}

}
