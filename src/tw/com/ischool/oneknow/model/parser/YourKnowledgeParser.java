package tw.com.ischool.oneknow.model.parser;

import org.json.JSONObject;

import tw.com.ischool.oneknow.model.Knowledge;
import tw.com.ischool.oneknow.util.JSONUtil;

public class YourKnowledgeParser extends KnowledgeParser {

	@Override
	public Knowledge parse(JSONObject json) {
		Knowledge k = new Knowledge();

		k.setUqid(JSONUtil.getString(json, "uqid"));
		k.setName(JSONUtil.getString(json, "name"));
		k.setApproveCode(JSONUtil.getString(json, "approve_code"));
		k.setTotalTime(JSONUtil.getInt(json, "total_time"));
		k.setGainedTime(JSONUtil.getInt(json, "gained_time"));
		k.setFinishCount(JSONUtil.getInt(json, "finish_count"));
		k.setRating(JSONUtil.getInt(json, "rating"));
		k.setLastUpdate(JSONUtil.getString(json, "last_update"));
		k.setLastViewTime(JSONUtil.getString(json, "last_view_time"));
		k.setLogo(JSONUtil.getString(json, "logo"));
		k.setPage(JSONUtil.getString(json, "page"));
		k.setSubscribed(true);

		return k;
	}

}
