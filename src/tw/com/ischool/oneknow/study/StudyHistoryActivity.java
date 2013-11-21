package tw.com.ischool.oneknow.study;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.ischool.oneknow.OneKnow;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.main.MainActivity;
import tw.com.ischool.oneknow.model.KnowDataSource;
import tw.com.ischool.oneknow.model.Knowledge;
import tw.com.ischool.oneknow.model.OnReceiveListener;
import tw.com.ischool.oneknow.util.JSONUtil;
import tw.com.ischool.oneknow.util.StringUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class StudyHistoryActivity extends Activity {

	public static final String PARAM_KNOW_UQID = "KnowUqid";
	public static final String PARAM_UNITS = "units";
	public static final String PARAM_KNOW = "knowledge";
	public static final int RESULT_SEEK_TO_UNIT = 1;
	public static final int RESULT_START_LEARNING = 2;
	public static final String SEEK_TO_UNIT = "seekTo";

	private UnitItemHandler mItemHandler;
	private Knowledge mKnowledge;
	private ListView mLvUnits;
	private LinearLayout mLayout;
	private Button mBtnToday;
	private Button mBtnWeek;
	private Button mBtnMonth;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_study_history);

		mLvUnits = (ListView) this.findViewById(R.id.lvUnits);
		mLayout = (LinearLayout) findViewById(R.id.container_chart);
		mBtnToday = (Button) findViewById(R.id.btnToday);
		mBtnWeek = (Button) findViewById(R.id.btnWeek);
		mBtnMonth = (Button) findViewById(R.id.btnMonth);

		mBtnToday.setOnClickListener(mOnClickListener);
		mBtnWeek.setOnClickListener(mOnClickListener);
		mBtnMonth.setOnClickListener(mOnClickListener);

		mKnowledge = (Knowledge) getIntent().getSerializableExtra(PARAM_KNOW);
		setTitle(mKnowledge.getName());

		if (android.os.Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
			Bitmap icon = mKnowledge.getCachedLogoBitmap(this);
			getActionBar().setIcon(new BitmapDrawable(getResources(), icon));
		} else {
			getActionBar().setDisplayShowHomeEnabled(false);
		}

		String jsonArrayString = getIntent().getStringExtra(PARAM_UNITS);
		JSONArray jsonArray = JSONUtil.parseToJSONArray(jsonArrayString);
		List<JSONObject> jsons = JSONUtil.toJSONObjects(jsonArray);

		mItemHandler = new UnitItemHandler();

		for (JSONObject json : jsons) {
			mItemHandler.putUnit(json);
		}

		mLvUnits.setAdapter(new ItemAdapter(this));

		mLvUnits.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				UnitItem item = mItemHandler.getItems().get(position);
				Intent data = new Intent();
				data.putExtra(SEEK_TO_UNIT, item);
				setResult(RESULT_SEEK_TO_UNIT, data);
				finish();
			}
		});

		loadStudyHistory(8, mBtnWeek.getText().toString());
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Button b = (Button) v;
			String title = b.getText().toString();
			switch (v.getId()) {
			case R.id.btnToday:
				loadStudyHistory(1, title);
				break;
			case R.id.btnWeek:
				loadStudyHistory(8, title);
				break;
			case R.id.btnMonth:
				loadStudyHistory(31, title);
				break;
			}
		}
	};

	private void loadStudyHistory(final int days, final String title) {
		AsyncTask<Void, Void, JSONObject> task = new AsyncTask<Void, Void, JSONObject>() {
			private Exception _exception;

			@Override
			protected JSONObject doInBackground(Void... params) {
				String serviceURL = OneKnow.SERVICE_STUDY_ACTIVITY;

				TimeZone tz = TimeZone.getDefault();
				Date now = new Date();
				int offsetFromUtc = tz.getOffset(now.getTime())
						/ (1000 * 60 * 60);

				serviceURL = String.format(serviceURL, mKnowledge.getUqid(),
						days, offsetFromUtc);
				try {
					return OneKnow.getFrom(serviceURL, null, JSONObject.class);
				} catch (Exception e) {
					_exception = e;
				}

				return null;
			}

			@Override
			protected void onPostExecute(JSONObject result) {
				if (_exception != null) {
					Toast.makeText(StudyHistoryActivity.this,
							_exception.toString(), Toast.LENGTH_LONG).show();
					return;
				}
				ArrayList<String> dateList = new ArrayList<String>();
				JSONArray dates = JSONUtil.getJSONArray(result, "dates");
				for (int i = 0; i < dates.length(); i++) {
					try {
						String dateString = dates.getString(i);
						dateList.add(dateString);
					} catch (JSONException e) {
					}
				}

				JSONArray array = JSONUtil.getJSONArray(result, "rows");
				JSONObject json;
				try {
					json = array.getJSONObject(0);
				} catch (JSONException e) {
					json = new JSONObject();
				}

				mLayout.removeAllViews();

				ArrayList<GraphViewData> list = new ArrayList<GraphViewData>();
				for (int i = 0; i < dateList.size(); i++) {
					String dateString = dateList.get(i);
					try {
						String valueString = json.getString(dateString);
						list.add(new GraphViewData(i, Double
								.valueOf(valueString)));
					} catch (JSONException e) {

					}
				}
				GraphViewData[] data = list.toArray(new GraphViewData[list
						.size()]);
				GraphViewSeries series = new GraphViewSeries(data);

				String caption = getString(R.string.history_title);
				caption = String.format(caption, title);

				GraphView graphView = new LineGraphView(
						StudyHistoryActivity.this, caption);
				graphView.setScalable(false);
				graphView.addSeries(series);
				graphView.getGraphViewStyle().setHorizontalLabelsColor(
						Color.DKGRAY);
				graphView.getGraphViewStyle().setVerticalLabelsColor(
						Color.DKGRAY);
				graphView.getGraphViewStyle().setNumVerticalLabels(5);

				Display display = getWindowManager().getDefaultDisplay();
				DisplayMetrics outMetrics = new DisplayMetrics();
				display.getMetrics(outMetrics);

				float density = getResources().getDisplayMetrics().density;
				// float dpHeight = outMetrics.heightPixels / density;
				float dpWidth = outMetrics.widthPixels / density;

				float perWidth = (dpWidth - 50) / days;
				final int maxCellWidth = 75;
				ArrayList<String> a = new ArrayList<String>();

				if (days == 1 && dpWidth > 600) {
					a = dateList;
				} else if (days == 1 && dpWidth <= 320) {
					for (int i = 0; i < dateList.size(); i += 4) {
						a.add(dateList.get(i));
					}
				} else if (days == 1) {
					for (int i = 0; i < dateList.size(); i += 2) {
						a.add(dateList.get(i));
					}
				} else if (perWidth > 110) {
					a = dateList;
				} else if (perWidth > maxCellWidth) {
					for (String dateString : dateList) {
						a.add(trimDate(dateString));
					}
				} else if (days % 7 == 1) {
					for (int i = 0; i <= dateList.size(); i += 7) {
						if ((i == 7 || i == dateList.size() - 1 - 7)
								&& perWidth * 7 < maxCellWidth)
							a.add(StringUtil.WHITESPACE);
						else
							a.add(trimDate(dateList.get(i)));
					}

				} else if (days % 3 == 1) {
					for (int i = 0; i < dateList.size(); i += 3) {
						if ((i == 3 || i == dateList.size() - 1 - 3)
								&& perWidth * 3 < maxCellWidth)
							a.add(StringUtil.WHITESPACE);
						else
							a.add(trimDate(dateList.get(i)));
					}

				} else if (days % 2 == 1) {
					for (int i = 0; i < dateList.size(); i += 2) {
						if ((i == 2 || i == dateList.size() - 1 - 2)
								&& perWidth * 2 < maxCellWidth)
							a.add(StringUtil.WHITESPACE);
						else
							a.add(trimDate(dateList.get(i)));
					}

				} else {
					a.add(dateList.get(0));
					a.add(dateList.get(dateList.size() - 1));
				}

				String[] dateArray = a.toArray(new String[a.size()]);
				graphView.setHorizontalLabels(dateArray);
				mLayout.addView(graphView);
			}
		};
		task.execute();
	}

	private String trimDate(String dateString) {
		return dateString.substring(5).replace('-', '/');
	}

	private class ItemAdapter extends BaseAdapter {
		private LayoutInflater _inflater;

		public ItemAdapter(Context context) {
			_inflater = (LayoutInflater) context
					.getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mItemHandler.getItems().size();
		}

		@Override
		public Object getItem(int position) {
			return mItemHandler.getItems().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			UnitItem item = mItemHandler.getItems().get(position);
			if (item.getMode() == UnitItem.MODE_CHAPTER) {
				convertView = _inflater.inflate(
						R.layout.item_study_history_chapter, null);
				TextView txt = (TextView) convertView;
				txt.setText(item.getName());
			} else {
				convertView = _inflater.inflate(
						R.layout.item_study_history_unit, null);
				TextView txtName = (TextView) convertView
						.findViewById(R.id.txtName);
				TextView txtPerc = (TextView) convertView
						.findViewById(R.id.txtPercent);

				txtName.setText(item.getName());

				JSONObject json = item.getJSON();
				double d = JSONUtil.getDouble(json, "progress");
				txtPerc.setText((int) d + "%");
				txtPerc.setTextColor(getColor(d));
			}
			return convertView;
		}
	}

	private int getColor(double d) {
		if (d == 100) {
			return Color.rgb(0xa0, 0xa0, 0xa0);
		} else if (d == 0) {
			return Color.rgb(0xd8, 0x60, 0x60);
		} else {
			return Color.rgb(0x42, 0x69, 0x64);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.study_history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_unsubscribe:
			unsubscribe();
		}
		return true;
	}

	private void unsubscribe() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.confirm)
				.setMessage(R.string.history_unsubscribe_confirm)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								UnsubscribeTask task = new UnsubscribeTask();
								task.setOnReceiveListener(mReceiveListener);
								task.execute();
							}

						}).setNegativeButton(R.string.no, null).show();
	}

	private class UnsubscribeTask extends AsyncTask<Void, Void, Void> {

		private OnReceiveListener<Void> mListener;
		private Exception _exception;
		@Override
		protected Void doInBackground(Void... params) {
			String serviceURL = OneKnow.SERVICE_KNOW_UNSUBSCRIBE;
			serviceURL = String.format(serviceURL, mKnowledge.getUqid());
			try {
				OneKnow.delete(serviceURL);
			} catch (Exception e) {
				_exception = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(_exception != null)
				mListener.onError(_exception);
			else
				mListener.onReceive(result);
		}
		
		private void setOnReceiveListener(OnReceiveListener<Void> listener) {
			mListener = listener;
		}
	}

	private OnReceiveListener<Void> mReceiveListener = new OnReceiveListener<Void>() {

		@Override
		public void onReceive(Void result) {
			KnowDataSource kds = new KnowDataSource(StudyHistoryActivity.this);
			kds.open();
			kds.subscribeKnowledge(mKnowledge, false);
			kds.close();

			Intent intent = new Intent(StudyHistoryActivity.this,
					MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}

		@Override
		public void onError(Exception e) {
			String text = getString(R.string.history_unsubscribe_failure);
			text = String.format(text, e.getMessage());
			Toast.makeText(StudyHistoryActivity.this, text, Toast.LENGTH_LONG)
					.show();
		}
	};
}
