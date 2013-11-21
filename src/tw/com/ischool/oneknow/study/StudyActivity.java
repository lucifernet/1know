package tw.com.ischool.oneknow.study;

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
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;

public class StudyActivity extends YouTubeBaseActivity implements
		OnItemClickListener {

	public static final String PARAM_KNOW = "knowledge";
	public static final String PARAM_UNIT = "unit";
	public static final String PARAM_SEEK_TO = "seekto";
	public static final String PARAM_TARGET_UNIT_UQID = "unituqid";
	public static final String PARAM_TARGET_TIME = "time";
	public static final int UNIT_STATUS_COMPLETED = 4;
	public static final int UNIT_STATUS_INCOMPLETED = 2;

	public static final int CODE_ADD_NOTE = 1;
	public static final int CODE_EDIT_NOTE = 2;
	private static final int CODE_VIEW_DETAIL = 3;

	private Knowledge mKnowledge;
	private LinearLayout mProgress;
	private TextView mProgressStep;
	private ProgressBar mProgressBar;
	private TextView mTxtProgressStatus;
	private TextView mTxtProgress;
	private ListView mLvUnits;
	private TextView mTxtUnitName;
	private UnitItemHandler mItemHandler;
	private int mSelectedItemIndex;
	private ItemAdapter mAdapter;
	private Button mBtnNext;
	private Button mBtnPrev;
	private ImageButton mBtnFullScreen;
	private CheckBox mChkAutoPlay;
	private CheckBox mChkCompleted;
	private Button mBtnMaterial;
	private Button mBtnNotes;
	private PopupWindow mPopupMaterial;
	private PopupWindow mPopupNotes;
	private WebView mWebDescription;
	private ListView mLvNotes;
	private View mPopNoteView;
	private JSONArray mUnitsJSONArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_study);

		new ActivityHelper(this).valid();
		
		getActionBar().setDisplayShowHomeEnabled(false);

		mProgress = (LinearLayout) this.findViewById(R.id.layoutProgress);
		mProgressStep = (TextView) this.findViewById(R.id.txtProgressStep);
		mTxtProgressStatus = (TextView) this
				.findViewById(R.id.txtProgressStatus);
		mTxtProgress = (TextView) this.findViewById(R.id.txtProgress);
		mProgressBar = (ProgressBar) this.findViewById(R.id.progressBar);
		mLvUnits = (ListView) this.findViewById(R.id.lvUnits);
		mLvUnits.setOnItemClickListener(this);
		mTxtUnitName = (TextView) this.findViewById(R.id.txtUnitName);
		mChkAutoPlay = (CheckBox) this.findViewById(R.id.chkAutoPlay);
		mBtnFullScreen = (ImageButton) this.findViewById(R.id.btnFullScreen);
		mBtnPrev = (Button) this.findViewById(R.id.btnPrev);
		mBtnNext = (Button) this.findViewById(R.id.btnNext);
		mChkCompleted = (CheckBox) this.findViewById(R.id.chkCompleted);
		mBtnMaterial = (Button) this.findViewById(R.id.tabMaterial);
		mBtnNotes = (Button) this.findViewById(R.id.tabNote);

		if (savedInstanceState != null) {
			mKnowledge = (Knowledge) savedInstanceState
					.getSerializable(PARAM_KNOW);
		} else {
			mKnowledge = (Knowledge) getIntent().getExtras().getSerializable(
					PARAM_KNOW);
			setTitle(mKnowledge.getName());
			displayTotalProgress();

			LoadUnitsTask task = new LoadUnitsTask();
			task.setOnReceiveListener(new UnitReceiveListener());
			setProgress(true);
			mProgressStep.setText(R.string.study_progress_load_units);
			task.execute(mKnowledge.getUqid());
		}

		mBtnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				moveToNextUnit();
			}
		});

		mBtnPrev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				moveToPrevUnit();
			}
		});

		mBtnFullScreen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getCurrentHandler().openFullScreen();
			}
		});

		mChkCompleted.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
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
		});

		mBtnMaterial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View button) {
				showPopupMaterial(button);
			}
		});

		mBtnNotes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View button) {
				showPopupNotes(button);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (getCurrentHandler().handleBackPressed()) {
			return;
		}

		super.onBackPressed();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_ADD_NOTE
				&& resultCode == EditNoteActivity.RESULT_OK) {
			if (mPopupNotes != null)
				mPopupNotes.dismiss();
			showPopupNotes(mBtnNotes);
		} else if (requestCode == CODE_EDIT_NOTE
				&& resultCode == EditNoteActivity.RESULT_OK) {
			if (mPopupNotes != null)
				mPopupNotes.dismiss();
			showPopupNotes(mBtnNotes);
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

	private void displayTotalProgress() {
		int totalTime = mKnowledge.getTotalTime();
		int gainTime = mKnowledge.getGainedTime();

		if (totalTime == 0)
			mTxtProgress.setText(R.string.study_default_progress);
		else {
			int p = (gainTime * 100) / totalTime;
			mProgressBar.setMax(totalTime);
			mProgressBar.setProgress(gainTime);
			mTxtProgress.setText(p + "%");

			if (totalTime == gainTime) {
				mTxtProgressStatus.setText(R.string.study_status_completed);
			}
		}

		mProgressStep.setText(R.string.study_progress_load_units);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long arg3) {
		view.setSelected(true);
		onUnitSelected(position);
	}

	private void showPopupNotes(final View button) {
		getCurrentHandler().pause();

		final View unitView = findViewById(R.id.container_unit);

		if (mPopupNotes == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mPopNoteView = layoutInflater.inflate(R.layout.popup_notes, null);
			mLvNotes = (ListView) mPopNoteView.findViewById(R.id.lvNotes);
			mLvNotes.setBackgroundColor(0x00000000);

			Button btnAddNote = (Button) mPopNoteView
					.findViewById(R.id.btnEditNote);
			btnAddNote.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					addNote();
				}
			});

			mLvNotes.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long itemId) {
					NoteViewHolder holder = (NoteViewHolder) view.getTag();
					JSONObject json = holder.getJSONObject();
					int time = JSONUtil.getInt(json, "time");
					getCurrentHandler().seekTo(time);

					mPopupNotes.dismiss();
				}
			});

			mPopupNotes = new PopupWindow(this);
			mPopupNotes.setContentView(mPopNoteView);

			mPopupNotes = new PopupWindow(mPopNoteView,
					unitView.getWidth() * 3 / 4, unitView.getHeight()
							+ findViewById(R.id.layout_unit_top).getHeight());
		}

		mBtnNotes.setEnabled(false);
		mBtnNotes.setText(R.string.study_notes_loading);

		NoteTask task = new NoteTask();
		task.setOnReceiveListener(new OnReceiveListener<JSONArray>() {

			@SuppressWarnings("deprecation")
			@Override
			public void onReceive(JSONArray result) {
				if (!isFinishing()) {
					NoteAdapter adapter = new NoteAdapter(result);
					mLvNotes.setAdapter(adapter);

					mPopupNotes.setBackgroundDrawable(new BitmapDrawable());
					mPopupNotes.setFocusable(true);
					mPopupNotes.setOutsideTouchable(true);

					int height = unitView.getHeight()
							+ findViewById(R.id.layout_unit_top).getHeight();

					mPopupNotes.showAsDropDown(button,
							(0 - unitView.getWidth() / 4),
							(0 - button.getHeight() - height));

					mBtnNotes.setEnabled(true);
					mBtnNotes.setText(R.string.study_notes);
				}
			}

			@Override
			public void onError(Exception e) {
				String msg = getString(R.string.study_load_notes_error);
				msg = String.format(msg, e.getMessage());
				Toast.makeText(StudyActivity.this, msg, Toast.LENGTH_LONG)
						.show();
				mBtnNotes.setEnabled(true);
				mBtnNotes.setText(R.string.study_notes);
			}
		});

		UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);
		JSONObject json = item.getJSON();
		String unitUqid = JSONUtil.getString(json, "uqid");
		task.execute(unitUqid);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	private void showPopupMaterial(View button) {
		if (mPopupMaterial == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.layout.popup_material, null);
			mWebDescription = (WebView) view.findViewById(R.id.webDescription);
			mWebDescription.getSettings().setJavaScriptEnabled(true);
			mWebDescription.getSettings().setDefaultTextEncodingName("UTF-8");
			mWebDescription.setBackgroundColor(0x00000000);

			mPopupMaterial = new PopupWindow(view, mBtnMaterial.getWidth(),
					findViewById(R.id.container_unit).getHeight()
							+ findViewById(R.id.layout_unit_top).getHeight());
		}

		mPopupMaterial.setBackgroundDrawable(new BitmapDrawable());
		mPopupMaterial.setFocusable(true);
		mPopupMaterial.setOutsideTouchable(true);
		UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);
		JSONObject json = item.getJSON();
		String description = JSONUtil.getString(json, "description");

		String html = "<html><head>"
				+ "<style type=\"text/css\">body{color: #dcdcdc; }"
				+ "</style></head><body>" + description + "</body></html>";

		mWebDescription.loadDataWithBaseURL(null, html, "text/html", "UTF-8",
				null);
		mPopupMaterial.showAsDropDown(button, 0,
				(0 - button.getHeight() - mPopupMaterial.getHeight()));

	}

	private void onUnitSelected(int position) {
		if (isFinishing())
			return;

		mAdapter.setSelectedIndex(position);

		UnitItem item = mItemHandler.getItems().get(position);
		mTxtUnitName.setText(item.getName());

		JSONObject json = item.getJSON();
		int status = JSONUtil.getInt(json, "status");
		mChkCompleted.setChecked(status == UNIT_STATUS_COMPLETED);

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
						if (!mChkAutoPlay.isChecked())
							return;

						moveToNextUnit();
					}

					// 當單元進度更新
					@Override
					public void onStudyHistoryUpdated(JSONObject result) {

					}
				});
			}

			getFragmentManager().beginTransaction()
					.replace(R.id.container_unit, fragment)
					.commitAllowingStateLoss();
		}
	}

	private boolean moveToNextUnit() {
		while (mSelectedItemIndex < mItemHandler.getItems().size() - 1) {
			mSelectedItemIndex = mSelectedItemIndex + 1;
			UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);
			if (item.getMode() == UnitItem.MODE_UNIT) {
				onUnitSelected(mSelectedItemIndex);
				return true;
			}
		}
		return false;
	}

	private boolean moveToPrevUnit() {
		while (mSelectedItemIndex > 0) {
			mSelectedItemIndex = mSelectedItemIndex - 1;
			UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);
			if (item.getMode() == UnitItem.MODE_UNIT) {
				onUnitSelected(mSelectedItemIndex);
				return true;
			}
		}
		return false;
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
				R.id.container_unit);

		if (fragment != null && fragment instanceof IUnitPlayerHandler) {
			return (IUnitPlayerHandler) fragment;

		}
		return new NullUnitPlayerHandler();
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
			Toast.makeText(StudyActivity.this, e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

	}

	private void setProgress(boolean progress) {
		mProgress.setVisibility(progress ? View.VISIBLE : View.GONE);
		mBtnFullScreen.setEnabled(!progress);
		mBtnMaterial.setEnabled(!progress);
		mBtnNotes.setEnabled(!progress);
		mBtnNext.setEnabled(!progress);
		mBtnPrev.setEnabled(!progress);
		mChkAutoPlay.setEnabled(!progress);
		mChkCompleted.setEnabled(!progress);
	}

	private class ItemAdapter extends BaseAdapter {
		private LayoutInflater _inflater;

		public ItemAdapter() {
			_inflater = (LayoutInflater) StudyActivity.this
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

			String time = getDisplayTime(StudyActivity.this, item.getTime());

			holder.getTxtTime().setText(time);

			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			UnitItem item = mItemHandler.getItems().get(position);
			return item.getMode() == UnitItem.MODE_UNIT;
		}
	}

	private class NoteAdapter extends BaseAdapter {
		private JSONArray _jsons;
		private LayoutInflater _inflater;

		public NoteAdapter(JSONArray result) {
			_jsons = result;
			_inflater = (LayoutInflater) StudyActivity.this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return _jsons.length();
		}

		@Override
		public Object getItem(int position) {
			try {
				return _jsons.get(position);
			} catch (JSONException e) {
				return null;
			}
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView txtTime, txtNote;
			ImageView imgEdit;
			NoteViewHolder holder;

			if (convertView == null) {
				convertView = _inflater
						.inflate(R.layout.item_study_notes, null);
				txtTime = (TextView) convertView.findViewById(R.id.txtTime);
				txtNote = (TextView) convertView.findViewById(R.id.txtNote);
				imgEdit = (ImageView) convertView.findViewById(R.id.imgEdit);

				holder = new NoteViewHolder();
				holder.setTxtNote(txtNote);
				holder.setTxtTime(txtTime);
				holder.setImgEdit(imgEdit);
				convertView.setTag(holder);
			} else {
				holder = (NoteViewHolder) convertView.getTag();
				txtTime = holder.getTxtTime();
				txtNote = holder.getTxtNote();
				imgEdit = holder.getImgEdit();
			}

			final JSONObject json = (JSONObject) getItem(position);
			final String content = JSONUtil.getString(json, "content");
			final int time = JSONUtil.getInt(json, "time");
			String displayTime = getDisplayTime(StudyActivity.this, time);
			txtTime.setText(displayTime);
			txtNote.setText(content);
			holder.setJSONObject(json);

			imgEdit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					UnitItem item = mItemHandler.getItems().get(
							mSelectedItemIndex);

					Intent intent = new Intent(StudyActivity.this,
							EditNoteActivity.class);
					intent.putExtra(EditNoteActivity.PARAM_CURRENT_TIME,
							(double) time);
					intent.putExtra(EditNoteActivity.PARAM_MODE, CODE_EDIT_NOTE);
					intent.putExtra(EditNoteActivity.PARAM_NOTE, content);
					intent.putExtra(EditNoteActivity.PARAM_TOTAL_TIME,
							item.getTime());
					intent.putExtra(EditNoteActivity.PARAM_NOTE_UQID,
							JSONUtil.getString(json, "uqid"));
					startActivityForResult(intent, CODE_EDIT_NOTE);
				}
			});

			return convertView;
		}

	}

	private void addNote() {
		Intent intent = new Intent(StudyActivity.this, EditNoteActivity.class);
		UnitItem item = mItemHandler.getItems().get(mSelectedItemIndex);
		intent.putExtra(EditNoteActivity.PARAM_UNIT_UQID,
				JSONUtil.getString(item.getJSON(), "uqid"));
		intent.putExtra(EditNoteActivity.PARAM_MODE, CODE_ADD_NOTE);
		intent.putExtra(EditNoteActivity.PARAM_CURRENT_TIME,
				getCurrentHandler().getCurrentTime());
		intent.putExtra(EditNoteActivity.PARAM_TOTAL_TIME, item.getTime());
		startActivityForResult(intent, CODE_ADD_NOTE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(PARAM_KNOW, mKnowledge);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.study, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.add_note) {
			getCurrentHandler().pause();
			addNote();
		} else if (item.getItemId() == R.id.view_detail
				&& mUnitsJSONArray != null) {
			Intent intent = new Intent(this, StudyHistoryActivity.class);
			intent.putExtra(StudyHistoryActivity.PARAM_KNOW_UQID,
					mKnowledge.getUqid());
			intent.putExtra(StudyHistoryActivity.PARAM_UNITS,
					mUnitsJSONArray.toString());
			intent.putExtra(StudyHistoryActivity.PARAM_KNOW, mKnowledge);

			startActivityForResult(intent, CODE_VIEW_DETAIL);
		}

		return super.onOptionsItemSelected(item);
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

	private class NoteViewHolder {
		private TextView _txtTime;
		private TextView _txtNote;
		private JSONObject _json;
		private ImageView _imgEdit;

		public TextView getTxtTime() {
			return _txtTime;
		}

		public void setImgEdit(ImageView imgEdit) {
			_imgEdit = imgEdit;
		}

		public ImageView getImgEdit() {
			return _imgEdit;
		}

		public void setTxtTime(TextView txtTime) {
			_txtTime = txtTime;
		}

		public TextView getTxtNote() {
			return _txtNote;
		}

		public void setTxtNote(TextView txtNote) {
			_txtNote = txtNote;
		}

		public void setJSONObject(JSONObject json) {
			_json = json;
		}

		public JSONObject getJSONObject() {
			return _json;
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

	private class NoteTask extends AsyncTask<String, Void, JSONArray> {
		private Exception _exception;
		private OnReceiveListener<JSONArray> _listener;

		@Override
		protected JSONArray doInBackground(String... arg0) {
			String uqid = arg0[0];
			String serviceURL = String.format(OneKnow.SERVICE_UNIT_NOTES, uqid);
			try {
				return OneKnow.getFrom(serviceURL, null, JSONArray.class);
			} catch (Exception e) {
				_exception = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			if (result != null && _listener != null) {
				_listener.onReceive(result);
			} else if (_exception != null && _listener != null) {
				_listener.onError(_exception);
			}
		}

		public void setOnReceiveListener(OnReceiveListener<JSONArray> listener) {
			_listener = listener;
		}
	}
}
