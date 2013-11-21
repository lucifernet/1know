package tw.com.ischool.oneknow.study;

import org.json.JSONObject;

import tw.com.ischool.oneknow.main.MainActivity;
import tw.com.ischool.oneknow.study.draw.DrawFragment;
import tw.com.ischool.oneknow.study.poll.PollFragment;
import tw.com.ischool.oneknow.study.qa.QAFragment;
import tw.com.ischool.oneknow.study.quiz.QuizFragment;
import tw.com.ischool.oneknow.study.url.URLFragment;
import tw.com.ischool.oneknow.study.vimeo.VimeoFragment;
import tw.com.ischool.oneknow.study.youtube.YoutubeVideoFragment;
import tw.com.ischool.oneknow.util.JSONUtil;
import android.app.Fragment;
import android.util.Log;

public class UnitPlayerFactory {
	public static final String UNIT_TYPE_VIDEO = "video";
	public static final String UNIT_TYPE_WEB = "web";
	public static final String UNIT_TYPE_QUIZ = "quiz";
	public static final String UNIT_TYPE_POLL = "poll";
	public static final String UNIT_TYPE_DRAW = "draw";
	public static final String UNIT_TYPE_EMBED = "embed";
	public static final String UNIT_TYPE_QA = "qa";

	public static Fragment createInstance(JSONObject json) {
		String type = JSONUtil.getString(json, "unit_type");
		if (type.equalsIgnoreCase(UNIT_TYPE_VIDEO)) {
			String urlString = JSONUtil.getString(json, "content_url");

			if (urlString.contains(YoutubeVideoFragment.YOUTUBE)) {
				return new YoutubeVideoFragment();
			} else if (urlString.contains(VimeoFragment.VIMEO)) {
				return new VimeoFragment();
			}
		} else if (type.equalsIgnoreCase(UNIT_TYPE_WEB)
				|| type.equalsIgnoreCase(UNIT_TYPE_EMBED)) {
			return new URLFragment();
		} else if (type.equalsIgnoreCase(UNIT_TYPE_QUIZ)) {
			return new QuizFragment();
		} else if (type.equalsIgnoreCase(UNIT_TYPE_DRAW)) {
			return new DrawFragment();
		} else if (type.equalsIgnoreCase(UNIT_TYPE_POLL)) {
			return new PollFragment();
		} else if (type.equalsIgnoreCase(UNIT_TYPE_QA)) {
			return new QAFragment();
		} else {
			Log.d(MainActivity.TAG, "type is : " + type);
		}

		return null;
	}
}
