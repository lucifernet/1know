package tw.com.ischool.oneknow.study.draw;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DrawFragment extends Fragment implements IUnitPlayerHandler {

	public static final int CODE_FULL_SCREEN = 514;
	public static final String PARAM_CURRENT_DRAW = "currentDraw";
	public static final String JS_CALLBACK = "jscallback";

	private WebView mWebView;
	private LinearLayout mProcessing;
	private Timer mTimer;
	private String mUnitUqid;
	private UnitItem mUnitItem;
	private JSONArray mCurrentStrokes;
	private Activity mActivity;
	private JSCallback mJSCallback;
	private FrameLayout mContainer;
	private ImageButton mBtnSave;
	private ImageButton mBtnUndo;
	private ImageButton mBtnRedo;
	private ImageButton mBtnClean;
	private String mScreenShot;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();
		mProcessing.setVisibility(View.VISIBLE);

		if (getArguments().containsKey(PARAM_CURRENT_DRAW)) {
			this.createNewWeb(new WebReadyListener() {
				@Override
				public void onReady() {
					String jsonString = getArguments().getString(
							PARAM_CURRENT_DRAW);
					mCurrentStrokes = JSONUtil.parseToJSONArray(jsonString);
					renderStrokes();
					mProcessing.setVisibility(View.GONE);
				}
			});
		} else {
			GetStrokeTask task = new GetStrokeTask();
			task.setListener(new OnReceiveListener<JSONArray>() {

				@Override
				public void onReceive(JSONArray result) {
					mCurrentStrokes = result;
					createNewWeb(new WebReadyListener() {

						@Override
						public void onReady() {
							renderStrokes();
							mProcessing.setVisibility(View.GONE);
						}
					});
				}

				@Override
				public void onError(Exception e) {
					createNewWeb(new WebReadyListener() {
						@Override
						public void onReady() {
							mProcessing.setVisibility(View.GONE);
						}
					});
				}
			});
			task.execute();
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_draw, container,
				false);
		mContainer = (FrameLayout) view.findViewById(R.id.container_web);
		mProcessing = (LinearLayout) view.findViewById(R.id.progressInfo);
		mBtnSave = (ImageButton) view.findViewById(R.id.btnSave);
		mBtnUndo = (ImageButton) view.findViewById(R.id.btnUndo);
		mBtnRedo = (ImageButton) view.findViewById(R.id.btnRedo);
		mBtnClean = (ImageButton) view.findViewById(R.id.btnClean);

		mBtnUndo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.loadUrl("javascript:undo();");
			}
		});

		mBtnRedo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.loadUrl("javascript:redo();");
			}
		});

		mBtnClean.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.loadUrl("javascript:clean();");
			}
		});

		mBtnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveStrokes();
			}
		});

		Bundle bundle = getArguments();
		mUnitItem = (UnitItem) bundle.getSerializable(StudyActivity.PARAM_UNIT);

		JSONObject json = mUnitItem.getJSON();
		mUnitUqid = JSONUtil.getString(json, "uqid");

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(MainActivity.TAG, "Draw fragment start");
		mTimer = new Timer(true);
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				SaveHistoryTask task = new SaveHistoryTask(mUnitUqid);
				task.execute(0, 20000);

				Log.d(MainActivity.TAG, "Draw Fragment save history.");

			}
		}, 0, 20000);
	}

	@Override
	public void onDestroyView() {
		Log.d(MainActivity.TAG, "Draw fragment onDestroyView");
		if (mTimer != null)
			mTimer.cancel();

		super.onDestroyView();
	}

	@Override
	public void onStop() {
		Log.d(MainActivity.TAG, "Draw fragment stop");
		if (mTimer != null)
			mTimer.cancel();

		super.onStop();
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
		mJSCallback.addJSListener(new JSStrokeListener() {

			@Override
			public void onStrokesGotten() {
				Intent intent = new Intent(mActivity, DrawActivity.class);
				Bundle b = createBundle();
				intent.putExtras(b);
				startActivityForResult(intent, CODE_FULL_SCREEN);
			}
		});

		mWebView.loadUrl("javascript:getStrokes();");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_FULL_SCREEN) {
			String jsonString = data.getStringExtra(PARAM_CURRENT_DRAW);
			mCurrentStrokes = JSONUtil.parseToJSONArray(jsonString);

			// Log.d(MainActivity.TAG, "strokes : " + mCurrentStrokes.length());
			// mWebView.loadUrl("file:///android_asset/Canvas/test.html");

			this.createNewWeb(new WebReadyListener() {

				@Override
				public void onReady() {
					renderStrokes();
				}
			});
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

	private Bundle createBundle() {
		Bundle b = new Bundle();
		b.putString(PARAM_CURRENT_DRAW, mCurrentStrokes.toString());
		b.putSerializable(StudyActivity.PARAM_UNIT, mUnitItem);
		return b;
	}

	public void createBundle(final BundleListener listener) {
		mJSCallback.addJSListener(new JSStrokeListener() {
			@Override
			public void onStrokesGotten() {
				Bundle bundle = createBundle();
				listener.onBundleReady(bundle);
			}
		});

		mWebView.loadUrl("javascript:getStrokes();");
	}

	private void renderStrokes() {
		mWebView.loadUrl("javascript:setStrokes();");
	}

	private class JSCallback {

		private ArrayList<JSStrokeListener> _listeners;
		private ArrayList<JSScreenShotListener> _ssListeners;

		@JavascriptInterface
		public void getStrokes(String text) {
			mCurrentStrokes = JSONUtil.parseToJSONArray(text);

			if (_listeners != null) {
				for (JSStrokeListener l : _listeners)
					l.onStrokesGotten();
			}
			_listeners.clear();
		}

		@JavascriptInterface
		public String setStrokes() {
			if (mCurrentStrokes != null) {
				Log.d(MainActivity.TAG, "strokes : " + mCurrentStrokes.length());
				return mCurrentStrokes.toString();
			}
			return StringUtil.EMPTY;
		}

		@JavascriptInterface
		public void getScreenShot(String screenshot) {
			mScreenShot = screenshot;

			if (_ssListeners != null) {
				for (JSScreenShotListener l : _ssListeners)
					l.onScreenShotGotton(screenshot);
			}
		}

		public void addJSListener(JSStrokeListener listener) {
			if (_listeners == null)
				_listeners = new ArrayList<DrawFragment.JSStrokeListener>();
			_listeners.add(listener);
		}

		public void addJSListener(JSScreenShotListener listener) {
			if (_ssListeners == null)
				_ssListeners = new ArrayList<DrawFragment.JSScreenShotListener>();
			_ssListeners.add(listener);
		}
	}

	private interface JSStrokeListener {
		void onStrokesGotten();
	}

	private interface JSScreenShotListener {
		void onScreenShotGotton(String screenshot);
	}

	private interface WebReadyListener {
		void onReady();
	}

	public interface BundleListener {
		void onBundleReady(Bundle bundle);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void createNewWeb(final WebReadyListener listener) {
		mContainer.removeAllViews();
		mWebView = new WebView(mActivity);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebChromeClient(new WebChromeClient());
		mJSCallback = new JSCallback();
		mWebView.addJavascriptInterface(mJSCallback, JS_CALLBACK);

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				listener.onReady();
			}
		});

		FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mContainer.addView(mWebView, param);
		// mWebView.getSettings().setUseWideViewPort(true);
		// mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.loadUrl("file:///android_asset/Canvas/test.html");

		// ViewTreeObserver vto = mWebView.getViewTreeObserver();
		// vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		//
		// @SuppressWarnings("deprecation")
		// @SuppressLint("NewApi")
		// @Override
		// public void onGlobalLayout() {
		// mViewHeight = mWebView.getHeight();
		// mViewWidth = mWebView.getWidth();
		//
		// Log.d(MainActivity.TAG, "view width:" + mViewWidth + " height:"
		// + mViewHeight);
		// //mWebView.loadUrl("file:///android_asset/Canvas/test.html");
		//
		// InputStream is = null;
		// String str = getString(R.string.draw_load_error);
		// try {
		// is = mActivity.getAssets().open("Canvas/test.html");
		// int size = is.available();
		//
		// byte[] buffer = new byte[size];
		// is.read(buffer);
		// is.close();
		//
		// str = new String(buffer);
		// } catch (Exception ex) {
		// str = getString(R.string.draw_load_error);
		// String.format(str, ex.getMessage());
		// } finally {
		// if (is == null)
		// return;
		// try {
		// is.close();
		// } catch (Exception ex) {
		// }
		// }
		//
		// ScreenHelper screen = new ScreenHelper(mActivity);
		// int width = screen.toDp(mViewWidth);
		// int height = screen.toDp(mViewHeight);
		//
		// str = str.replace("FragmentWidth", String.valueOf(width));
		// str = str.replace("FragmentHeight", String.valueOf(height));
		//
		// //mWebView.loadUrl("file:///android_asset/Canvas/test.html");
		// mWebView.loadDataWithBaseURL("file:///android_asset/Canvas/test.html",
		// str, "text/html", "UTF-8", null);
		//
		// ViewTreeObserver obs = mWebView.getViewTreeObserver();
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		// obs.removeOnGlobalLayoutListener(this);
		// } else {
		// obs.removeGlobalOnLayoutListener(this);
		// }
		// }
		// });
	}

	private boolean mStrokeReady = false;
	private boolean mScreenShotReady = false;

	private void saveStrokes() {
		mStrokeReady = false;
		mScreenShotReady = false;

		mJSCallback.addJSListener(new JSStrokeListener() {
			@Override
			public void onStrokesGotten() {
				mStrokeReady = true;
				if (mScreenShotReady)
					readyToSave();
			}
		});

		mJSCallback.addJSListener(new JSScreenShotListener() {

			@Override
			public void onScreenShotGotton(String screenshot) {
				mScreenShotReady = true;
				if (mStrokeReady)
					readyToSave();
			}
		});

		mWebView.loadUrl("javascript:getScreenShot();");
		mWebView.loadUrl("javascript:getStrokes();");
	}

	private void readyToSave() {
		SaveTask task = new SaveTask();
		task.setListener(new OnReceiveListener<JSONObject>() {

			@Override
			public void onReceive(JSONObject result) {
				String text = getString(R.string.draw_save_completed);
				Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(Exception e) {
				String text = getString(R.string.draw_save_error);
				text = String.format(text, e.getMessage());
				Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
			}
		});
		task.execute();
	}

	private class SaveTask extends AsyncTask<Void, Void, JSONObject> {
		private OnReceiveListener<JSONObject> _listener;
		private Exception _ex;

		@Override
		protected JSONObject doInBackground(Void... params) {
			String serviceURL = OneKnow.SERVICE_STUDY_RESULT;
			serviceURL = String.format(serviceURL, mUnitUqid);

			//JSONObject req;
			try {
				//req = new JSONObject();
				JSONObject json = new JSONObject();
				json.put("unit_type", "draw");
				JSONObject result = new JSONObject();
				result.put("strokes", mCurrentStrokes);
				result.put("screenshot", mScreenShot);
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

	private class GetStrokeTask extends AsyncTask<Void, Void, JSONArray> {
		private OnReceiveListener<JSONArray> _listener;
		private Exception _ex;

		@Override
		protected JSONArray doInBackground(Void... params) {
			String serviceURL = OneKnow.SERVICE_GET_UNIT;
			serviceURL = String.format(serviceURL, mUnitUqid);

			try {
				JSONArray jsonArray = OneKnow.getFrom(serviceURL, null,
						JSONArray.class);
				JSONObject json = JSONUtil.getJSONObject(jsonArray, 0);
				JSONObject studyresult = json.getJSONObject("study_result");
				JSONObject result = studyresult.getJSONObject("result");
				return result.getJSONArray("strokes");
			} catch (Exception e) {
				_ex = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			if (_ex != null && _listener != null)
				_listener.onError(_ex);
			else
				_listener.onReceive(result);
		}

		public void setListener(OnReceiveListener<JSONArray> listener) {
			_listener = listener;
		}
	}
}
