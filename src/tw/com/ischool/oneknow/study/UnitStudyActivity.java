package tw.com.ischool.oneknow.study;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.main.MainActivity;
import tw.com.ischool.oneknow.model.Knowledge;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import tw.com.ischool.oneknow.util.ActivityHelper;
import tw.com.ischool.oneknow.util.JSONUtil;
import tw.com.ischool.oneknow.util.StringUtil;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;

public class UnitStudyActivity extends YouTubeBaseActivity {

	public static final String PARAM_KNOW = "knowledge";
	public static final String PARAM_UNIT = "unit";
	public static final String PARAM_SEEK_TO = "seekto";
	public static final String PARAM_TARGET_UNIT_UQID = "unituqid";
	public static final String PARAM_TARGET_TIME = "time";

	public static final int CODE_ADD_NOTE = 1;
	public static final int CODE_EDIT_NOTE = 2;
	private static final int CODE_VIEW_DETAIL = 3;

	public static final int UNIT_STATUS_COMPLETED = 4;
	public static final int UNIT_STATUS_INCOMPLETED = 2;

	private UnitItemHandler mItemHandler;
	private int mSelectedItemIndex;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private ListView mLvUnits;
	private ListView mLvFunction;
	private Knowledge mKnowledge;
	private JSONArray mUnitsJSONArray;
	private ItemAdapter mAdapter;
	private LinearLayout mProgress;
	private ArrayList<Integer> mFuncIcons;
	private FuncAdapter mFuncAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unit_study);

		new ActivityHelper(this).valid();

		mProgress = (LinearLayout) findViewById(R.id.progress);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			/**
			 * Called when a drawer has settled in a completely closed state.
			 */
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(getTitle());
				invalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(getTitle());
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mLvUnits = (ListView) findViewById(R.id.left_drawer);
		mLvUnits.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long viewid) {
				view.setSelected(true);
				onUnitSelected(position);
				mDrawerLayout.closeDrawer(mLvUnits);
			}
		});

		mLvFunction = (ListView) findViewById(R.id.right_drawer);
		mFuncAdapter = new FuncAdapter();
		mLvFunction.setAdapter(mFuncAdapter);
		mLvFunction.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long viewid) {
				// TODO Auto-generated method stub
				int imageId = mFuncIcons.get(position);

				if (imageId == R.drawable.ic_actualsize) {
					fullscreen(true);
				} else if (imageId == R.drawable.ic_note) {
					addNote();
				} else if (imageId == R.drawable.ic_info) {
					viewMaterial();
				} else if (imageId == R.drawable.ic_left) {
					moveToPrevUnit();
				} else if (imageId == R.drawable.ic_right) {
					moveToNextUnit();
				}

				mDrawerLayout.closeDrawer(mLvFunction);
			}
		});

		mKnowledge = (Knowledge) getIntent().getExtras().getSerializable(
				PARAM_KNOW);
		setTitle(mKnowledge.getName());
		Drawable icon = new BitmapDrawable(getResources(),
				mKnowledge.getCachedLogoBitmap(this));
		getActionBar().setIcon(icon);

		LoadUnitsTask task = new LoadUnitsTask();
		task.setOnReceiveListener(new UnitReceiveListener());
		setProgress(true);
		task.execute(mKnowledge.getUqid());

		fullscreen(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.unit_study, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mItemHandler != null && mSelectedItemIndex > -1) {
			MenuItem menuItem = menu.findItem(R.id.action_completed);
			UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);

			JSONObject json = item.getJSON();
			int status = JSONUtil.getInt(json, "status");
			menuItem.setChecked(status == UNIT_STATUS_COMPLETED);

			int index = getPreviousIndex();
			if (index == -1)
				menu.removeItem(R.id.action_previous);

			index = getNextIndex();
			if (index == -1)
				menu.removeItem(R.id.action_next);

		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		int id = item.getItemId();

		if (id == R.id.action_fullscreen) {
			fullscreen(false);
		} else if (id == R.id.action_previous) {
			moveToPrevUnit();
		} else if (id == R.id.action_next) {
			moveToNextUnit();
		} else if (id == R.id.action_view_detail) {
			viewStudyHistory();
		} else if (id == R.id.action_add_notes) {
			addNote();
		} else if (id == R.id.action_material) {
			viewMaterial();
		} else if (id == R.id.action_completed) {
			boolean checked = !item.isChecked();
			item.setChecked(checked);
			completeUnit(checked);
		}

		return true;
	}

	@Override
	public void onBackPressed() {
		if (!getActionBar().isShowing())
			fullscreen(true);
		else
			super.onBackPressed();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_ADD_NOTE
				&& resultCode == EditNoteActivity.RESULT_OK) {
//			if (mPopupNotes != null)
//				mPopupNotes.dismiss();
//			showPopupNotes(mBtnNotes);
		} else if (requestCode == CODE_EDIT_NOTE
				&& resultCode == EditNoteActivity.RESULT_OK) {
//			if (mPopupNotes != null)
//				mPopupNotes.dismiss();
//			showPopupNotes(mBtnNotes);
		} else if (requestCode == CODE_VIEW_DETAIL
				&& resultCode == StudyHistoryActivity.RESULT_SEEK_TO_UNIT) {
			// TODO
			UnitItem item = (UnitItem) data
					.getSerializableExtra(StudyHistoryActivity.SEEK_TO_UNIT);

			for (int i = 0; i < mItemHandler.getItems().size(); i++) {
				UnitItem ui = mItemHandler.getItems().get(i);
				if (ui.equals(item)) {
					onUnitSelected(i);
					break;
				}
			}

		}
	};

	@SuppressLint("InlinedApi")
	private void fullscreen(boolean show) {
		if (show) {
			getActionBar().show();
			if (Build.VERSION.SDK_INT < 16) {
				WindowManager.LayoutParams attrs = getWindow().getAttributes();
				attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
				getWindow().setAttributes(attrs);
			} else {
				View decorView = getWindow().getDecorView();
				int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
				decorView.setSystemUiVisibility(uiOptions);
			}
			mDrawerLayout.setDrawerLockMode(
					DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mLvFunction);
			mDrawerLayout.setTop(getActionBar().getHeight());
		} else {
			getActionBar().hide();
			if (Build.VERSION.SDK_INT < 16) {
				WindowManager.LayoutParams attrs = getWindow().getAttributes();
				attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				getWindow().setAttributes(attrs);
			} else {
				View decorView = getWindow().getDecorView();
				int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
				decorView.setSystemUiVisibility(uiOptions);
			}
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
					mLvFunction);

		}
	}

	private void setProgress(boolean progress) {
		mProgress.setVisibility(progress ? View.VISIBLE : View.GONE);
	}

	private boolean moveToNextUnit() {
		// while (mSelectedItemIndex < mItemHandler.getItems().size() - 1) {
		// mSelectedItemIndex = mSelectedItemIndex + 1;
		// UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);
		// if (item.getMode() == UnitItem.MODE_UNIT) {
		// onUnitSelected(mSelectedItemIndex);
		// return true;
		// }
		// }
		// return false;

		int index = getNextIndex();
		if (index > -1) {
			onUnitSelected(index);
			return true;
		}
		return false;
	}

	private int getNextIndex() {
		int index = mSelectedItemIndex;
		while (index < mItemHandler.getItems().size() - 1) {
			index = index + 1;
			UnitItem item = mItemHandler.getItems().get(index);
			if (item.getMode() == UnitItem.MODE_UNIT) {
				return index;
			}
		}
		return -1;
	}

	private boolean moveToPrevUnit() {
		// while (mSelectedItemIndex > 0) {
		// mSelectedItemIndex = mSelectedItemIndex - 1;
		// UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);
		// if (item.getMode() == UnitItem.MODE_UNIT) {
		// onUnitSelected(mSelectedItemIndex);
		// return true;
		// }
		// }
		// return false;

		int index = getPreviousIndex();
		if (index > -1) {
			onUnitSelected(index);
			return true;
		}
		return false;
	}

	private int getPreviousIndex() {
		int index = mSelectedItemIndex;
		while (index > 0) {
			index = index - 1;
			UnitItem item = mItemHandler.getItems().get(index);
			if (item.getMode() == UnitItem.MODE_UNIT) {
				return index;
			}
		}
		return -1;
	}

	private void viewStudyHistory() {
		Intent intent = new Intent(this, StudyHistoryActivity.class);
		intent.putExtra(StudyHistoryActivity.PARAM_KNOW_UQID,
				mKnowledge.getUqid());
		intent.putExtra(StudyHistoryActivity.PARAM_UNITS,
				mUnitsJSONArray.toString());
		intent.putExtra(StudyHistoryActivity.PARAM_KNOW, mKnowledge);

		startActivityForResult(intent, CODE_VIEW_DETAIL);
	}

	private void completeUnit(boolean checked) {
		UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);
		JSONObject json = item.getJSON();
		String unitUqid = JSONUtil.getString(json, "uqid");
		try {
			json.put("status", checked ? UNIT_STATUS_COMPLETED
					: UNIT_STATUS_INCOMPLETED);
		} catch (JSONException e) {

		}

		UnitStatusTask task = new UnitStatusTask(unitUqid, checked);
		task.execute();
	}

	private void addNote() {
		Intent intent = new Intent(this, EditNoteActivity.class);
		UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);
		intent.putExtra(EditNoteActivity.PARAM_UNIT_UQID,
				JSONUtil.getString(item.getJSON(), "uqid"));
		intent.putExtra(EditNoteActivity.PARAM_MODE, CODE_ADD_NOTE);
		intent.putExtra(EditNoteActivity.PARAM_CURRENT_TIME,
				getCurrentHandler().getCurrentTime());
		intent.putExtra(EditNoteActivity.PARAM_TOTAL_TIME, item.getTime());
		startActivityForResult(intent, CODE_ADD_NOTE);
	}

	private void viewMaterial() {
		// TODO viewMaterial
	}

	private void onUnitSelected(int position) {
		if (isFinishing())
			return;

		mAdapter.setSelectedIndex(position);

		UnitItem item = mItemHandler.getItems().get(position);
		getActionBar().setTitle(item.getName());

		JSONObject json = item.getJSON();

		getCurrentHandler().beforeDestory();
		Fragment fragment = UnitPlayerFactory.createInstance(json);
		if (fragment != null) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(PARAM_UNIT, item);

			int time = getIntent().getIntExtra(PARAM_TARGET_TIME, 0);
			bundle.putInt(PARAM_SEEK_TO, time);
			fragment.setArguments(bundle);
		}

		if (fragment != null) {
			if (fragment instanceof IUnitPlayerHandler) {
				IUnitPlayerHandler callback = (IUnitPlayerHandler) fragment;
				callback.setUnitEventListener(new OnUnitEventListener() {
					// 當單元播放完成
					@Override
					public void onCompleted() {
						// if (!mChkAutoPlay.isChecked())
						// return;
						// 這邊看要不要給提示選擇自動播放
						moveToNextUnit();
					}

					// 當單元進度更新
					@Override
					public void onStudyHistoryUpdated(JSONObject result) {

					}
				});
			}

			invalidateOptionsMenu();

			invalidFunctionMenu();

			getFragmentManager().beginTransaction()
					.replace(R.id.container, fragment)
					.commitAllowingStateLoss();
		}
	}

	private void invalidFunctionMenu() {
		mFuncIcons = new ArrayList<Integer>();
		mFuncIcons.add(R.drawable.ic_actualsize);
		mFuncIcons.add(R.drawable.ic_note);
		mFuncIcons.add(R.drawable.ic_info);

		int index = getPreviousIndex();
		if (index != -1)
			mFuncIcons.add(R.drawable.ic_left);

		index = getNextIndex();
		if (index != -1)
			mFuncIcons.add(R.drawable.ic_right);

		mFuncAdapter.notifyDataSetChanged();
	}

	public static String getDisplayTime(Context context, int seconds) {
		String time = StringUtil.EMPTY;
		int totalSecond = seconds;
		int hour = totalSecond / 3600;
		if (hour > 0) {
			time = String.format(context.getString(R.string.study_time_hour),
					String.valueOf(hour));
		}

		totalSecond = totalSecond % 3600;
		int minute = totalSecond / 60;
		if (minute > 0 || hour > 0) {
			time = time
					+ StringUtil.WHITESPACE
					+ String.format(
							context.getString(R.string.study_time_minute),
							String.valueOf(minute));
		}

		totalSecond = totalSecond % 60;
		if (totalSecond >= 0) {
			time = time
					+ StringUtil.WHITESPACE
					+ String.format(
							context.getString(R.string.study_time_second),
							totalSecond);
		}
		return time;
	}

	private IUnitPlayerHandler getCurrentHandler() {
		Fragment fragment = getFragmentManager().findFragmentById(
				R.id.container);

		if (fragment != null && fragment instanceof IUnitPlayerHandler) {
			return (IUnitPlayerHandler) fragment;

		}
		return new NullUnitPlayerHandler();
	}

	private class ItemAdapter extends BaseAdapter {
		private LayoutInflater _inflater;

		public ItemAdapter() {
			_inflater = (LayoutInflater) UnitStudyActivity.this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mItemHandler.getItems().size();
		}

		@Override
		public Object getItem(int index) {
			return mItemHandler.getItems().get(index);
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		public void setSelectedIndex(int index) {
			mSelectedItemIndex = index;
			notifyDataSetChanged();
		}

		@Override
		public View getView(int index, View convertView, ViewGroup parent) {

			UnitViewHolder holder;

			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.item_unit, parent,
						false);

				TextView txtName = (TextView) convertView
						.findViewById(R.id.txtName);
				TextView txtTime = (TextView) convertView
						.findViewById(R.id.txtTime);

				holder = new UnitViewHolder();
				holder.setTxtName(txtName);
				holder.setTxtTime(txtTime);

				convertView.setTag(holder);
			} else {
				holder = (UnitViewHolder) convertView.getTag();
			}

			UnitItem item = mItemHandler.getItems().get(index);
			if (item.getMode() == UnitItem.MODE_CHAPTER) {
				int color = getResources().getColor(R.color.unit_chapter);
				convertView.setBackgroundColor(color);
			} else if (mSelectedItemIndex != -1 && index == mSelectedItemIndex) {
				int color = getResources().getColor(R.color.unit_selected);
				convertView.setBackgroundColor(color);

			} else {
				convertView
						.setBackgroundResource(R.drawable.list_item_selector);
			}

			holder.getTxtName().setText(item.getName());

			String time = getDisplayTime(UnitStudyActivity.this, item.getTime());

			holder.getTxtTime().setText(time);

			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			UnitItem item = mItemHandler.getItems().get(position);
			return item.getMode() == UnitItem.MODE_UNIT;
		}
	}

	private class UnitViewHolder {
		private TextView _txtName;
		private TextView _txtTime;

		public TextView getTxtName() {
			return _txtName;
		}

		public void setTxtName(TextView txtName) {
			_txtName = txtName;
		}

		public TextView getTxtTime() {
			return _txtTime;
		}

		public void setTxtTime(TextView txtTime) {
			_txtTime = txtTime;
		}
	}

	private class UnitReceiveListener implements OnReceiveListener<JSONArray> {

		@Override
		public void onReceive(JSONArray result) {
			setProgress(false);

			mUnitsJSONArray = result;
			mSelectedItemIndex = -1;
			mItemHandler = new UnitItemHandler();
			for (int i = 0; i < result.length(); i++) {
				try {
					JSONObject unitObject = result.getJSONObject(i);
					mItemHandler.putUnit(unitObject);
				} catch (JSONException e) {
				}

				if (mSelectedItemIndex > -1)
					continue;

				UnitItem item = mItemHandler.getItems().get(i);
				if (item.getMode() == UnitItem.MODE_UNIT)
					mSelectedItemIndex = i;
			}

			mAdapter = new ItemAdapter();

			mLvUnits.setAdapter(mAdapter);

			// 如果有指定單元的話轉到指定單元
			String unitUqid = getIntent()
					.getStringExtra(PARAM_TARGET_UNIT_UQID);
			if (!StringUtil.isNullOrWhitespace(unitUqid)) {
				int index = mItemHandler.findUnitIndex(unitUqid);
				if (index != -1) {
					mSelectedItemIndex = index;
				}
			}

			// TODO 如果有已選項目的話
			if (mSelectedItemIndex != -1) {
				onUnitSelected(mSelectedItemIndex);
			}
		}

		@Override
		public void onError(Exception e) {
			setProgress(false);
			Toast.makeText(UnitStudyActivity.this, e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

	}

	private class UnitStatusTask extends AsyncTask<String, Void, Void> {
		private boolean _checked;
		private String _uqid;

		public UnitStatusTask(String unitUqid, boolean checked) {
			_checked = checked;
			_uqid = unitUqid;
		}

		@Override
		protected Void doInBackground(String... params) {
			String serviceURL = String.format(OneKnow.SERVICE_UNIT_STATUS,
					_uqid);
			JSONObject json = new JSONObject();
			try {
				json.put("uqid", _uqid);
				json.put("status", _checked ? UNIT_STATUS_COMPLETED
						: UNIT_STATUS_INCOMPLETED);
				OneKnow.putTo(serviceURL, json, Void.class);
			} catch (Exception ex) {
				Log.e(MainActivity.TAG, ex.getMessage());
			}
			return null;
		}

	}

	private class FuncAdapter extends BaseAdapter {
		private LayoutInflater _inflater;

		public FuncAdapter() {
			if (mFuncIcons == null)
				mFuncIcons = new ArrayList<Integer>();

			_inflater = (LayoutInflater) UnitStudyActivity.this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mFuncIcons.size();
		}

		@Override
		public Object getItem(int position) {
			return mFuncIcons.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.item_study_func, null);
			}
			int imageId = mFuncIcons.get(position);
			ImageView img = (ImageView) convertView.findViewById(R.id.icon);
			img.setImageResource(imageId);
			return convertView;
		}

	}
}
