package tw.com.ischool.oneknow.study.draw;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.study.draw.DrawFragment.BundleListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class DrawActivity extends Activity {

	private DrawFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw);

		getActionBar().hide();

		FragmentManager fm = getFragmentManager();
		mFragment = new DrawFragment();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		mFragment.setArguments(bundle);
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.container_draw, mFragment);
		ft.commitAllowingStateLoss();
	}

	@Override
	public void onBackPressed() {
		mFragment.createBundle(new BundleListener() {
			@Override
			public void onBundleReady(Bundle bundle) {
				Intent data = new Intent();				
				data.putExtras(bundle);
				setResult(0, data);
				finish();
			}
		});
	}
}
