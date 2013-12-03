package tw.com.ischool.oneknow.item;

import org.json.JSONObject;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.learn.SubscribeFragment;
import tw.com.ischool.oneknow.util.CacheHelper;
import tw.com.ischool.oneknow.util.HttpUtil;
import tw.com.ischool.oneknow.util.JSONUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfilerItem extends FragmentItem {
	private JSONObject mProfile;
	private ImageView mImageView;

	public ProfilerItem() {
		DisplayStatus status = DisplayStatus.combine(DisplayStatus.LOGINED,
				DisplayStatus.GUEST);
		init(R.string.empty, R.drawable.nouser_photo, status,
				SubscribeFragment.class);
	}

	public void setProfile(JSONObject json) {
		mProfile = json;
	}

	public void setItemView(Context context, View convertView) {
		if (mProfile == null)
			return;

		String name = JSONUtil.getString(mProfile, "full_name");
		String uqid = JSONUtil.getString(mProfile, "uqid");

		TextView textView = (TextView) convertView.findViewById(R.id.txtItem);
		textView.setText(name);

		mImageView = (ImageView) convertView.findViewById(R.id.imgItemIcon);

		String filename = uqid + ".png";
		Bitmap bitmap = CacheHelper.loadImage(context, filename);

		if (bitmap == null) {
			String url = JSONUtil.getString(mProfile, "photo");
			DownloadImageTask task = new DownloadImageTask(context, filename);
			task.execute(url);
		} else {
			mImageView.setImageBitmap(bitmap);
		}
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		private Context _context;
		private String _filename;

		public DownloadImageTask(Context context, String filename) {
			_context = context;
			_filename = filename;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String url = params[0];
			HttpUtil http = HttpUtil.createInstanceWithCookie();
			return http.getImage(url);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				CacheHelper.cacheImage(_context, _filename, result);
				mImageView.setImageBitmap(result);
			}
		}
	}
}
