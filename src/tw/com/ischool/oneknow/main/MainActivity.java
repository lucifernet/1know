package tw.com.ischool.oneknow.main;

import java.util.List;

import org.json.JSONObject;

import tw.com.ischool.oneknow.OrientationEnum;
import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.item.BaseItem;
import tw.com.ischool.oneknow.item.ItemProvider;
import tw.com.ischool.oneknow.item.YourKnowledgeItem;
import tw.com.ischool.oneknow.learn.DisplayStatus;
import tw.com.ischool.oneknow.login.LoginActivity;
import tw.com.ischool.oneknow.login.UserLoginHelper;
import tw.com.ischool.oneknow.login.UserLoginHelper.OnLoginResultListener;
import tw.com.ischool.oneknow.main.IReloadable.OnReloadCompletedListener;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	private static OrientationEnum ORIENTATION;
	private static boolean IsLogined;

	public static final String TAG = "1know";
	private static final int REQUEST_CODE_LOGIN = 1;
	private static final String CURRENT_INDEX = "current_index";
	private static int MainCurrentIndex;

	public static final int MODE_DISCOVER = 0;
	public static final int MODE_EDITOR_CHOICE = 1;
	public static final int MODE_YOUR_CHANNEL = 2;

	private static int INIT_FLAG = -1;

	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private Fragment mCurrentFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		INIT_FLAG = 1;

		UserLoginHelper login = new UserLoginHelper(this);
		if (savedInstanceState == null && !login.getUserName().isEmpty()) {

			login.setOnLoginResultListener(new OnLoginResultListener() {

				@Override
				public void onSucceed(JSONObject json) {
					IsLogined = true;
					renderItems();
					int i = ItemProvider.findItemIndex(YourKnowledgeItem.class);
					onItemSelected(i);
				}

				@Override
				public void onFail(String message, Exception e) {
					IsLogined = false;
					renderItems();

					// TODO 應該是discovery item 才是
					int i = ItemProvider.findItemIndex(YourKnowledgeItem.class);
					onItemSelected(i);
				}
			});

			login.execute();

		} else {
			int index = ItemProvider.findItemIndex(YourKnowledgeItem.class);

			if (savedInstanceState != null)
				index = savedInstanceState.getInt(CURRENT_INDEX);

			renderItems();
			this.onItemSelected(index);
		}
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListView);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);

		MenuItem reloadItem = menu.findItem(R.id.reload);
		if (reloadItem != null)
			reloadItem.setVisible(false);

		MenuItem searchItem = menu.findItem(R.id.search);
		if (searchItem != null)
			searchItem.setVisible(false);

		// TODO
		if (mCurrentFragment != null) {
			if (mCurrentFragment instanceof IReloadable) {
				reloadItem.setVisible(true);
			}

			if (mCurrentFragment instanceof ISearchable) {
				searchItem.setVisible(true);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// 如果回傳 true, 表示這是 icon 被按的事件，而且已經被處理了
		if (ORIENTATION == OrientationEnum.NORMAL
				&& mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		if (item.getItemId() == R.id.reload) {
			Fragment fragment = this.getSupportFragmentManager()
					.findFragmentById(R.id.content_frame);
			if (fragment != null && fragment instanceof IReloadable) {

				LayoutInflater inflater = (LayoutInflater) this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ImageView iv = (ImageView) inflater.inflate(
						R.layout.refresh_action_view, null);

				Animation rotation = AnimationUtils.loadAnimation(this,
						R.anim.rotate);
				rotation.setRepeatCount(Animation.INFINITE);
				iv.startAnimation(rotation);
				item.setActionView(iv);

				IReloadable reloadable = (IReloadable) fragment;
				reloadable
						.setOnReloadCompletedListener(new OnReloadCompletedListener() {

							@Override
							public void onCompleted() {
								item.getActionView().clearAnimation();
								item.setActionView(null);
							}
						});
				reloadable.reload();
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (ORIENTATION == OrientationEnum.NORMAL && mDrawerToggle != null)
			mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (ORIENTATION == OrientationEnum.NORMAL)
			mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private void renderItems() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (mDrawerLayout != null) {
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
					R.drawable.ic_drawer, R.string.drawer_open,
					R.string.drawer_close) {

				/**
				 * Called when a drawer has settled in a completely closed
				 * state.
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
			ORIENTATION = OrientationEnum.NORMAL;
		} else {
			ORIENTATION = OrientationEnum.SW600DPLAND;
		}

		ItemAdapter adapter = new ItemAdapter(this, R.layout.drawer_list_item);
		mDrawerListView = (ListView) this.findViewById(R.id.left_drawer);
		mDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
		mDrawerListView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		MenuItem menuItem = menu.findItem(R.id.search);

		if (menuItem != null) {
			SearchView searchView = (SearchView) menuItem.getActionView();
			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(CURRENT_INDEX, MainCurrentIndex);
		super.onSaveInstanceState(outState);
	}

	public static OrientationEnum getOrientation() {
		return ORIENTATION;
	}

	public static boolean isLogined() {
		return IsLogined;
	}

	public static int getCurrentIndex() {
		return MainCurrentIndex;
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			onItemSelected(position);
		}
	}

	public void onItemSelected(int position) {
		BaseItem selectedItem = ItemProvider.getItem(position);

		setTitle(selectedItem.getTitle());
		mDrawerListView.setItemChecked(position, true);
		if (ORIENTATION == OrientationEnum.NORMAL)
			mDrawerLayout.closeDrawer(mDrawerListView);

		switch (selectedItem.getTitle()) {

		case R.string.item_discover:
			this.renderTabs(position);
			break;
		case R.string.item_editor_choice:
			this.renderTabs(position);
			break;
		case R.string.item_your_channel:

			this.renderTabs(position);
			break;
		case R.string.item_login:
			Intent intent = new Intent(this, LoginActivity.class);
			startActivityForResult(intent, REQUEST_CODE_LOGIN);
			return;
		case R.string.item_logout:
			IsLogined = false;
			mDrawerListView.invalidateViews();
			break;
		case R.string.item_profile:
			this.renderTabs(position);

			break;
		default:
			this.renderTabs(position);
		}

		MainCurrentIndex = position;
	}

	private void renderTabs(int position) {
		ActionBar bar = this.getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.removeAllTabs();

		BaseItem item = ItemProvider.getItem(position);

		if (item.getFragmentClass() != null) {
			deployFragment(item.getFragmentClass().getName());
		}

		if (item.getGroup() == ItemProvider.GROUP_NONE)
			return;

		List<BaseItem> items = ItemProvider.getItems(item.getGroup());
		if (items.size() > 1) {
			for (BaseItem it : items) {
				if (it.getStatus().isMember(DisplayStatus.LOGINED)
						&& !IsLogined)
					continue;

				Tab tab = bar.newTab().setText(it.getTitle())
						.setIcon(it.getIcon())
						.setTabListener(new MyTabListener(it));
				bar.addTab(tab);
			}

			bar.setSelectedNavigationItem(item.getSortInGroup());
		}
	}

	public class MyTabListener implements ActionBar.TabListener {
		// private Class<?> _class;
		// private String _tagName;
		private BaseItem _item;

		public MyTabListener(BaseItem item) {
			_item = item;
		}

		@Override
		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {

		}

		@Override
		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
			setTitle(tab.getText());

			MainCurrentIndex = ItemProvider.findItemIndex(_item.getClass());
			if (_item.getFragmentClass() != null)
				deployFragment(_item.getFragmentClass().getName());
		}

		@Override
		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {

		}
	}

	private void deployFragment(String className) {
		// TODO
		mCurrentFragment = Fragment.instantiate(this, className);
		this.getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mCurrentFragment, className)
				.commitAllowingStateLoss();

		invalidateOptionsMenu();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_LOGIN
				&& resultCode == LoginActivity.RESULT_CODE_SUCCEED) {
			IsLogined = true;
			mDrawerListView.invalidateViews();

			renderItems();
			int i = ItemProvider.findItemIndex(YourKnowledgeItem.class);
			onItemSelected(i);
		}
	}

	private class ItemAdapter extends ArrayAdapter<BaseItem> {

		private int _resource;
		// private Context _context;
		private LayoutInflater _inflator;

		public ItemAdapter(Context context, int resource) {
			super(context, resource, ItemProvider.getItems());

			_resource = resource;
			_inflator = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BaseItem item = ItemProvider.getItem(position);

			if (item.getStatus().isMember(DisplayStatus.LOGINED) && !IsLogined)
				convertView = _inflator.inflate(R.layout.disappear_item, null);
			else if (item.getStatus().isMember(DisplayStatus.UNLOGIN)
					&& IsLogined)
				convertView = _inflator.inflate(R.layout.disappear_item, null);
			else if (item.getStatus().isMember(DisplayStatus.SEPARATION)) {
				convertView = _inflator.inflate(R.layout.drawer_sep, null);

				TextView textView = (TextView) convertView
						.findViewById(R.id.txtCaption);
				textView.setText(item.getTitle());
			} else {

				convertView = _inflator.inflate(_resource, null);

				TextView textView = (TextView) convertView
						.findViewById(R.id.txtItem);
				textView.setText(item.getTitle());

				ImageView image = (ImageView) convertView
						.findViewById(R.id.imgItemIcon);
				image.setImageResource(item.getIcon());
			}

			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			BaseItem item = ItemProvider.getItem(position);
			if (item.getStatus().isMember(DisplayStatus.SEPARATION))
				return false;
			if (item.getStatus() == DisplayStatus.LOGINED && !IsLogined)
				return false;
			if (item.getStatus() == DisplayStatus.UNLOGIN && IsLogined)
				return false;

			return true;
		}
	}

	public static int getInitFlag() {
		return INIT_FLAG;
	}
}
