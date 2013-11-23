package tw.com.ischool.oneknow.learn;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

public class SubscribeFragment extends Fragment {
	public static final String ACTION_SUBSCRIBE = "KnowledgeSubscribed";
	public static final int CODEE_QR_CODE = 555;

	private Activity mActivity;
	private FrameLayout mProgress;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mActivity = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_subscribe, container,
				false);

		mProgress = (FrameLayout)view.findViewById(R.id.progress);
		mProgress.setVisibility(View.GONE);
		
		Button btnQR = (Button) view.findViewById(R.id.btnQRCode);
		btnQR.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent(
							"com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
					intent.putExtra("SAVE_HISTORY", false);
					startActivityForResult(intent, CODEE_QR_CODE);
				} catch (Exception ex) {
					// 如果發生錯誤，則表示應該還未安裝 ZXing 程式，幫他導到 market 下載吧
					Uri marketUri = Uri
							.parse("market://details?id=com.google.zxing.client.android");
					Intent marketIntent = new Intent(Intent.ACTION_VIEW,
							marketUri);
					startActivity(marketIntent);
				}
			}
		});

		final EditText editKnowCode = (EditText) view
				.findViewById(R.id.editKnowCode);

		Button btnCode = (Button) view.findViewById(R.id.btnSubscribe);
		btnCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editKnowCode.setError(null);
				mProgress.setVisibility(View.VISIBLE);
				
				if (editKnowCode.length() == 0) {
					String error = getString(R.string.subscribe_empty_code);
					editKnowCode.setError(error);
					return;
				}

				SubscribeTask task = new SubscribeTask();
				task.setListener(new OnReceiveListener<Void>() {

					@Override
					public void onReceive(Void result) {
						editKnowCode.setError(null);
						Toast.makeText(mActivity,
								getString(R.string.subscribe_succeed),
								Toast.LENGTH_LONG).show();
						mProgress.setVisibility(View.GONE);
					}

					@Override
					public void onError(Exception e) {
						String error = getString(R.string.subscribe_error);
						error = String.format(error, e.getMessage());
						editKnowCode.setError(error);
						mProgress.setVisibility(View.GONE);
					}
				});
				task.execute(editKnowCode.getText().toString());
			}
		});

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODEE_QR_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				subscribe(contents);
			} else if (resultCode == Activity.RESULT_CANCELED) {
			}
		}
	}

	private void subscribe(String content) {
		if (!content.contains(OneKnow.DOMAIN)) {
			Toast.makeText(mActivity,
					getString(R.string.subscribe_invalid_qrcode),
					Toast.LENGTH_LONG).show();
			return;
		}

		String code = content.substring(content.lastIndexOf('/') + 1);
		mProgress.setVisibility(View.VISIBLE);
		SubscribeTask task = new SubscribeTask();
		task.setListener(new OnReceiveListener<Void>() {

			@Override
			public void onReceive(Void result) {
				Toast.makeText(mActivity,
						getString(R.string.subscribe_succeed),
						Toast.LENGTH_LONG).show();
				mProgress.setVisibility(View.GONE);
			}

			@Override
			public void onError(Exception e) {
				String error = getString(R.string.subscribe_error);
				error = String.format(error, e.getMessage());
				Toast.makeText(mActivity, error, Toast.LENGTH_LONG).show();
				mProgress.setVisibility(View.GONE);
			}
		});
		task.execute(code);
	}

	private class SubscribeTask extends AsyncTask<String, Void, Void> {
		private Exception _exception;
		private OnReceiveListener<Void> _listener;

		@Override
		protected Void doInBackground(String... params) {
			String code = params[0];

			String serviceURL = OneKnow.SERVICE_SUBSCRIBE;
			serviceURL = String.format(serviceURL, code);
			try {
				OneKnow.postTo(serviceURL, null, Void.class);
			} catch (Exception e) {
				_exception = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (_exception != null && _listener != null) {
				_listener.onError(_exception);
			} else {
				_listener.onReceive(result);
				broadcast();
			}
		}

		public void setListener(OnReceiveListener<Void> listener) {
			_listener = listener;
		}
	}

	private void broadcast() {
		Intent intent = new Intent();
		intent.setAction(ACTION_SUBSCRIBE);
		mActivity.sendBroadcast(intent);
	}
}
