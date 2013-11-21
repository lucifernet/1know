package tw.com.ischool.oneknow.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.main.MainActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class KnowImageTask extends AsyncTask<Void, Integer, Bitmap> {

	private Knowledge mKnowledge;
	private Context mContext;

	private OnImageProgresListener mProgressListener;
	private OnImageCompleteListener mCompleteListener;

	public KnowImageTask(Context context, Knowledge knowledge) {
		mKnowledge = knowledge;
		mContext = context;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		this.publishProgress(0);
		File dir = mContext.getExternalCacheDir();
		File imgDir = new File(dir, "images");
		File file = new File(imgDir, mKnowledge.getLogoFileName());

		imgDir.mkdirs();

		if (!file.exists()) {

			URLConnection conexion;
			InputStream input = null;
			OutputStream output = null;

			int count;

			try {
				URL url = new URL(mKnowledge.getLogo());
				conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();
				input = new BufferedInputStream(url.openStream());
				output = new FileOutputStream(file);

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress((int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();

			} catch (Exception e) {
				Log.w(MainActivity.TAG, e.getMessage());
				return BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.ic_launcher);
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (Exception ex) {
				}
			}
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		this.publishProgress(100);
		return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (mProgressListener != null)
			mProgressListener.onProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Bitmap result) {
//		mKnowledge.setLogoBitmap(result);
		if (mCompleteListener != null)
			mCompleteListener.onImageComplete(result);
	}

	public void setOnImageProgressListener(OnImageProgresListener listener) {
		mProgressListener = listener;
	}

	public void setOnImageCompleteListener(OnImageCompleteListener listener) {
		mCompleteListener = listener;
	}

	public interface OnImageProgresListener {
		void onProgress(int progress);
	}

	public interface OnImageCompleteListener {
		void onImageComplete(Bitmap bitmap);
	}
}
