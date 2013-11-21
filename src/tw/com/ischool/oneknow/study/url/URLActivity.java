package tw.com.ischool.oneknow.study.url;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.study.UnitPlayerFactory;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class URLActivity extends Activity {
	public static final String PARAM_CONTENT = "content";
	public static final String PARAM_TYPE = "type";

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_url);

		getActionBar().hide();

		Intent intent = getIntent();
		String content = intent.getStringExtra(PARAM_CONTENT);
		String type = intent.getStringExtra(PARAM_TYPE);

		final LinearLayout processView = (LinearLayout) findViewById(R.id.progressInfo);
		final WebView webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				processView.setVisibility(View.GONE);
			}
		});
		if (type.equalsIgnoreCase(UnitPlayerFactory.UNIT_TYPE_WEB)) {
			webView.loadUrl(content);
			
		} else {
			webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8",
					null);
		}
	}
}
