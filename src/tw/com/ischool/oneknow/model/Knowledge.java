package tw.com.ischool.oneknow.model;

import java.io.File;
import java.io.Serializable;

import org.json.JSONObject;

import tw.com.ischool.oneknow.model.parser.KnowledgeParser;
import tw.com.ischool.oneknow.util.StringUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Knowledge implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int mId;
	private String mName;
	private String mUqid;
	private String mLastUpdate;
	private int mReader;
	private int mRating;
	private boolean mPublic;
	private boolean mSubscribed;
	private String mLogo;
	private String mPage;
	private boolean mDiscover;
	private boolean mEditorChoice;
	// private Bitmap mLogoBitmap;
	private int mTotalTime;
	private int mGainedTime;
	private int mFinishCount;
	private String mLastViewTime;
	private String mApproveCode;

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getUqid() {
		return mUqid;
	}

	public void setUqid(String uqid) {
		mUqid = uqid;
	}

	public String getLastUpdate() {
		return mLastUpdate;
	}

	public void setLastUpdate(String lastUpdate) {
		mLastUpdate = lastUpdate;
	}

	public int getReader() {
		return mReader;
	}

	public void setReader(int reader) {
		mReader = reader;
	}

	public int getRating() {
		return mRating;
	}

	public void setRating(int rating) {
		mRating = rating;
	}

	public boolean isPublic() {
		return mPublic;
	}

	public void setPublic(boolean _public) {
		mPublic = _public;
	}

	public boolean isSubscribed() {
		return mSubscribed;
	}

	public void setSubscribed(boolean subscribed) {
		mSubscribed = subscribed;
	}

	public String getLogo() {
		// 在學習裡傳回來的網址是舊的, 這裡先用程式組合網址, 之後再看情況調整
		return mLogo;

		// return "http://1know.net/images/know_" + mUqid + ".png";
	}

	public void setLogo(String logo) {
		mLogo = logo;
	}

	public String getPage() {
		return mPage;
	}

	public void setPage(String page) {
		mPage = page;
	}

	public boolean isDiscover() {
		return mDiscover;
	}

	public void setDiscover(boolean discover) {
		mDiscover = discover;
	}

	public boolean isEditorChoice() {
		return mEditorChoice;
	}

	public void setEditorChoice(boolean editorChoice) {
		mEditorChoice = editorChoice;
	}

	public String getLogoFileName() {
		if (StringUtil.isNullOrWhitespace(mLogo))
			return StringUtil.EMPTY;

		String[] splits = mLogo.split("/");
		return splits[splits.length - 1];
	}

	public Bitmap getCachedLogoBitmap(Context context) {
		return Knowledge.loadLogoImage(context, this);
	}

	// public void setLogoBitmap(Bitmap logoBitmap) {
	// mLogoBitmap = logoBitmap;
	// }

	public int getTotalTime() {
		return mTotalTime;
	}

	public void setTotalTime(int totalTime) {
		mTotalTime = totalTime;
	}

	public int getGainedTime() {
		return mGainedTime;
	}

	public void setGainedTime(int gainedTime) {
		mGainedTime = gainedTime;
	}

	public int getFinishCount() {
		return mFinishCount;
	}

	public void setFinishCount(int finishCount) {
		mFinishCount = finishCount;
	}

	public String getLastViewTime() {
		return mLastViewTime;
	}

	public void setLastViewTime(String lastViewTime) {
		mLastViewTime = lastViewTime;
	}

	public static <T extends KnowledgeParser> Knowledge parseJSON(
			JSONObject json, Class<T> type) {
		try {
			T instance = type.newInstance();
			return instance.parse(json);
		} catch (Exception e) {

		}
		return null;
	}

	public static Bitmap loadLogoImage(Context context, Knowledge knowledge) {
		File dir = context.getExternalCacheDir();
		File imgDir = new File(dir, "images");
		File file = new File(imgDir, knowledge.getLogoFileName());

		if (file.exists()) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),
					options);
			// knowledge.setLogoBitmap(bitmap);
			return bitmap;
		}
		// return knowledge.getLogoBitmap();
		return null;
	}

	public String getApproveCode() {
		return mApproveCode;
	}

	public void setApproveCode(String approveCode) {
		mApproveCode = approveCode;
	}
}
