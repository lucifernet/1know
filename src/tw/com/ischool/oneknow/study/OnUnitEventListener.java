package tw.com.ischool.oneknow.study;

import org.json.JSONObject;

public interface OnUnitEventListener {
	void onCompleted();

	void onStudyHistoryUpdated(JSONObject result);	
}
