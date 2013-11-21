package tw.com.ischool.oneknow.study;

import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.main.MainActivity;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import android.os.AsyncTask;
import android.util.Log;

public class SaveHistoryTask extends AsyncTask<Integer, Void, JSONObject> {

	private String mUnitUqid;
	private OnReceiveListener<JSONObject> mListener;
	
	public SaveHistoryTask(String unituqid){
		mUnitUqid = unituqid;
	}
	
	@Override
	protected JSONObject doInBackground(Integer... params) {
		double lastSecondWatched = params[0];
		double secondsWatched = params[1];

		String serviceName = String.format(OneKnow.SERVICE_STUDY_HISTORY,
				mUnitUqid);

		JSONObject json = new JSONObject();
		try {

			json.put("last_second_watched", lastSecondWatched / 1000);
			json.put("seconds_watched", secondsWatched / 1000);

			return OneKnow.postTo(serviceName, json, JSONObject.class);
		} catch (Exception ex) {
			Log.e(MainActivity.TAG, ex.toString());
		}
		return null;
	}

	public void setOnReceiveListener(OnReceiveListener<JSONObject> listener){
		mListener = listener;
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		if(mListener != null)
			mListener.onReceive(result);
//		if (mListener != null && result != null)
//			mListener.onStudyHistoryUpdated(result);
	}
}