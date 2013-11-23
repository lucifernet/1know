package tw.com.ischool.oneknow.learn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.main.IReloadable;
import tw.com.ischool.oneknow.main.ISearchable;
import tw.com.ischool.oneknow.model.Knowledge;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import tw.com.ischool.oneknow.model.parser.YourKnowledgeParser;
import tw.com.ischool.oneknow.study.StudyActivity;
import tw.com.ischool.oneknow.util.JSONUtil;
import tw.com.ischool.oneknow.util.ScreenHelper;
import tw.com.ischool.oneknow.util.StringUtil;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class YourNoteFragment extends Fragment implements IReloadable,
		ISearchable {

	public static final int CODE_PLAY = 285;
	private LinearLayout mContainer;
	private LinearLayout mProgress;
	private OnSearchListener mSearchListener;
	private OnReloadCompletedListener mReloadListener;
	// private JSONArray mJSONArray;
	private List<JSONObject> mJSONObjects;

	private String mKeyword;
	private Activity mActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_your_notes, container,
				false);

		mContainer = (LinearLayout) view.findViewById(R.id.container);
		mProgress = (LinearLayout) view.findViewById(R.id.progressInfo);
		mProgress.setVisibility(View.INVISIBLE);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();

		if (StringUtil.isNullOrWhitespace(mKeyword)) {
			reload();
		}
	}

	@Override
	public void setOnSearchListener(OnSearchListener listener) {
		mSearchListener = listener;
	}

	@Override
	public void search(String keyword) {
		mKeyword = keyword.toLowerCase(Locale.getDefault());

		// TODO
		ArrayList<JSONObject> jsons = new ArrayList<JSONObject>();
		for (JSONObject json : mJSONObjects) {
			String content = JSONUtil.getString(json, "content").toLowerCase(
					Locale.getDefault());

			if (content.contains(keyword)) {
				jsons.add(json);
				continue;
			}

			JSONObject knowObject = JSONUtil.getJSONObject(json, "know");
			String knowName = JSONUtil.getString(knowObject, "name")
					.toLowerCase(Locale.getDefault());
			if (knowName.contains(keyword)) {
				jsons.add(json);
				continue;
			}

			JSONObject unitObject = JSONUtil.getJSONObject(json, "unit");
			String unitName = JSONUtil.getString(unitObject, "name")
					.toLowerCase(Locale.getDefault());
			if (unitName.contains(keyword)) {
				jsons.add(json);
				continue;
			}
		}

		mJSONObjects = jsons;
		bindData();

		if (mSearchListener != null)
			mSearchListener.onSearchCompleted(jsons.size());
	}

	@Override
	public void reload() {
		mProgress.setVisibility(View.VISIBLE);
		NoteTask task = new NoteTask();
		task.setOnReceiveListener(new OnReceiveListener<JSONArray>() {

			@Override
			public void onReceive(JSONArray result) {
				mJSONObjects = JSONUtil.getJSONObjects(result);

				if (mSearchListener != null
						&& StringUtil.isNullOrWhitespace(mKeyword)) {
					mSearchListener.onDataReady();
				}

				if(!StringUtil.isNullOrWhitespace(mKeyword)){
					search(mKeyword);
				}
				
				bindData();
				mProgress.setVisibility(View.GONE);

				if (mReloadListener != null)
					mReloadListener.onCompleted();
			}

			@Override
			public void onError(Exception e) {
				if (mReloadListener != null)
					mReloadListener.onCompleted();

				mProgress.setVisibility(View.GONE);
				String text = getString(R.string.note_load_error);
				text = String.format(text, e.getMessage());
				Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
			}
		});
		task.execute();
	}

	private void bindData() {
		mContainer.removeAllViews();
		mContainer.setBackgroundColor(Color.rgb(0xfb, 0xfb, 0xfb));
		mContainer.setPadding(10, 10, 10, 10);

		ScreenHelper screen = new ScreenHelper(mActivity);
		int hPadding = 15;
		int maxWidth = screen.getWidthPixels() - (hPadding * 2);
		float bannerTextSize = 15;

		for (JSONObject json : mJSONObjects) {

			LinearLayout layout = new LinearLayout(mActivity);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 0, 0); // 黑邊與最外圍的間距
			// params.gravity = Gravity.CENTER_VERTICAL;
			layout.setGravity(Gravity.CENTER_VERTICAL);
			layout.setLayoutParams(params);
			layout.setPadding(hPadding, 0, hPadding, 0); // 黑邊與文字的間距
			layout.setBackgroundColor(Color.rgb(0x68, 0x6d, 0x6e));

			// 建立 know
			JSONObject knowObject = JSONUtil.getJSONObject(json, "know");
			String knowName = JSONUtil.getString(knowObject, "name");
			TextView txtNoteName = new TextView(mActivity);
			txtNoteName.setText(knowName);
			txtNoteName.setMaxLines(1);
			txtNoteName.setMaxWidth(maxWidth * 4 / 10);
			txtNoteName.setTextSize(TypedValue.COMPLEX_UNIT_SP, bannerTextSize);
			txtNoteName.setTextColor(Color.WHITE);
			txtNoteName.setTag(json);
			txtNoteName.setOnClickListener(new NoteClickListener(0));
			layout.addView(txtNoteName);
			mContainer.addView(layout);

			// 分隔線
			ImageView imgView = new ImageView(mActivity);
			imgView.setImageResource(R.drawable.note_sep);
			float height = screen.toPixelFloat(34);
			float width = height / 102 * 32;
			LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
					(int) width, (int) height);
			sepParams.setMargins(hPadding, 0, hPadding, 0);
			imgView.setLayoutParams(sepParams);
			layout.addView(imgView);

			// 建立 unit
			JSONObject unitObject = JSONUtil.getJSONObject(json, "unit");
			String unitName = JSONUtil.getString(unitObject, "name");
			TextView txtUnitName = new TextView(mActivity);
			txtUnitName.setText(unitName);
			txtUnitName.setMaxLines(1);
			txtUnitName.setMaxWidth(maxWidth * 4 / 10);
			txtUnitName.setTextColor(Color.WHITE);
			txtUnitName.setTextSize(TypedValue.COMPLEX_UNIT_SP, bannerTextSize);
			txtUnitName.setTag(json);
			txtUnitName.setOnClickListener(new NoteClickListener(1));
			layout.addView(txtUnitName);

			// 分隔線
			imgView = new ImageView(mActivity);
			imgView.setImageResource(R.drawable.note_sep);
			imgView.setLayoutParams(sepParams);
			layout.addView(imgView);

			// 補上時間
			int time = JSONUtil.getInt(json, "time");
			String displayTime = StudyActivity.getDisplayTime(mActivity, time);
			TextView txtTime = new TextView(mActivity);
			txtTime.setText(displayTime);
			txtTime.setMaxLines(1);
			txtTime.setMaxWidth(maxWidth * 2 / 10);
			txtTime.setTextColor(Color.WHITE);
			txtTime.setTag(json);
			txtTime.setOnClickListener(new NoteClickListener(2));

			layout.addView(txtTime);

			// 與上物間距
			TextView sep = new TextView(mActivity);
			sep.setWidth(1);
			sep.setHeight(10);
			sep.setBackgroundColor(mActivity.getResources().getColor(
					android.R.color.transparent));
			sep.setText(StringUtil.WHITESPACE);
			mContainer.addView(sep);

			// 建立 note border
			params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			LinearLayout border = new LinearLayout(mActivity);
			params.setMargins(0, 0, 0, 0); // 與上下物間的間距
			border.setLayoutParams(params);
			border.setBackgroundColor(Color.rgb(0xe5, 0xe5, 0xe5));
			border.setPadding(10, 10, 10, 10); // 外框與內框的間距

			LinearLayout border2 = new LinearLayout(mActivity);
			params.setMargins(0, 0, 0, 0); // border 的邊框
			border2.setLayoutParams(params);
			border2.setBackgroundColor(Color.rgb(0xfa, 0xfa, 0xfa));
			border2.setPadding(13, 18, 13, 18); // 文字內容與邊框的距離
			border.addView(border2);

			String content = JSONUtil.getString(json, "content");
			TextView txtNote = new TextView(mActivity);
			txtNote.setText(content);
			border2.addView(txtNote);
			mContainer.addView(border);

			// 與上物間距
			sep = new TextView(mActivity);
			sep.setWidth(1);
			sep.setHeight(20);
			sep.setBackgroundColor(mActivity.getResources().getColor(
					android.R.color.transparent));
			sep.setText(StringUtil.WHITESPACE);
			mContainer.addView(sep);

		}
	}

	private class NoteClickListener implements OnClickListener {

		private int _level = 0;

		/**
		 * @param level
		 *            : 0 : to knowledge, 1: to unit, 2: to time
		 * **/
		public NoteClickListener(int level) {
			_level = level;
		}

		@Override
		public void onClick(View v) {
			mProgress.setVisibility(View.VISIBLE);

			JSONObject json = (JSONObject) v.getTag();
			JSONObject knowObject = JSONUtil.getJSONObject(json, "know");
			String knowUqid = JSONUtil.getString(knowObject, "uqid");

			JSONObject unitObject = JSONUtil.getJSONObject(json, "unit");
			final String unitUqid = JSONUtil.getString(unitObject, "uqid");

			final int time = JSONUtil.getInt(json, "time");

			KnowTask task = new KnowTask();
			task.setListener(new OnReceiveListener<JSONObject>() {

				@Override
				public void onReceive(JSONObject result) {
					YourKnowledgeParser parser = new YourKnowledgeParser();
					Knowledge k = parser.parse(result);

					mProgress.setVisibility(View.GONE);

					Intent intent = new Intent(mActivity, StudyActivity.class);
					intent.putExtra(StudyActivity.PARAM_KNOW, k);

					if (_level > 0)
						intent.putExtra(StudyActivity.PARAM_TARGET_UNIT_UQID,
								unitUqid);

					if (_level > 1)
						intent.putExtra(StudyActivity.PARAM_TARGET_TIME, time);

					startActivityForResult(intent, CODE_PLAY);
				}

				@Override
				public void onError(Exception e) {
					mProgress.setVisibility(View.GONE);
					String text = mActivity
							.getString(R.string.note_get_know_error);
					text = String.format(text, e.getMessage());
					Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
				}
			});
			task.execute(knowUqid);

		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_PLAY)
			reload();
	};

	@Override
	public void setOnReloadCompletedListener(OnReloadCompletedListener listener) {
		mReloadListener = listener;
	}

	private class NoteTask extends AsyncTask<Void, Void, JSONArray> {
		private Exception _ex;
		private OnReceiveListener<JSONArray> _listener;

		@Override
		protected JSONArray doInBackground(Void... params) {
			try {
				return OneKnow.getFrom(OneKnow.SERVICE_YOUR_NOTES, null,
						JSONArray.class);
			} catch (Exception e) {
				_ex = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			if (_ex != null)
				_listener.onError(_ex);
			else
				_listener.onReceive(result);
		}

		public void setOnReceiveListener(OnReceiveListener<JSONArray> listener) {
			_listener = listener;
		}
	}

	private class KnowTask extends AsyncTask<String, Void, JSONArray> {
		private Exception _exception;
		private OnReceiveListener<JSONObject> _listener;
		private String mKnowUqid;

		@Override
		protected JSONArray doInBackground(String... params) {
			mKnowUqid = params[0];
			try {
				String serviceURL = OneKnow.SERVICE_KNOW_INFO;
				serviceURL = String.format(serviceURL, "");

				return OneKnow.getFrom(serviceURL, null, JSONArray.class);
			} catch (Exception e) {
				_exception = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			if (_exception != null)
				_listener.onError(_exception);
			else {
				for (JSONObject json : JSONUtil.getJSONObjects(result)) {
					if (JSONUtil.getString(json, "uqid").equals(mKnowUqid)) {
						_listener.onReceive(json);
						break;
					}
				}
			}
		}

		public void setListener(OnReceiveListener<JSONObject> listener) {
			_listener = listener;
		}
	}

}
