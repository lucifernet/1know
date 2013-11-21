package tw.com.ischool.oneknow.study.quiz;

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
import tw.com.ischool.oneknow.util.StringUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class QuizFragment extends Fragment implements IUnitPlayerHandler {

	public static final int CODE_FULL_SCREEN = 513;
	public static final String PARAM_CURRENT_INDEX = "CurrentIndex";
	public static final String PARAM_QUIZ_JSON = "quizJSON";

	private LinearLayout mLayoutOptions;
	private Button mBtnPrev;
	private Button mBtnNext;
	private Button mBtnCheck;
	private TextView mTxtCount;
	private LinearLayout mProcess;
	private String mUnitUqid;
	private Activity mActivity;
	private JSONArray mJSONArray;
	private int mCurrentQuizIndex;
	private View mMainView;
	private FrameLayout mLayoutWebContent;
	private PopupWindow mPopWindow;
	private Timer mTimer;
	private UnitItem mUnitItem;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();
		if (getArguments().containsKey(PARAM_QUIZ_JSON)) {
			renderQuiz(mCurrentQuizIndex);
			mProcess.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(MainActivity.TAG, "Quiz fragment start");
		mTimer = new Timer(true);
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				SaveHistoryTask task = new SaveHistoryTask(mUnitUqid);
				task.execute(0, 20000);

				Log.d(MainActivity.TAG, "Quiz Fragment save history.");

			}
		}, 0, 20000);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		Log.d(MainActivity.TAG, "Quiz fragment onDestroyView");
		if (mTimer != null) 
			mTimer.cancel();
		
		super.onDestroyView();
	}

	@Override
	public void onStop() {
		Log.d(MainActivity.TAG, "Quiz fragment stop");
		if (mTimer != null) 
			mTimer.cancel();
		

		super.onStop();
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_quiz, container, false);
		mMainView = view;
		// mAnswers = new HashMap<String, QuizFragment.Answers>();
		mLayoutWebContent = (FrameLayout) view.findViewById(R.id.layout_web);

		mLayoutOptions = (LinearLayout) view
				.findViewById(R.id.container_options);
		mBtnPrev = (Button) view.findViewById(R.id.btnPrev);
		mBtnNext = (Button) view.findViewById(R.id.btnNext);
		mBtnCheck = (Button) view.findViewById(R.id.btnCheck);
		mTxtCount = (TextView) view.findViewById(R.id.txtCount);
		mProcess = (LinearLayout) view.findViewById(R.id.progressInfo);

		Bundle bundle = getArguments();
		mUnitItem = (UnitItem) bundle.getSerializable(StudyActivity.PARAM_UNIT);

		JSONObject json = mUnitItem.getJSON();
		mUnitUqid = JSONUtil.getString(json, "uqid");

		if (!bundle.containsKey(PARAM_QUIZ_JSON)) {
			QuizTask task = new QuizTask();
			task.setListener(new OnReceiveListener<JSONArray>() {

				@Override
				public void onReceive(JSONArray result) {
					mJSONArray = result;
					mProcess.setVisibility(View.GONE);
					if (result.length() > 0)
						renderQuiz(0);
				}

				@Override
				public void onError(Exception e) {
					mProcess.setVisibility(View.GONE);
					String text = getString(R.string.quiz_get_error);
					text = String.format(text, e.getMessage());
					Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
				}
			});
			mProcess.setVisibility(View.VISIBLE);
			task.execute();
		} else {
			String quizString = bundle.getString(PARAM_QUIZ_JSON);
			try {
				mJSONArray = new JSONArray(quizString);
			} catch (JSONException e) {
				mJSONArray = new JSONArray();
			}
			mCurrentQuizIndex = bundle.getInt(PARAM_CURRENT_INDEX);
		}

		mBtnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveAnswers();
				renderQuiz(mCurrentQuizIndex + 1);
			}
		});

		mBtnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveAnswers();
				renderQuiz(mCurrentQuizIndex - 1);
			}
		});

		mBtnCheck.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveAnswers();
				checkAnswer();
				submitAnswer();
			}
		});

		return view;
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void renderQuiz(int index) {
		mLayoutWebContent.removeAllViews();

		mBtnPrev.setEnabled(index != 0);
		mBtnNext.setEnabled(index != mJSONArray.length() - 1);
		mTxtCount.setVisibility(index == mJSONArray.length() - 1 ? View.GONE
				: View.VISIBLE);
		mBtnCheck.setVisibility(index != mJSONArray.length() - 1 ? View.GONE
				: View.VISIBLE);

		JSONObject json = JSONUtil.getJSONObject(mJSONArray, index);
		if (json == null)
			return;

		mCurrentQuizIndex = index;

		WebView webView = createWebView(json);
		mLayoutWebContent.addView(webView);

		mLayoutOptions.removeAllViews();
		JSONArray options = JSONUtil.getJSONArray(json, "options");
		boolean isSingleSelect = JSONUtil.getString(json, "quiz_type")
				.equalsIgnoreCase("single");

		CheckboxClickListener listener = new CheckboxClickListener(
				isSingleSelect);

		for (int i = 0; i < options.length(); i++) {
			JSONObject option = JSONUtil.getJSONObject(options, i);
			CheckBox checkbox = new CheckBox(mActivity);
			checkbox.setText(JSONUtil.getString(option, "item"));
			checkbox.setOnCheckedChangeListener(listener);
			checkbox.setTag(option);

			checkbox.setChecked(JSONUtil.getBoolean(option, "checked"));

			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params1.setMargins(20, 0, 10, 10);
			mLayoutOptions.addView(checkbox, params1);
		}

		mTxtCount.setText(index + 1 + " / " + mJSONArray.length());
	}

	@SuppressWarnings("deprecation")
	private void checkAnswer() {
		if (mPopWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.layout.popup_quiz, null);

			mPopWindow = new PopupWindow(view, mLayoutOptions.getWidth(),
					mMainView.getHeight() - mBtnCheck.getHeight());
		}

		mPopWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopWindow.setFocusable(true);
		mPopWindow.setOutsideTouchable(true);

		View view = mPopWindow.getContentView();
		LinearLayout container = (LinearLayout) view
				.findViewById(R.id.container);
		container.removeAllViews();
		view.setBackgroundColor(Color.WHITE);

		LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(7, 5, 7, 5);

		for (int i = 0; i < mJSONArray.length(); i++) {
			TextView txtQuiz = createTitleTextView(R.string.quiz);
			container.addView(txtQuiz);

			JSONObject quiz = JSONUtil.getJSONObject(mJSONArray, i);

			WebView webView = createWebView(quiz);
			container.addView(webView);

			TextView txtCorrect = createTitleTextView(R.string.quiz_correct_ans);
			container.addView(txtCorrect);

			JSONArray options = JSONUtil.getJSONArray(quiz, "options");
			for (int j = 0; j < options.length(); j++) {
				JSONObject option = JSONUtil.getJSONObject(options, j);
				boolean correct = JSONUtil.getBoolean(option, "correct");
				if (!correct)
					continue;

				TextView t = new TextView(mActivity);
				t.setText(JSONUtil.getString(option, "item"));
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				param.setMargins(20, 0, 10, 0);
				container.addView(t, param);
			}

			TextView txtYours = createTitleTextView(R.string.quiz_your_ans);
			container.addView(txtYours);

			boolean correct = true;

			for (int j = 0; j < options.length(); j++) {
				JSONObject option = JSONUtil.getJSONObject(options, j);
				boolean c1 = JSONUtil.getBoolean(option, "correct");
				boolean c2 = JSONUtil.getBoolean(option, "checked");

				if (c1 != c2)
					correct = false;

				if (!c2)
					continue;

				TextView t = new TextView(mActivity);
				t.setText(JSONUtil.getString(option, "item"));
				if (c1 != c2) {
					t.setTextColor(Color.RED);
				} else {
					t.setTextColor(Color.rgb(0x17, 0x89, 0x51));
				}
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				param.setMargins(20, 0, 10, 0);
				container.addView(t, param);
			}

			if (!correct) {
				txtYours.setTextColor(Color.RED);
			} else {
				txtYours.setTextColor(Color.rgb(0x17, 0x89, 0x51));
			}

			String explain = JSONUtil.getString(quiz, "explain");
			if (!StringUtil.isNullOrWhitespace(explain)) {
				TextView txtExplain = createTitleTextView(R.string.quiz_explain);
				container.addView(txtExplain);

				txtExplain = new TextView(mActivity);
				txtExplain.setText(explain);
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				param.setMargins(20, 3, 10, 10);
				container.addView(txtExplain, param);
			}

			final String explainURL = JSONUtil.getString(quiz, "explain_url");
			if (!StringUtil.isNullOrWhitespace(explainURL)) {
				TextView txtExplainURL = createTitleTextView(R.string.quiz_explain);
				container.addView(txtExplainURL);

				txtExplainURL = new TextView(mActivity);
				txtExplainURL.setText(explainURL);
				txtExplainURL.setTextColor(Color.BLUE);
				txtExplainURL.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(explainURL));
						startActivity(i);
					}
				});
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				param.setMargins(20, 3, 10, 10);
				container.addView(txtExplainURL, param);
			}

			TextView textView = new TextView(mActivity);
			textView.setBackgroundColor(Color.DKGRAY);
			textView.setLayoutParams(new ViewGroup.LayoutParams(
					LayoutParams.MATCH_PARENT, 1));
			container.addView(textView);
		}

		mPopWindow.showAsDropDown(mBtnCheck,
				(0 - mLayoutOptions.getWidth() / 2),
				(0 - mBtnCheck.getHeight() - mPopWindow.getHeight()));

	}

	private void submitAnswer(){
		SaveTask task = new SaveTask();
		task.setListener(new OnReceiveListener<JSONObject>() {

			@Override
			public void onReceive(JSONObject result) {
				String text = getString(R.string.quiz_save_completed);
				Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(Exception e) {
				String text = getString(R.string.quiz_save_error);
				text = String.format(text, e.getMessage());
				Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
			}
		});
		task.execute();
	}
	
	private TextView createTitleTextView(int titleId) {
		TextView textView = new TextView(mActivity);
		LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(7, 5, 7, 5);
		textView.setText(titleId);
		textView.setLayoutParams(lp);
		textView.setTypeface(null, Typeface.BOLD_ITALIC);
		textView.setTextSize(17);

		return textView;
	}

	@SuppressLint("SetJavaScriptEnabled")
	private WebView createWebView(JSONObject json) {
		WebView webView = new WebView(mActivity);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient());

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		String content = JSONUtil.getString(json, "content");
		webView.setLayoutParams(params);
		webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
		return webView;
	}

	private void saveAnswers() {
		for (int i = 0; i < mLayoutOptions.getChildCount(); i++) {
			View view = mLayoutOptions.getChildAt(i);
			if (!(view instanceof CheckBox))
				continue;

			CheckBox checkbox = (CheckBox) view;

			JSONObject opt = (JSONObject) checkbox.getTag();

			try {
				opt.put("checked", checkbox.isChecked());
			} catch (JSONException e) {

			}
		}
	}

	public class CheckboxClickListener implements OnCheckedChangeListener {
		private boolean _isSingleSelect;

		public CheckboxClickListener(boolean isSingleSelect) {
			_isSingleSelect = isSingleSelect;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (!_isSingleSelect)
				return;
			if (!isChecked)
				return;

			for (int i = 0; i < mLayoutOptions.getChildCount(); i++) {
				View view = mLayoutOptions.getChildAt(i);
				if (!(view instanceof CheckBox))
					continue;

				CheckBox box = (CheckBox) view;
				if (box == buttonView)
					continue;
				box.setChecked(false);
			}
		}
	}

	@Override
	public void setUnitEventListener(OnUnitEventListener listener) {

	}

	@Override
	public void beforeDestory() {
		if (mTimer != null) 
			mTimer.cancel();
	}

	@Override
	public void openFullScreen() {
		if (mJSONArray == null)
			return;

		Intent intent = new Intent(mActivity, QuizActivity.class);
		Bundle b = createBundle();
		intent.putExtras(b);
		startActivityForResult(intent, CODE_FULL_SCREEN);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_FULL_SCREEN) {
			int index = data.getIntExtra(PARAM_CURRENT_INDEX, 0);
			String jsonString = data.getStringExtra(PARAM_QUIZ_JSON);
			mJSONArray = JSONUtil.parseToJSONArray(jsonString);
			renderQuiz(index);
		}
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

	private class QuizTask extends AsyncTask<Void, Void, JSONArray> {

		private OnReceiveListener<JSONArray> _listener;
		private Exception _exception;

		@Override
		protected JSONArray doInBackground(Void... params) {
			String serviceURL = OneKnow.SERVICE_STUDY_QUIZ;
			serviceURL = String.format(serviceURL, mUnitUqid);

			try {
				return OneKnow.getFrom(serviceURL, null, JSONArray.class);
			} catch (Exception e) {
				_exception = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			if (_exception != null && _listener != null)
				_listener.onError(_exception);
			else if (result != null && _listener != null)
				_listener.onReceive(result);
		}

		public void setListener(OnReceiveListener<JSONArray> listener) {
			_listener = listener;
		}
	}

	public Bundle createBundle() {
		Bundle b = new Bundle();
		b.putInt(PARAM_CURRENT_INDEX, mCurrentQuizIndex);
		b.putString(PARAM_QUIZ_JSON, mJSONArray.toString());
		b.putSerializable(StudyActivity.PARAM_UNIT, mUnitItem);
		return b;
	}
	
	private class SaveTask extends AsyncTask<Void, Void, JSONObject> {
		private OnReceiveListener<JSONObject> _listener;
		private Exception _ex;

		@Override
		protected JSONObject doInBackground(Void ...params) {
			String serviceURL = OneKnow.SERVICE_STUDY_RESULT;
			serviceURL = String.format(serviceURL, mUnitUqid);

			//JSONObject req;
			try {
				//req = new JSONObject();
				JSONObject json = new JSONObject();
				json.put("unit_type", "quiz");
				JSONArray result = new JSONArray();

				for(int i=0;i<mJSONArray.length();i++){
					JSONObject q = JSONUtil.getJSONObject(mJSONArray, i);
					
					String uqid = JSONUtil.getString(q, "uqid");
					JSONObject saveObject = new JSONObject();
					saveObject.put("uqid", uqid);
					JSONArray correctObject = new JSONArray();
					JSONArray answersObject = new JSONArray();
															
					JSONArray options = JSONUtil.getJSONArray(q, "options");
					for(int j=0;j<options.length();j++){
						JSONObject option = JSONUtil.getJSONObject(options, j);
						
						boolean cor = JSONUtil.getBoolean(option, "correct");
						boolean chk = JSONUtil.getBoolean(option, "checked");
						int value = JSONUtil.getInt(option, "value");
						
						if (cor)
							correctObject.put(value);
						if(chk)
							answersObject.put(value);
					}
					saveObject.put("correct", correctObject);
					saveObject.put("answer", answersObject);
					result.put(saveObject);
				}
				
				json.put("result", result);
				//req.put("content", json);

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
}
