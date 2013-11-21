package tw.com.ischool.oneknow.study.vimeo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.study.IUnitPlayerHandler;
import tw.com.ischool.oneknow.study.OnUnitEventListener;
import tw.com.ischool.oneknow.study.StudyActivity;
import tw.com.ischool.oneknow.study.UnitItem;
import tw.com.ischool.oneknow.util.JSONUtil;
import tw.com.ischool.oneknow.util.StringUtil;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.PluginState;

public class VimeoFragment extends Fragment implements IUnitPlayerHandler {
	public static final String VIMEO = "vimeo.com";

	private HTML5WebView mVimeoPlayer;

	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mVimeoPlayer = new HTML5WebView(getActivity());

		Bundle bundle = getArguments();
		UnitItem unitItem = (UnitItem) bundle
				.getSerializable(StudyActivity.PARAM_UNIT);
		String url = JSONUtil.getString(unitItem.getJSON(), "content_url");
		String vimeoid = url.substring(url.lastIndexOf('/') + 1);

		// Auto playing vimeo videos in Android webview
		mVimeoPlayer.getSettings().setJavaScriptEnabled(true);
		mVimeoPlayer.getSettings().setAllowFileAccess(true);
		mVimeoPlayer.getSettings().setAppCacheEnabled(true);
		mVimeoPlayer.getSettings().setDomStorageEnabled(true);
		mVimeoPlayer.getSettings().setPluginState(PluginState.OFF);
		mVimeoPlayer.getSettings().setAllowFileAccess(true);

		InputStream is = getResources().openRawResource(R.raw.vimeo_1_0);
		String content = convertStreamToString(is);
		content = content.replace("{vimeoid}", vimeoid);

		mVimeoPlayer.loadDataWithBaseURL(null, content, "text/html", "UTF-8",
				null);

		return mVimeoPlayer.getLayout();
	}

	public String convertStreamToString(InputStream is) {
		if (is == null)
			return StringUtil.EMPTY;

		Writer writer = new StringWriter();

		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} catch (IOException ex) {
			return StringUtil.EMPTY;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return writer.toString();

	}

	@Override
	public void setUnitEventListener(OnUnitEventListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDestory() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openFullScreen() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void seekTo(double toSecond) {

	}

	@Override
	public double getCurrentTime() {
		// TODO Auto-generated method stub
		return 0;
	}
}
