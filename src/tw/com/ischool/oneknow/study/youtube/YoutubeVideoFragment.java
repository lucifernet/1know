package tw.com.ischool.oneknow.study.youtube;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.main.MainActivity;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import tw.com.ischool.oneknow.study.DeveloperKey;
import tw.com.ischool.oneknow.study.IUnitPlayerHandler;
import tw.com.ischool.oneknow.study.OnUnitEventListener;
import tw.com.ischool.oneknow.study.SaveHistoryTask;
import tw.com.ischool.oneknow.study.StudyActivity;
import tw.com.ischool.oneknow.study.UnitItem;
import tw.com.ischool.oneknow.util.JSONUtil;
import tw.com.ischool.oneknow.util.StringUtil;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.OnFullscreenListener;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayerView;

public class YoutubeVideoFragment extends Fragment implements
		YouTubePlayer.OnInitializedListener, IUnitPlayerHandler {

	public static final String YOUTUBE = "www.youtube.com";
	public static final String PARAM_VIDEO_KEY = "v";

	private static final int RECOVERY_DIALOG_REQUEST = 1;

	private YouTubePlayer mPlayer;
	private int mSecondFlag;
	private String mUnitUqid;
	private Timer mTimer;
	private OnUnitEventListener mListener;
	private boolean mIsFullScreen;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_youtube_video,
				container, false);

		YouTubePlayerView player = (YouTubePlayerView) view
				.findViewById(R.id.youtube_view);

		player.initialize(DeveloperKey.DEVELOPER_KEY, this);

		view.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && mPlayer != null
						&& mIsFullScreen) {
					mPlayer.setFullscreen(false);
					return true;
				}
				return false;
			}
		});

		return view;
	}

	@Override
	public void onInitializationFailure(YouTubePlayer.Provider provider,
			YouTubeInitializationResult errorReason) {
		if (errorReason.isUserRecoverableError()) {
			errorReason.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST)
					.show();
		} else {
			String errorMessage = String.format(
					getString(R.string.error_player), errorReason.toString());
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG)
					.show();
		}
	}

	public void setOnUnitEventListener(OnUnitEventListener listener) {
		this.mListener = listener;
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider,
			YouTubePlayer player, boolean wasRestored) {
		mPlayer = player;
		player.setPlayerStyle(PlayerStyle.DEFAULT);
		player.setShowFullscreenButton(true);

		mTimer = new Timer(true);
		mTimer.scheduleAtFixedRate(new MyTimerTask(), 0, 20000);
		if (!wasRestored) {
			Bundle bundle = getArguments();
			UnitItem unitItem = (UnitItem) bundle
					.getSerializable(StudyActivity.PARAM_UNIT);

			JSONObject json = unitItem.getJSON();
			mUnitUqid = JSONUtil.getString(json, "uqid");
			int seekTo = bundle.getInt(StudyActivity.PARAM_SEEK_TO) * 1000;
			
			String videoId = getVideoId(json);

			player.setPlaybackEventListener(new PlaybackEventListener() {

				@Override
				public void onStopped() {
					Log.d(MainActivity.TAG,
							"on Stopped : " + mPlayer.getCurrentTimeMillis());
					recalcHistoryAndSave(mPlayer.getCurrentTimeMillis());
				}

				@Override
				public void onSeekTo(int newPositionMillis) {
					Log.d(MainActivity.TAG, "on SeekTo : " + newPositionMillis);
				}

				@Override
				public void onPlaying() {
					Log.d(MainActivity.TAG, "on Playing");
					// mCounter = 0;
					mSecondFlag = mPlayer.getCurrentTimeMillis();
				}

				@Override
				public void onPaused() {
					Log.d(MainActivity.TAG,
							"on Paused : " + mPlayer.getCurrentTimeMillis());
					recalcHistoryAndSave(mPlayer.getCurrentTimeMillis());
				}

				@Override
				public void onBuffering(boolean arg0) {
					Log.d(MainActivity.TAG, "on Buffering");

				}
			});

			player.setPlayerStateChangeListener(new PlayerStateChangeListener() {

				@Override
				public void onVideoStarted() {
					Log.d(MainActivity.TAG, "Video Started");
				}

				@Override
				public void onVideoEnded() {
					if (mListener != null)
						mListener.onCompleted();
					Log.d(MainActivity.TAG, "Video Ended");
					recalcHistoryAndSave(mPlayer.getCurrentTimeMillis());
				}

				@Override
				public void onLoading() {
					Log.d(MainActivity.TAG, "on Loading");
				}

				@Override
				public void onLoaded(String arg0) {
					Log.d(MainActivity.TAG, "on Loaded");
				}

				@Override
				public void onError(ErrorReason arg0) {
					Log.d(MainActivity.TAG, "on Error : " + arg0);
				}

				@Override
				public void onAdStarted() {
					Log.d(MainActivity.TAG, "on Ad Started");
				}
			});

			player.setOnFullscreenListener(new OnFullscreenListener() {

				@Override
				public void onFullscreen(boolean isFullScreen) {
					mIsFullScreen = isFullScreen;
				}
			});

			player.loadVideo(videoId, seekTo);
		}
	}

	private String getVideoId(JSONObject json) {
		String urlString = JSONUtil.getString(json, "content_url");
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			return StringUtil.EMPTY;
		}
		String query = url.getQuery();
		String[] pairs = query.split("&");
		String key = StringUtil.EMPTY;
		for (String pair : pairs) {
			String[] r = pair.split("=");
			String pk = r[0];
			String pv = r[1];
			if (pk.equals(YoutubeVideoFragment.PARAM_VIDEO_KEY)) {
				key = pv;
				break;
			}
		}
		return key;
	}

	private void recalcHistoryAndSave(int millInSeconds) {
		int counter = millInSeconds - mSecondFlag;
		mSecondFlag = millInSeconds;

		saveHistory(mSecondFlag, counter);
	}

	private void saveHistory(int lastSecondWatched, int secondsWatched) {
		if (secondsWatched == 0)
			return;

		SaveHistoryTask task = new SaveHistoryTask(mUnitUqid);
		task.setOnReceiveListener(new OnReceiveListener<JSONObject>() {

			@Override
			public void onReceive(JSONObject result) {
				if (mListener != null && result != null)
					mListener.onStudyHistoryUpdated(result);
			}

			@Override
			public void onError(Exception e) {
			}
		});
		task.execute(lastSecondWatched, secondsWatched);

		String msg = "LastSecondWatched : %s ; SecondWatched : %s";
		String str = String.format(msg, String.valueOf(lastSecondWatched),
				String.valueOf(secondsWatched));
		Log.w(MainActivity.TAG, str);
	}

	// private class SaveHistoryTask extends AsyncTask<Integer, Void,
	// JSONObject> {
	//
	// @Override
	// protected JSONObject doInBackground(Integer... params) {
	// double lastSecondWatched = params[0];
	// double secondsWatched = params[1];
	//
	// String serviceName = String.format(OneKnow.SERVICE_STUDY_HISTORY,
	// mUnitUqid);
	//
	// JSONObject json = new JSONObject();
	// try {
	//
	// json.put("last_second_watched", lastSecondWatched / 1000);
	// json.put("seconds_watched", secondsWatched / 1000);
	//
	// return OneKnow.postTo(serviceName, json, JSONObject.class);
	// } catch (Exception ex) {
	// Log.e(MainActivity.TAG, ex.toString());
	// }
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(JSONObject result) {
	// if (mListener != null && result != null)
	// mListener.onStudyHistoryUpdated(result);
	// }
	// }

	private class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			if (mPlayer != null) {
				try {
					recalcHistoryAndSave(mPlayer.getCurrentTimeMillis());
				} catch (Exception e) {
					this.cancel();
				}
			}
		}
	}

	@Override
	public void setUnitEventListener(OnUnitEventListener listener) {
		mListener = listener;
	}

	@Override
	public void beforeDestory() {
		if (mTimer != null)
			mTimer.purge();
	}

	@Override
	public void openFullScreen() {
		if (mPlayer != null) {
			mPlayer.setFullscreen(true);
		}
	}

	@Override
	public void pause() {
		if (mPlayer != null) {
			mPlayer.pause();
		}
	}

	@Override
	public void seekTo(double toSecond) {
		if (mPlayer != null) {
			double s = toSecond * 1000;
			mPlayer.seekToMillis((int) s);
		}
	}

	@Override
	public boolean handleBackPressed() {
		if (mPlayer != null && mIsFullScreen) {
			mPlayer.setFullscreen(false);
			return true;
		}
		return false;
	}

	@Override
	public double getCurrentTime() {
		if (mPlayer != null) {
			return mPlayer.getCurrentTimeMillis() / 1000;
		}
		return 0;
	}
}
