package tw.com.ischool.oneknow.channel;

import java.util.ArrayList;

import tw.com.ischool.oneknow.model.KnowDataSource;
import tw.com.ischool.oneknow.model.Knowledge;
import tw.com.ischool.oneknow.model.OnKnowledgeReceiveListener;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class UpdateChannelService extends Service {
	public static final String ACTION = "tw.com.ischool.oneknow.channel.updatediscover";
	public static final String PARAM_RAND = "rand";
	public static final String URL = "URL";

	private KnowDataSource mKnows;
	private Handler mHandler = new Handler();
	private long mDelay = 21600000;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mKnows = new KnowDataSource(this);
		mKnows.open();
		mHandler.postDelayed(mRunnable, mDelay);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		mHandler.removeCallbacks(mRunnable);
		mKnows.close();
		super.onDestroy();
	}

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			try {
				DiscoverTask task = new DiscoverTask(mKnows);
				task.setOnKnowledgeReceivedListener(new OnKnowledgeReceiveListener() {

					@Override
					public void onReceived(ArrayList<Knowledge> knowledges) {
						broadcast();
					}
				});

				task.execute();
			} catch (Exception e) {

			} finally {
				mHandler.postDelayed(this, mDelay);
			}
		}
	};

	private void broadcast() {
		Intent intent = new Intent();
		intent.setAction(ACTION);
		this.sendBroadcast(intent);
	}
}
