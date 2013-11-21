package tw.com.ischool.oneknow.study;

import org.json.JSONArray;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import android.os.AsyncTask;

public class LoadUnitsTask extends AsyncTask<String, Void, JSONArray> {
	private String mKnowUqid;
	private Exception mException;
	private OnReceiveListener<JSONArray> mListener;

	@Override
	protected JSONArray doInBackground(String... params) {
		mKnowUqid = params[0];
		try {
			String url = String
					.format(OneKnow.SERVICE_LEARNING_UNIT, mKnowUqid);
			return OneKnow.getFrom(url, null, JSONArray.class);
		} catch (Exception e) {
			mException = e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(JSONArray result) {
		if (result != null && mListener != null) {
			mListener.onReceive(result);
		} else if (mException != null && mListener != null) {
			mListener.onError(mException);
		}
	}

	public void setOnReceiveListener(OnReceiveListener<JSONArray> listener) {
		mListener = listener;
	}
}
