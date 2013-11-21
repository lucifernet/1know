package tw.com.ischool.oneknow.study.qa;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.main.MainActivity;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import tw.com.ischool.oneknow.study.IUnitPlayerHandler;
import tw.com.ischool.oneknow.study.OnUnitEventListener;
import tw.com.ischool.oneknow.study.SaveHistoryTask;
import tw.com.ischool.oneknow.study.StudyActivity;
import tw.com.ischool.oneknow.study.UnitItem;
import tw.com.ischool.oneknow.util.JSONUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannedString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class QAFragment extends Fragment implements IUnitPlayerHandler {

	public static final String PARAM_QA_JSON = "qaJSON";
	private static final int CODE_FULL_SCREEN = 516;
	private OnUnitEventListener mListener;
	private LinearLayout mContainer;
	private String mUnitUqid;
	private UnitItem mUnitItem;
	private Activity mActivity;
	private Timer mTimer;
	private JSONObject mJSONObject;
	private EditText mEditText;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();
		if (getArguments().containsKey(PARAM_QA_JSON)) {
			try {
				mJSONObject = new JSONObject(getArguments().getString(
						PARAM_QA_JSON));
			} catch (JSONException e) {
			}
			bind(mJSONObject);
		} else {
			UnitTask task = new UnitTask();
			task.setListener(new OnReceiveListener<JSONObject>() {

				@Override
				public void onReceive(JSONObject result) {
					mJSONObject = result;
					bind(result);
				}

				@Override
				public void onError(Exception e) {
				}
			});
			task.execute();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_qa, container, false);

		Bundle bundle = getArguments();
		mUnitItem = (UnitItem) bundle.getSerializable(StudyActivity.PARAM_UNIT);

		JSONObject json = mUnitItem.getJSON();
		mUnitUqid = JSONUtil.getString(json, "uqid");

		mContainer = (LinearLayout) view.findViewById(R.id.container);

		Button btnSave = (Button) view.findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveQA();

				if (mListener != null)
					mListener.onCompleted();
			}
		});

		mEditText = (EditText) view.findViewById(R.id.editContent);
		JSONObject resultResult = JSONUtil.getJSONObject(json, "study_result");
		String str = JSONUtil.getString(resultResult, "result");
		mEditText.setText(Html.fromHtml(str));

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(MainActivity.TAG, "QA fragment start");
		mTimer = new Timer(true);
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				SaveHistoryTask task = new SaveHistoryTask(mUnitUqid);
				task.execute(0, 20000);

				Log.d(MainActivity.TAG, "QA Fragment save history.");

			}
		}, 0, 20000);
	}

	@Override
	public void onDestroyView() {
		Log.d(MainActivity.TAG, "QA fragment onDestroyView");
		if (mTimer != null)
			mTimer.cancel();

		super.onDestroyView();
	}

	@Override
	public void onStop() {
		Log.d(MainActivity.TAG, "QA fragment stop");
		if (mTimer != null)
			mTimer.cancel();

		super.onStop();
	}

	private void saveQA() {
		SaveTask task = new SaveTask();
		task.setListener(new OnReceiveListener<JSONObject>() {

			@Override
			public void onReceive(JSONObject result) {
				String text = getString(R.string.qa_save_completed);
				Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(Exception e) {
				String text = getString(R.string.qa_save_error);
				text = String.format(text, e.getMessage());
				Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
			}
		});

		ArrayList<Integer> values = new ArrayList<Integer>();
		for (int i = 0; i < mContainer.getChildCount(); i++) {
			View child = mContainer.getChildAt(i);
			if (!(child instanceof CheckBox))
				continue;

			CheckBox b = (CheckBox) child;
			if (!b.isChecked())
				continue;

			JSONObject json = (JSONObject) b.getTag();
			values.add(JSONUtil.getInt(json, "value"));
		}

		task.execute(values.toArray(new Integer[values.size()]));
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void bind(JSONObject result) {
		mContainer.removeAllViews();

		String content = JSONUtil.getString(result, "content");
		WebView webView = new WebView(mActivity);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient());

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		webView.setLayoutParams(params);
		webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
		mContainer.addView(webView);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_FULL_SCREEN) {
			String jsonString = data.getStringExtra(PARAM_QA_JSON);
			mJSONObject = JSONUtil.parseToJSONObject(jsonString);
			bind(mJSONObject);
		}
	}

	@Override
	public void setUnitEventListener(OnUnitEventListener listener) {
		mListener = listener;
	}

	@Override
	public void beforeDestory() {

	}

	@Override
	public void openFullScreen() {
		Intent intent = new Intent(mActivity, QAActivity.class);
		Bundle b = createBundle();
		intent.putExtras(b);
		startActivityForResult(intent, CODE_FULL_SCREEN);
	}

	@Override
	public boolean handleBackPressed() {
		return false;
	}

	@Override
	public void pause() {

	}

	@Override
	public void seekTo(double toSecond) {

	}

	@Override
	public double getCurrentTime() {
		return 0;
	}

	private class UnitTask extends AsyncTask<Void, Void, JSONObject> {

		private OnReceiveListener<JSONObject> _listener;
		private Exception _exception;

		@Override
		protected JSONObject doInBackground(Void... params) {
			String serviceURL = OneKnow.SERVICE_GET_UNIT;
			serviceURL = String.format(serviceURL, mUnitUqid);

			try {
				JSONArray jsonArray = OneKnow.getFrom(serviceURL, null,
						JSONArray.class);
				return jsonArray.getJSONObject(0);
			} catch (Exception e) {
				_exception = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (_exception != null && _listener != null)
				_listener.onError(_exception);
			else if (result != null && _listener != null)
				_listener.onReceive(result);
		}

		public void setListener(OnReceiveListener<JSONObject> listener) {
			_listener = listener;
		}
	}

	private class SaveTask extends AsyncTask<Integer, Void, JSONObject> {
		private OnReceiveListener<JSONObject> _listener;
		private Exception _ex;

		@Override
		protected JSONObject doInBackground(Integer... params) {
			String serviceURL = OneKnow.SERVICE_STUDY_RESULT;
			serviceURL = String.format(serviceURL, mUnitUqid);

			try {			
				JSONObject json = new JSONObject();		
				SpannedString bar = new SpannedString(mEditText.getText().toString());
				json.put("result", Html.toHtml(bar));
				json.put("unit_type", "qa");
							
				return OneKnow.putTo(serviceURL, json, JSONObject.class);
			} catch (Exception e) {
				_ex = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (_ex != null && _listener != null)
				_listener.onError(_ex);
			else
				_listener.onReceive(result);
		}

		public void setListener(OnReceiveListener<JSONObject> listener) {
			_listener = listener;
		}
	}

	public Bundle createBundle() {
		Bundle b = new Bundle();
		b.putString(PARAM_QA_JSON, mJSONObject.toString());
		b.putSerializable(StudyActivity.PARAM_UNIT, mUnitItem);
		return b;
	}
}
