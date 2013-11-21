package tw.com.ischool.oneknow.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KnowDBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "oneknow.db";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_NAME = "knowledge";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_UQID = "uqid";
	public static final String COLUMN_LAST_UPDATE = "last_upate";
	public static final String COLUMN_READER = "reader";
	public static final String COLUMN_RATING = "rating";
	public static final String COLUMN_IS_PUBLIC = "is_public";
	public static final String COLUMN_SUBSCRIBED = "subscribed";
	public static final String COLUMN_LOGO = "logo";
	public static final String COLUMN_PAGE = "page";
	public static final String COLUMN_IS_DISCOVER = "is_discover";
	public static final String COLUMN_IS_COHICE = "is_editor_choice";
	
	//以下欄位是 YourKnowledge 用的	
	public static final String COLUMN_APPROVE_CODE = "approve_code";
	public static final String COLUMN_TOTAL_TIME = "total_time";
	public static final String COLUMN_GAINED_TIME = "gained_time";
	public static final String COLUMN_FINISH_COUNT = "finish_count";
	public static final String COLUMN_LAST_VIEW_TIME = "last_view_time";
	
	
	public KnowDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder("create table " + TABLE_NAME
				+ "(" + COLUMN_ID + " integer primary key autoincrement, ");
		sql.append(COLUMN_UQID + " text not null,");
		sql.append(COLUMN_NAME + " text not null,");
		sql.append(COLUMN_LAST_UPDATE + " text,");
		sql.append(COLUMN_READER + " integer,");
		sql.append(COLUMN_RATING + " integer,");
		sql.append(COLUMN_IS_PUBLIC + " integer,");
		sql.append(COLUMN_SUBSCRIBED + " integer,");
		sql.append(COLUMN_LOGO + " text,");
		sql.append(COLUMN_PAGE + " text,");
		sql.append(COLUMN_IS_DISCOVER + " integer,");
		sql.append(COLUMN_IS_COHICE + " integer,");
		sql.append(COLUMN_APPROVE_CODE + " text,");
		sql.append(COLUMN_TOTAL_TIME + " integer,");
		sql.append(COLUMN_GAINED_TIME + " integer,");
		sql.append(COLUMN_FINISH_COUNT + " integer,");
		sql.append(COLUMN_LAST_VIEW_TIME + " text");
		sql.append(");");

		db.execSQL(sql.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}	
}
