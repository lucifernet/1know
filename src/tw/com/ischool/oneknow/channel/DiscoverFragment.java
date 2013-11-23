package tw.com.ischool.oneknow.channel;

import java.util.ArrayList;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.model.KnowDataSource;
import tw.com.ischool.oneknow.model.KnowImageTask;
import tw.com.ischool.oneknow.model.KnowImageTask.OnImageCompleteListener;
import tw.com.ischool.oneknow.model.KnowImageTask.OnImageProgresListener;
import tw.com.ischool.oneknow.model.Knowledge;
import tw.com.ischool.oneknow.model.OnKnowledgeReceiveListener;
import tw.com.ischool.oneknow.util.CircleProgressBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class DiscoverFragment extends Fragment {
	private GridView mGridView;
	private LinearLayout mProgress;
	private ArrayList<Knowledge> mKnowList;

	private KnowDataSource mKnows;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mKnows = new KnowDataSource(getActivity());

		View view = inflater.inflate(R.layout.fragment_knowledge, container,
				false);

		mGridView = (GridView) view.findViewById(R.id.lvKnowledge);
		mProgress = (LinearLayout) view.findViewById(R.id.progressInfo);
		mProgress.setVisibility(View.INVISIBLE);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		mKnows.open();

		mKnowList = mKnows.getDiscoverKnowledges();

		if (mKnowList.size() == 0) {
			mProgress.setVisibility(View.VISIBLE);
			DiscoverTask task = new DiscoverTask(mKnows);
			task.setOnKnowledgeReceivedListener(new OnKnowledgeReceiveListener() {

				@Override
				public void onReceived(ArrayList<Knowledge> knowledges) {
					mKnowList = knowledges;
					bindData();
					mProgress.setVisibility(View.GONE);
				}
			});

			task.execute();
		} else {
			bindData();
		}

		IntentFilter filter = new IntentFilter(UpdateChannelService.ACTION);

		this.getActivity().registerReceiver(mReceiver, filter);
	}

	@Override
	public void onStop() {
		mKnows.close();
		this.getActivity().unregisterReceiver(mReceiver);

		super.onStop();
	}

	private void bindData() {
		KnowAdapter adapter = new KnowAdapter();
		mGridView.setAdapter(adapter);
	}

	private class KnowAdapter extends ArrayAdapter<Knowledge> {

		private LayoutInflater _inflater;

		public KnowAdapter() {
			super(getActivity(), R.layout.item_discover_know, mKnowList);

			_inflater = (LayoutInflater) getActivity().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.item_discover_know,
						null);
			}

			final ImageView imgKnow = (ImageView) convertView
					.findViewById(R.id.imgKnowledge);
			final CircleProgressBar progImg = (CircleProgressBar) convertView
					.findViewById(R.id.progress);

			TextView txtKnowName = (TextView) convertView
					.findViewById(R.id.txtKnowName);
			TextView txtSubCount = (TextView) convertView
					.findViewById(R.id.txtSubscriberCount);
			RatingBar rating = (RatingBar) convertView
					.findViewById(R.id.rating);

			Knowledge know = mKnowList.get(position);

			txtKnowName.setText(know.getName());
			txtSubCount.setText(String.valueOf(know.getReader()));
			rating.setRating(know.getRating());

			Bitmap cachedImage = know.getCachedLogoBitmap(getActivity());
			if (cachedImage != null) {
				imgKnow.setImageBitmap(cachedImage);
				return convertView;
			}

			// if (Knowledge.loadLogoImage(getActivity(), know) != null) {
			// imgKnow.setImageBitmap(know.getLogoBitmap());
			// return convertView;
			// }

			KnowImageTask task = new KnowImageTask(getActivity(), know);
			progImg.setVisibility(View.VISIBLE);
			progImg.setMaxProgress(100);

			task.setOnImageCompleteListener(new OnImageCompleteListener() {

				@Override
				public void onImageComplete(Bitmap bitmap) {
					imgKnow.setImageBitmap(bitmap);
					progImg.setVisibility(View.INVISIBLE);
				}
			});

			task.setOnImageProgressListener(new OnImageProgresListener() {

				@Override
				public void onProgress(int progress) {
					progImg.setProgress(progress);
				}
			});
			task.execute();

			return convertView;
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mKnowList = mKnows.getDiscoverKnowledges();
			bindData();
		}

	};
}
