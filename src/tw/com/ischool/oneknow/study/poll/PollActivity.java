package tw.com.ischool.oneknow.study.poll;

import tw.com.ischool.oneknow.R;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class PollActivity extends Activity {

	private PollFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poll);

		getActionBar().hide();

		FragmentManager fm = getFragmentManager();
		mFragment = new PollFragment();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		mFragment.setArguments(bundle);
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.container, mFragment);
		ft.commitAllowingStateLoss();
	}

	@Override
	public void onBackPressed() {
		Bundle bundle = mFragment.createBundle();
		Intent data = new Intent();
		data.putExtras(bundle);
		setResult(0, data);
		finish();
	}

}
