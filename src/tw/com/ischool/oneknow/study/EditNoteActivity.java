package tw.com.ischool.oneknow.study;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class EditNoteActivity extends Activity {

	protected static final String PARAM_TOTAL_TIME = "TotalTime";
	public static String PARAM_MODE = "Mode";
	public static String PARAM_CURRENT_TIME = "CurrentTime";
	public static String PARAM_NOTE = "Note";
	public static String PARAM_UNIT_UQID = "UniqUqid";
	public static String PARAM_NOTE_UQID = "NoteID";
	public static int RESULT_OK = 1;
	public static int RESULT_CANCEL = 2;

	private String mUnitUqid;
	private EditText mEditNote;
	private SeekBar mSeekTime;
	private TextView mTxtTime;
	// private Button mBtnRemove;
	// private Button mBtnSave;
	private String mNoteID;
	private int mMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_note);

		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);

		mEditNote = (EditText) this.findViewById(R.id.editNote);
		mSeekTime = (SeekBar) this.findViewById(R.id.seekTime);
		mTxtTime = (TextView) this.findViewById(R.id.txtTimeString);
		// mBtnSave = (Button) this.findViewById(R.id.btnSave);
		// mBtnRemove = (Button) this.findViewById(R.id.btnDelete);

		Bundle bundle = this.getIntent().getExtras();
		int totalTime = bundle.getInt(PARAM_TOTAL_TIME);
		double currentTime = bundle.getDouble(PARAM_CURRENT_TIME);

		mMode = bundle.getInt(PARAM_MODE);
		if (mMode == UnitStudyActivity.CODE_EDIT_NOTE) {
			String note = bundle.getString(PARAM_NOTE);
			mEditNote.setText(note);
			mNoteID = bundle.getString(PARAM_NOTE_UQID);
			getActionBar().setTitle(R.string.study_edit_note);
		} else {
			// mBtnRemove.setVisibility(View.GONE);
			mUnitUqid = bundle.getString(PARAM_UNIT_UQID);
			getActionBar().setTitle(R.string.study_add_notes);
		}

		mSeekTime.setMax(totalTime);
		mSeekTime.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onProgressChanged(SeekBar bar, int progress,
					boolean fromUser) {
				setTimeString(progress);
			}
		});
		if (currentTime == 0)
			setTimeString(0);

		mSeekTime.setProgress((int) currentTime);
		mEditNote.requestFocus();

		// mBtnSave.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// if (mode == StudyActivity.CODE_ADD_NOTE) {
		// AddTask task = new AddTask();
		// task.setOnReceiveListener(mReceiveListener);
		// task.execute();
		// } else {
		// UpdateTask task = new UpdateTask();
		// task.setOnReceiveListener(mReceiveListener);
		// task.execute();
		// }
		// }
		// });

		// mBtnRemove.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// new AlertDialog.Builder(EditNoteActivity.this)
		// .setIcon(android.R.drawable.ic_dialog_alert)
		// .setTitle(R.string.confirm)
		// .setMessage(R.string.study_note_del_confirm)
		// .setPositiveButton(R.string.yes,
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		// RemoveTask task = new RemoveTask();
		// task.setOnReceiveListener(mReceiveListener);
		// task.execute();
		// }
		//
		// }).setNegativeButton(R.string.no, null).show();
		// }
		// });
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem itemRemove = menu.findItem(R.id.action_remove);
		if (mMode == UnitStudyActivity.CODE_ADD_NOTE)
			itemRemove.setVisible(false);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit_note, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.setResult(RESULT_CANCEL);
			finish();
		} else if (item.getItemId() == R.id.action_save) {
			if (mMode == StudyActivity.CODE_ADD_NOTE) {
				AddTask task = new AddTask();
				task.setOnReceiveListener(mReceiveListener);
				task.execute();
			} else {
				UpdateTask task = new UpdateTask();
				task.setOnReceiveListener(mReceiveListener);
				task.execute();
			}
		} else if (item.getItemId() == R.id.action_remove) {
			new AlertDialog.Builder(EditNoteActivity.this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.confirm)
					.setMessage(R.string.study_note_del_confirm)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									RemoveTask task = new RemoveTask();
									task.setOnReceiveListener(mReceiveListener);
									task.execute();
								}

							}).setNegativeButton(R.string.no, null).show();
		}

		return true;
	}

	private void setTimeString(int progress) {
		String time = StudyActivity.getDisplayTime(EditNoteActivity.this,
				progress);
		mTxtTime.setText(time);
	}

	private OnReceiveListener<Void> mReceiveListener = new OnReceiveListener<Void>() {

		@Override
		public void onReceive(Void result) {
			setResult(RESULT_OK);
			finish();
		}

		@Override
		public void onError(Exception e) {
			String err = getString(R.string.study_save_notes_error);
			err = String.format(err, e.getMessage());
			Toast.makeText(EditNoteActivity.this, err, Toast.LENGTH_LONG)
					.show();
		}
	};

	private class AddTask extends AsyncTask<Void, Void, Void> {
		private Exception _exception;
		private OnReceiveListener<Void> _listener;

		@Override
		protected Void doInBackground(Void... params) {
			String value = mEditNote.getText().toString();
			int time = mSeekTime.getProgress();

			JSONObject json = new JSONObject();
			try {
				json.put("time", time);
				json.put("content", value);
			} catch (JSONException e) {

			}

			String serviceURL = String.format(OneKnow.SERVICE_UNIT_NOTES,
					mUnitUqid);
			try {
				OneKnow.postTo(serviceURL, json, Void.class);
			} catch (Exception e) {
				_exception = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (_listener == null)
				return;

			if (_exception != null)
				_listener.onError(_exception);
			else
				_listener.onReceive(result);
		}

		public void setOnReceiveListener(OnReceiveListener<Void> listener) {
			_listener = listener;
		}
	}

	private class UpdateTask extends AsyncTask<Void, Void, Void> {

		private Exception _exception;
		private OnReceiveListener<Void> _listener;

		@Override
		protected Void doInBackground(Void... params) {
			String value = mEditNote.getText().toString();
			int time = mSeekTime.getProgress();

			JSONObject json = new JSONObject();
			try {
				json.put("time", time);
				json.put("content", value);
			} catch (JSONException e) {

			}

			String serviceURL = String.format(OneKnow.SERVICE_NOTE_UPDATE,
					mNoteID);
			try {
				OneKnow.putTo(serviceURL, json, Void.class);
			} catch (Exception e) {
				_exception = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (_listener == null)
				return;

			if (_exception != null)
				_listener.onError(_exception);
			else
				_listener.onReceive(result);
		}

		public void setOnReceiveListener(OnReceiveListener<Void> listener) {
			_listener = listener;
		}

	}

	private class RemoveTask extends AsyncTask<Void, Void, Void> {

		private Exception _exception;
		private OnReceiveListener<Void> _listener;

		@Override
		protected Void doInBackground(Void... params) {

			String serviceURL = String.format(OneKnow.SERVICE_NOTE_UPDATE,
					mNoteID);
			try {
				OneKnow.delete(serviceURL);
			} catch (Exception e) {
				_exception = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (_listener == null)
				return;

			if (_exception != null)
				_listener.onError(_exception);
			else
				_listener.onReceive(result);
		}

		public void setOnReceiveListener(OnReceiveListener<Void> listener) {
			_listener = listener;
		}

	}
}
