package tw.com.ischool.oneknow.learn;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.model.KnowDataSource;
import tw.com.ischool.oneknow.model.Knowledge;
import tw.com.ischool.oneknow.model.OnKnowledgeReceiveListener;
import tw.com.ischool.oneknow.model.parser.YourKnowledgeParser;
import android.os.AsyncTask;

public class YourKnowledgeTask extends AsyncTask<Void, Void, JSONArray> {
	private KnowDataSource mKnows;
	private OnKnowledgeReceiveListener mListener;

	public YourKnowledgeTask(KnowDataSource datasource) {
		mKnows = datasource;
	}

	@Override
	protected JSONArray doInBackground(Void... params) {
		try {
			return OneKnow.getFrom(OneKnow.SERVICE_LEARNING_KNOW, null,
					JSONArray.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(JSONArray result) {
		if (result != null) {
			ArrayList<Knowledge> knows = new ArrayList<Knowledge>();

			for (int i = 0; i < result.length(); i++) {
				JSONObject json;
				try {
					json = result.getJSONObject(i);
					Knowledge know = Knowledge.parseJSON(json,
							YourKnowledgeParser.class);
					mKnows.setYourKnowledge(know);
					knows.add(know);
				} catch (JSONException e) {
				}
			}

			if (mListener != null)
				mListener.onReceived(knows);
		}
	}

	public void setOnKnowledgeReceivedListener(
			OnKnowledgeReceiveListener listener) {
		mListener = listener;
	}

}
