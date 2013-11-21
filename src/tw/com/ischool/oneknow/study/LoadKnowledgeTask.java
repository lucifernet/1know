package tw.com.ischool.oneknow.study;

import org.json.JSONArray;
import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import android.os.AsyncTask;

public class LoadKnowledgeTask extends AsyncTask<String, Void, JSONObject> {

	private String mKnowUqid;
	private Exception mException;
	private OnReceiveListener<JSONObject> mListener;

	@Override
	protected JSONObject doInBackground(String... params) {
		mKnowUqid = params[0];
		try {
			String url = OneKnow.SERVICE_LEARNING_KNOW + "/"
					+ mKnowUqid;
			JSONArray array = OneKnow.getFrom(url, null, JSONArray.class);
			if(array.length() > 0)
				return array.getJSONObject(0);
		} catch (Exception e) {
			mException = e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		if (result != null && mListener != null) {
			mListener.onReceive(result);
		} else if (mException != null && mListener != null) {
			mListener.onError(mException);
		}
	}

	public void setOnReceiveListener(OnReceiveListener<JSONObject> listener) {
		mListener = listener;
	}
}
