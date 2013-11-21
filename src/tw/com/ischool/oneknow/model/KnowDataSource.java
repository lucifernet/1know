package tw.com.ischool.oneknow.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class KnowDataSource {
	private KnowDBHelper mDBHelper;
	private SQLiteDatabase mDatabase;

	public KnowDataSource(Context context) {
		mDBHelper = new KnowDBHelper(context);
	}

	public void open() {
		mDatabase = mDBHelper.getWritableDatabase();
	}

	public void close() {
		mDatabase.close();
	}

	public void addDiscover(Knowledge knowledge) {
		ContentValues values = new ContentValues();
		values.put(KnowDBHelper.COLUMN_NAME, knowledge.getName());
		values.put(KnowDBHelper.COLUMN_UQID, knowledge.getUqid());
		values.put(KnowDBHelper.COLUMN_LAST_UPDATE, knowledge.getLastUpdate());
		values.put(KnowDBHelper.COLUMN_READER, knowledge.getReader());
		values.put(KnowDBHelper.COLUMN_RATING, knowledge.getRating());
		values.put(KnowDBHelper.COLUMN_IS_PUBLIC, knowledge.isPublic());
		values.put(KnowDBHelper.COLUMN_SUBSCRIBED, knowledge.isSubscribed());
		values.put(KnowDBHelper.COLUMN_LOGO, knowledge.getLogo());
		values.put(KnowDBHelper.COLUMN_PAGE, knowledge.getPage());
		values.put(KnowDBHelper.COLUMN_IS_DISCOVER, 1);
		mDatabase.insert(KnowDBHelper.TABLE_NAME, null, values);
	}

	public boolean hasKnowledge(String uqid) {
		boolean has = false;

		Cursor c = mDatabase.query(KnowDBHelper.TABLE_NAME,
				new String[] { KnowDBHelper.COLUMN_ID }, "uqid=?",
				new String[] { uqid }, null, null, null);
		if (c.moveToNext()) {
			has = true;
		}
		c.close();

		return has;
	}

	public void removeAllDiscover() {
		ContentValues values = new ContentValues();
		values.put(KnowDBHelper.COLUMN_IS_DISCOVER, false);
		mDatabase.update(KnowDBHelper.TABLE_NAME, values,
				KnowDBHelper.COLUMN_ID + ">",
				new String[] { String.valueOf(0) });
	}

	public void updateDiscover(Knowledge knowledge) {
		ContentValues values = new ContentValues();
		values.put(KnowDBHelper.COLUMN_NAME, knowledge.getName());
		values.put(KnowDBHelper.COLUMN_LAST_UPDATE, knowledge.getLastUpdate());
		values.put(KnowDBHelper.COLUMN_READER, knowledge.getReader());
		values.put(KnowDBHelper.COLUMN_RATING, knowledge.getRating());
		values.put(KnowDBHelper.COLUMN_IS_PUBLIC, knowledge.isPublic());
		values.put(KnowDBHelper.COLUMN_SUBSCRIBED, knowledge.isSubscribed());
		values.put(KnowDBHelper.COLUMN_LOGO, knowledge.getLogo());
		values.put(KnowDBHelper.COLUMN_PAGE, knowledge.getPage());
		values.put(KnowDBHelper.COLUMN_IS_DISCOVER, 1);

		mDatabase.update(KnowDBHelper.TABLE_NAME, values,
				KnowDBHelper.COLUMN_UQID + "=?",
				new String[] { knowledge.getUqid() });
	}

	public void setDiscover(Knowledge knowledge) {
		if (this.hasKnowledge(knowledge.getUqid())) {
			this.updateDiscover(knowledge);
		} else {
			this.addDiscover(knowledge);
		}
	}

	public ArrayList<Knowledge> getDiscoverKnowledges() {
		ArrayList<Knowledge> knows = new ArrayList<Knowledge>();

		String[] columns = new String[] { KnowDBHelper.COLUMN_UQID,
				KnowDBHelper.COLUMN_NAME, KnowDBHelper.COLUMN_LAST_UPDATE,
				KnowDBHelper.COLUMN_READER, KnowDBHelper.COLUMN_RATING,
				KnowDBHelper.COLUMN_IS_PUBLIC, KnowDBHelper.COLUMN_SUBSCRIBED,
				KnowDBHelper.COLUMN_LOGO, KnowDBHelper.COLUMN_PAGE,
				KnowDBHelper.COLUMN_IS_DISCOVER };

		String selection = KnowDBHelper.COLUMN_IS_DISCOVER + "=?";
		String[] selectionArgs = new String[] { "1" };
		String orderBy = KnowDBHelper.COLUMN_LAST_UPDATE + " desc";
		Cursor c = mDatabase.query(KnowDBHelper.TABLE_NAME, columns, selection,
				selectionArgs, null, null, orderBy);

		while (c.moveToNext()) {
			Knowledge k = new Knowledge();

			k.setUqid(c.getString(0));
			k.setName(c.getString(1));
			k.setLastUpdate(c.getString(2));
			k.setReader(c.getInt(3));
			k.setRating(c.getInt(4));
			k.setPublic(c.getInt(5) == 1 ? true : false);
			k.setSubscribed(c.getInt(6) == 1 ? true : false);
			k.setLogo(c.getString(7));
			k.setPage(c.getString(8));
			k.setDiscover(c.getInt(9) == 1 ? true : false);
			knows.add(k);
		}

		c.close();
		return knows;
	}

	public ArrayList<Knowledge> getYourKnowledges() {
		ArrayList<Knowledge> knows = new ArrayList<Knowledge>();

		String[] columns = new String[] { KnowDBHelper.COLUMN_UQID,
				KnowDBHelper.COLUMN_NAME, KnowDBHelper.COLUMN_LAST_UPDATE,
				KnowDBHelper.COLUMN_APPROVE_CODE, KnowDBHelper.COLUMN_RATING,
				KnowDBHelper.COLUMN_TOTAL_TIME,
				KnowDBHelper.COLUMN_GAINED_TIME, KnowDBHelper.COLUMN_LOGO,
				KnowDBHelper.COLUMN_PAGE, KnowDBHelper.COLUMN_FINISH_COUNT };

		String selection = KnowDBHelper.COLUMN_SUBSCRIBED + "=?";
		String[] selectionArgs = new String[] { "1" };
		String orderBy = KnowDBHelper.COLUMN_LAST_VIEW_TIME + " desc";
		Cursor c = mDatabase.query(KnowDBHelper.TABLE_NAME, columns, selection,
				selectionArgs, null, null, orderBy);

		while (c.moveToNext()) {
			Knowledge k = new Knowledge();

			k.setUqid(c.getString(0));
			k.setName(c.getString(1));
			k.setLastUpdate(c.getString(2));
			k.setApproveCode(c.getString(3));
			k.setRating(c.getInt(4));
			k.setTotalTime(c.getInt(5));
			k.setGainedTime(c.getInt(6));
			k.setLogo(c.getString(7));
			k.setPage(c.getString(8));
			k.setFinishCount(c.getInt(9));
			k.setSubscribed(true);
			knows.add(k);
		}

		c.close();
		return knows;
	}

	public void setYourKnowledge(Knowledge knowledge) {
		if (this.hasKnowledge(knowledge.getUqid())) {
			this.updateYourKnowledge(knowledge);
		} else {
			this.addYourKnowledge(knowledge);
		}
	}

	public void subscribeKnowledge(Knowledge knowledge, boolean subscribed) {
		ContentValues values = new ContentValues();
		values.put(KnowDBHelper.COLUMN_SUBSCRIBED, subscribed ? 1 : 0);

		mDatabase.update(KnowDBHelper.TABLE_NAME, values,
				KnowDBHelper.COLUMN_UQID + "=?",
				new String[] { knowledge.getUqid() });
	}

	private void addYourKnowledge(Knowledge knowledge) {
		ContentValues values = new ContentValues();
		values.put(KnowDBHelper.COLUMN_NAME, knowledge.getName());
		values.put(KnowDBHelper.COLUMN_UQID, knowledge.getUqid());
		values.put(KnowDBHelper.COLUMN_LAST_UPDATE, knowledge.getLastUpdate());
		values.put(KnowDBHelper.COLUMN_LAST_VIEW_TIME,
				knowledge.getLastViewTime());
		values.put(KnowDBHelper.COLUMN_APPROVE_CODE, knowledge.getApproveCode());
		values.put(KnowDBHelper.COLUMN_RATING, knowledge.getRating());
		values.put(KnowDBHelper.COLUMN_TOTAL_TIME, knowledge.getTotalTime());
		values.put(KnowDBHelper.COLUMN_GAINED_TIME, knowledge.getGainedTime());
		values.put(KnowDBHelper.COLUMN_SUBSCRIBED, 1);
		values.put(KnowDBHelper.COLUMN_FINISH_COUNT, knowledge.getFinishCount());
		values.put(KnowDBHelper.COLUMN_LOGO, knowledge.getLogo());
		values.put(KnowDBHelper.COLUMN_PAGE, knowledge.getPage());
		mDatabase.insert(KnowDBHelper.TABLE_NAME, null, values);
	}

	private void updateYourKnowledge(Knowledge knowledge) {
		ContentValues values = new ContentValues();
		values.put(KnowDBHelper.COLUMN_NAME, knowledge.getName());
		values.put(KnowDBHelper.COLUMN_LAST_UPDATE, knowledge.getLastUpdate());
		values.put(KnowDBHelper.COLUMN_LAST_VIEW_TIME,
				knowledge.getLastViewTime());
		values.put(KnowDBHelper.COLUMN_APPROVE_CODE, knowledge.getApproveCode());
		values.put(KnowDBHelper.COLUMN_RATING, knowledge.getRating());
		values.put(KnowDBHelper.COLUMN_TOTAL_TIME, knowledge.getTotalTime());
		values.put(KnowDBHelper.COLUMN_GAINED_TIME, knowledge.getGainedTime());
		values.put(KnowDBHelper.COLUMN_SUBSCRIBED, 1);
		values.put(KnowDBHelper.COLUMN_FINISH_COUNT, knowledge.getFinishCount());
		values.put(KnowDBHelper.COLUMN_LOGO, knowledge.getLogo());
		values.put(KnowDBHelper.COLUMN_PAGE, knowledge.getPage());

		mDatabase.update(KnowDBHelper.TABLE_NAME, values,
				KnowDBHelper.COLUMN_UQID + "=?",
				new String[] { knowledge.getUqid() });

	}
}
