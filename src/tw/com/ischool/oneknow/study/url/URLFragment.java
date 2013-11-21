package tw.com.ischool.oneknow.study.url;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.main.MainActivity;
import tw.com.ischool.oneknow.study.IUnitPlayerHandler;
import tw.com.ischool.oneknow.study.OnUnitEventListener;
import tw.com.ischool.oneknow.study.SaveHistoryTask;
import tw.com.ischool.oneknow.study.StudyActivity;
import tw.com.ischool.oneknow.study.UnitItem;
import tw.com.ischool.oneknow.study.UnitPlayerFactory;
import tw.com.ischool.oneknow.util.JSONUtil;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class URLFragment extends Fragment implements IUnitPlayerHandler {

	private WebView mWebView;
	private LinearLayout mProcessing;
	private String mUnitUqid;
	private String mContent;
	private String mType;
	private Timer mTimer;

	@Override
	public void onStart() {
		super.onStart();

		mTimer = new Timer(true);
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				SaveHistoryTask task = new SaveHistoryTask(mUnitUqid);
				task.execute(0, 20000);

				Log.d(MainActivity.TAG, "URL Fragment save history.");

			}
		}, 0, 20000);
	}

	@Override
	public void onDestroyView() {
		if (mTimer != null)
			mTimer.cancel();

		super.onDestroyView();
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_url, container, false);

		mWebView = (WebView) view.findViewById(R.id.webView);
		mProcessing = (LinearLayout) view.findViewById(R.id.progressInfo);
		mProcessing.setVisibility(View.VISIBLE);

		Bundle bundle = getArguments();
		UnitItem unitItem = (UnitItem) bundle
				.getSerializable(StudyActivity.PARAM_UNIT);

		mUnitUqid = JSONUtil.getString(unitItem.getJSON(), "uqid");

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setSupportZoom(true);

		mWebView.setWebViewClient(new WebViewClient() {

			public void onPageFinished(WebView view, String url) {
				mProcessing.setVisibility(View.GONE);
			}
		});

		JSONObject json = unitItem.getJSON();

		mType = JSONUtil.getString(json, "unit_type");
		if (mType.equalsIgnoreCase(UnitPlayerFactory.UNIT_TYPE_WEB)) {
			mContent = JSONUtil.getString(json, "content_url");
			mWebView.loadUrl(mContent);
		} else {
			mContent = JSONUtil.getString(json, "content");
			mWebView.loadDataWithBaseURL(null, mContent, "text/html", "UTF-8",
					null);
		}
		return view;
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
		Intent intent = new Intent(getActivity(), URLActivity.class);
		intent.putExtra(URLActivity.PARAM_CONTENT, mContent);
		intent.putExtra(URLActivity.PARAM_TYPE, mType);
		startActivity(intent);
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

}
