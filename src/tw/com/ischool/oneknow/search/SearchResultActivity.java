package tw.com.ischool.oneknow.search;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.item.FragmentItem;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.TextView;

public class SearchResultActivity extends FragmentActivity {

	private TextView mTxtTitle;
	private FragmentItem mSelectedItem;
	private TextView mTxtCount;
	private String mKeyword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);

		mTxtTitle = (TextView) this.findViewById(R.id.search_title);
//		mSelectedItem = MainActivity.get
		mTxtTitle.setText(mSelectedItem.getTitle());

		mTxtCount = (TextView) this.findViewById(R.id.search_result_count);
		
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);

		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			mKeyword = intent.getStringExtra(SearchManager.QUERY);

			String title = getString(R.string.title_activity_search_result);
			title = String.format(title, mKeyword);
			setTitle(title);
		}

//		Fragment fragment = Fragment.instantiate(this, mSelectedItem
//				.getFragmentClass().getName());
//
//		FragmentManager fm = getSupportFragmentManager();
//		fm.beginTransaction().replace(R.id.search_container, fragment)
//				.commitAllowingStateLoss();
//
//		if (fragment instanceof ISearchable) {
//			final ISearchable searchable = (ISearchable) fragment;
//			searchable.setOnSearchListener(new OnSearchListener() {
//
//				@Override
//				public void onSearchCompleted(int count) {
//					String result = getString(R.string.search_result_count);
//					result = String.format(result, count);
//					mTxtCount.setText(result);
//				}
//
//				@Override
//				public void onDataReady() {
//					searchable.search(mKeyword);
//				}
//			});
//		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_result, menu);
		return true;
	}

}
