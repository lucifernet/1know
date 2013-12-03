package tw.com.ischool.oneknow.main;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.item.ActionItem;
import tw.com.ischool.oneknow.item.BaseItem;
import tw.com.ischool.oneknow.item.DisplayStatus;
import tw.com.ischool.oneknow.item.FragmentItem;
import tw.com.ischool.oneknow.item.ItemProvider;
import tw.com.ischool.oneknow.item.LearningItem;
import tw.com.ischool.oneknow.item.ProfilerItem;
import tw.com.ischool.oneknow.item.TabsItem;
import tw.com.ischool.oneknow.login.ILoginHelper.OnLoginResultListener;
import tw.com.ischool.oneknow.login.LoginActivity;
import tw.com.ischool.oneknow.login.LoginHelper;
import tw.com.ischool.oneknow.main.IReloadable.OnReloadCompletedListener;
import tw.com.ischool.oneknow.util.ActivityHelper;
import tw.com.ischool.oneknow.util.JSONUtil;
import tw.com.ischool.oneknow.util.ViewPagerAdapter;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;

public class MainActivity extends Activity {

	// private static OrientationEnum ORIENTATION;

	public static final String TAG = "1know";
	public static final String PARAM_EXITS = "exits";

	public static final int REQUEST_CODE_LOGIN = 1;
	private static final String CURRENT_TAB_INDEX = "current_index";
	private static final String CURRENT_ITEM_INDEX = "current_item_index";
	private static final String CURRENT_DISPLAY_STATUS = "current_display_status";
	private static final String CURRENT_LOGIN_INFO = "current_login_info";

	public static final int MODE_DISCOVER = 0;
	public static final int MODE_EDITOR_CHOICE = 1;
	public static final int MODE_YOUR_CHANNEL = 2;

	private static int INIT_FLAG = -1;

	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ItemAdapter mItemAdapter;
	private Fragment mCurrentFragment;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private FrameLayout mContainer;
	private LinearLayout mProgress;
	private List<BaseItem> mItemList;
	private int mCurrentTabIndex = -1;
	private int mCurrentItemIndex = -1;
	private DisplayStatus mCurrentDisplayStatus;
	private JSONObject mLoginInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mItemList = new ArrayList<BaseItem>();

		if (savedInstanceState != null) {
			mCurrentTabIndex = savedInstanceState.getInt(CURRENT_TAB_INDEX);
			mCurrentItemIndex = savedInstanceState.getInt(CURRENT_ITEM_INDEX);
			mCurrentDisplayStatus = (DisplayStatus) savedInstanceState
					.getSerializable(CURRENT_DISPLAY_STATUS);
			mItemList = ItemProvider.getItems(mCurrentDisplayStatus);

			String loginInfo = savedInstanceState.getString(CURRENT_LOGIN_INFO);
			mLoginInfo = JSONUtil.parseToJSONObject(loginInfo);
		}

		Intent intent = getIntent();
		if (intent != null && intent.getBooleanExtra(PARAM_EXITS, false)) {
			finish();
			return;
		}

		getActionBar().setIcon(R.drawable.ic_1know);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		mProgress = (LinearLayout) findViewById(R.id.progress);
		mProgress.setVisibility(View.GONE);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		mContainer = (FrameLayout) this.findViewById(R.id.container);
		mDrawerListView = (ListView) this.findViewById(R.id.left_drawer);

		mItemAdapter = new ItemAdapter(this, R.layout.drawer_list_item);
		mDrawerListView.setAdapter(mItemAdapter);
		mDrawerListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {				
				onItemSelected(position);
			}
		});

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

		ActivityHelper helper = new ActivityHelper(this);
		helper.valid();

		if (savedInstanceState == null)
			this.autoLogin();
		else
			onItemSelected(mCurrentItemIndex);

		// UserLoginHelper login = new UserLoginHelper(this);
		// if (savedInstanceState == null && !login.getUserName().isEmpty()) {
		//
		// login.setOnLoginResultListener(new OnLoginResultListener() {
		//
		// @Override
		// public void onSucceed(JSONObject json) {
		// IsLogined = true;
		// renderItems();
		// int i = ItemProvider.findItemIndex(YourKnowledgeItem.class);
		// onItemSelected(i);
		// }
		//
		// @Override
		// public void onFail(String message, Exception e) {
		// IsLogined = false;
		// renderItems();
		//
		// // TODO 應該是discovery item 才是
		// int i = ItemProvider.findItemIndex(YourKnowledgeItem.class);
		// onItemSelected(i);
		// }
		// });
		//
		// login.execute();
		//
		// } else {
		// int index = ItemProvider.findItemIndex(YourKnowledgeItem.class);
		//
		// if (savedInstanceState != null)
		// index = savedInstanceState.getInt(CURRENT_INDEX);
		//
		// renderItems();
		// this.onItemSelected(index);
		// }

		INIT_FLAG = 1;
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
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		if (item.getItemId() == R.id.reload) {
			// TODO 這裡要取出目前在 pager 裡的 fragment
			Fragment fragment = null;

			if (mViewPager.getVisibility() == View.VISIBLE) {
				fragment = mTabsAdapter.getRegisteredFragment(mViewPager
						.getCurrentItem());
			} else {
				fragment = this.getFragmentManager().findFragmentById(
						R.id.container);
			}

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
	public void onPostCreate(Bundle savedInstanceState) {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		MenuItem menuItem = menu.findItem(R.id.search);

		if (menuItem != null) {
			SearchView searchView = (SearchView) menuItem.getActionView();
			int id = searchView.getContext().getResources()
					.getIdentifier("android:id/search_src_text", null, null);
			final TextView textView = (TextView) searchView.findViewById(id);
			textView.setHintTextColor(Color.WHITE);
			searchView.setOnQueryTextListener(new OnQueryTextListener() {

				@Override
				public boolean onQueryTextSubmit(String query) {
					if (mCurrentFragment instanceof ISearchable) {
						ISearchable searchable = (ISearchable) mCurrentFragment;
						searchable.search(query);
					}
					return true;
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					if (newText.length() > 0 || !textView.isFocused())
						return false;

					if (mCurrentFragment instanceof ISearchable) {
						ISearchable searchable = (ISearchable) mCurrentFragment;
						searchable.cancelSearch();
					}
					return true;
				}
			});

			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));
		}
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(CURRENT_TAB_INDEX, mCurrentTabIndex);
		outState.putInt(CURRENT_ITEM_INDEX, mCurrentItemIndex);
		outState.putSerializable(CURRENT_DISPLAY_STATUS, mCurrentDisplayStatus);
		outState.putString(CURRENT_LOGIN_INFO, mLoginInfo.toString());

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_LOGIN
				&& resultCode == LoginActivity.RESULT_CODE_SUCCEED) {
			renderItems(DisplayStatus.LOGINED);

			int index = ItemProvider.findIndex(mItemList, ProfilerItem.class);

			String jsonString = data
					.getStringExtra(LoginActivity.BUNDLE_LOGIN_INFO);
			JSONObject json = JSONUtil.parseToJSONObject(jsonString);
			ProfilerItem pitem = (ProfilerItem) mItemList.get(index);
			pitem.setProfile(json);

			index = ItemProvider.findIndex(mItemList, LearningItem.class);
			onItemSelected(index);
		}
	}

	public void autoLogin() {
		mProgress.setVisibility(View.VISIBLE);
		LoginHelper.autoLogin(this, new OnLoginResultListener() {

			@Override
			public void onSucceed(JSONObject json, DisplayStatus status) {
				switchUser(status, json);
				mProgress.setVisibility(View.GONE);
			}

			@Override
			public void onFail(String message, Exception e) {
				// 理論上除非網路異常或怎樣, 不然應該不會出現在這裡
				Log.d(TAG, "Auto login failure : " + message);
				mProgress.setVisibility(View.GONE);

				String error = getString(R.string.switch_failure);
				error = String.format(error, message);
				Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	public void switchUser(DisplayStatus status, JSONObject userInfo) {
		mCurrentDisplayStatus = status;
		mLoginInfo = userInfo;

		renderItems(status);

		int index = ItemProvider.findIndex(mItemList, ProfilerItem.class);
		ProfilerItem pitem = (ProfilerItem) mItemList.get(index);
		pitem.setProfile(userInfo);

		index = ItemProvider.findIndex(mItemList, LearningItem.class);
		onItemSelected(index);
	}

	public FragmentItem getCurrentItem() {
		BaseItem item = mItemList.get(mCurrentItemIndex);
		if (item instanceof TabsItem) {
			TabsItem titem = (TabsItem) item;
			return titem.getItems().get(mCurrentTabIndex);
		}

		if (item instanceof FragmentItem)
			return (FragmentItem) item;

		return null;
	}

	private void renderItems(DisplayStatus status) {
		List<BaseItem> items = ItemProvider.getItems(status);
		mItemList.clear();
		mItemList.addAll(items);
		// mTabsAdapter.clearTabs();
		mItemAdapter.notifyDataSetChanged();
		mItemAdapter.notifyDataSetInvalidated();

		mTabsAdapter = new TabsAdapter(this, mViewPager);
	}

	// private void renderTabs(int position) {
	// mTabsAdapter.clearTabs();
	//
	// BaseItem item = ItemProvider.getItem(position);
	// ActionBar bar = this.getActionBar();
	// bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	//
	// // bar.removeAllTabs();
	//
	// if (item.getGroup() == ItemProvider.GROUP_NONE) {
	// mContainer.setVisibility(View.VISIBLE);
	// mViewPager.setVisibility(View.GONE);
	// if (item.getFragmentClass() != null) {
	// deployFragment(item.getFragmentClass().getName());
	// }
	// return;
	// }
	// if (item.getSortInGroup() == BaseItem.SORT_NO_TAB) {
	// setTitle(item.getTitle());
	// mContainer.setVisibility(View.VISIBLE);
	// mViewPager.setVisibility(View.GONE);
	// if (item.getFragmentClass() != null) {
	// deployFragment(item.getFragmentClass().getName());
	// }
	// return;
	// }
	//
	// bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	// mContainer.setVisibility(View.GONE);
	// mViewPager.setVisibility(View.VISIBLE);
	//
	// List<BaseItem> items = ItemProvider.getTabItems(item.getGroup());
	// if (items.size() > 1) {
	// for (BaseItem it : items) {
	// if (it.getStatus().isMember(DisplayStatus.LOGINED)
	// && !IsLogined)
	// continue;
	//
	// Tab tab = bar.newTab().setText(it.getTitle())
	// .setIcon(it.getIcon())
	// .setTabListener(new MyTabListener(it));
	//
	// mTabsAdapter.addTab(tab, it.getFragmentClass(), new Bundle());
	//
	// //
	// // bar.addTab(tab);
	// }
	//
	// // if (bar.getTabCount() > 0)
	// // bar.setSelectedNavigationItem(item.getSortInGroup());
	// }
	// }

	private void onItemSelected(int position) {
		BaseItem item = mItemList.get(position);
		mCurrentItemIndex = position;
		mDrawerListView.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerListView);

		if (item instanceof TabsItem) {
			setTitle(item.getTitle());
			ActionBar bar = getActionBar();
			mContainer.setVisibility(View.GONE);
			mViewPager.setVisibility(View.VISIBLE);
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			TabsItem tabItem = (TabsItem) item;
			mTabsAdapter.clearTabs();

			for (FragmentItem ft : tabItem.getItems()) {
				Tab tab = bar.newTab().setText(ft.getTitle())
						.setIcon(ft.getIcon());
				mTabsAdapter.addTab(tab, ft.getFragmentClass(), new Bundle());

				// bar.addTab(tab);
			}
			mTabsAdapter.notifyDataSetChanged();

			if (mCurrentTabIndex > 0) {
				mViewPager.setCurrentItem(mCurrentTabIndex);
			}
			
			mItemAdapter.notifyDataSetChanged();
		} else if (item instanceof FragmentItem) {
			setTitle(item.getTitle());
			mContainer.setVisibility(View.VISIBLE);
			mViewPager.setVisibility(View.GONE);
			getActionBar()
					.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

			FragmentItem ft = (FragmentItem) item;
			String className = ft.getFragmentClass().getName();
			mCurrentFragment = Fragment.instantiate(this, className);
			this.getFragmentManager().beginTransaction()
					.replace(R.id.container, mCurrentFragment, className)
					.commitAllowingStateLoss();

			mItemAdapter.notifyDataSetChanged();
		} else if (item instanceof ActionItem) {
			ActionItem action = (ActionItem) item;
			action.invoke(this);
		}
		
		invalidateOptionsMenu();
		
		
		// switch (selectedItem.getTitle()) {
		//
		// case R.string.item_discover:
		// this.renderTabs(position);
		// break;
		// case R.string.item_editor_choice:
		// this.renderTabs(position);
		// break;
		// case R.string.item_your_channel:
		// this.renderTabs(position);
		// break;
		// case R.string.item_login:
		// Intent intent = new Intent(this, LoginActivity.class);
		// startActivityForResult(intent, REQUEST_CODE_LOGIN);
		// return;
		// case R.string.item_logout:
		// IsLogined = false;
		// mDrawerListView.invalidateViews();
		// break;
		// case R.string.item_profile:
		// this.renderTabs(position);
		//
		// break;
		// default:
		// this.renderTabs(position);
		// }
	}

	// private void deployFragment(String className) {
	// if (this.isFinishing())
	// return;
	//
	// mCurrentFragment = Fragment.instantiate(this, className);
	// this.getFragmentManager().beginTransaction()
	// .replace(R.id.container, mCurrentFragment, className)
	// .commitAllowingStateLoss();
	//
	// invalidateOptionsMenu();
	// }

	public static int getInitFlag() {
		return INIT_FLAG;
	}

	private class ItemAdapter extends ArrayAdapter<BaseItem> {

		private int _resource;
		// private Context _context;
		private LayoutInflater _inflator;

		public ItemAdapter(Context context, int resource) {
			super(context, resource, mItemList);

			_resource = resource;
			_inflator = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BaseItem item = mItemAdapter.getItem(position);

			// if (item.getStatus().isMember(DisplayStatus.LOGINED) &&
			// !IsLogined)
			// convertView = _inflator.inflate(R.layout.disappear_item, null);
			// else if (item.getStatus().isMember(DisplayStatus.UNLOGIN)
			// && IsLogined)
			// convertView = _inflator.inflate(R.layout.disappear_item, null);
			// else if (item.getStatus().isMember(DisplayStatus.SEPARATION)) {
			// convertView = _inflator.inflate(R.layout.drawer_sep, null);
			//
			// TextView textView = (TextView) convertView
			// .findViewById(R.id.txtCaption);
			// textView.setText(item.getTitle());
			// } else {

			if (item instanceof ProfilerItem) {
				convertView = _inflator.inflate(R.layout.drawer_profile_item,
						null);
				ProfilerItem pitem = (ProfilerItem) item;
				pitem.setItemView(MainActivity.this, convertView);
			} else {
				convertView = _inflator.inflate(_resource, null);

				TextView textView = (TextView) convertView
						.findViewById(R.id.txtItem);
				textView.setText(item.getTitle());

				ImageView image = (ImageView) convertView
						.findViewById(R.id.imgItemIcon);
				image.setImageResource(item.getIcon());
			}

			if(position == mCurrentItemIndex){
				int color = getResources().getColor(R.color.drawer_item_selected);
				convertView.setBackgroundColor(color);
			}
			
			// }

			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			// BaseItem item = ItemProvider.getItem(position);
			// if (item.getStatus().isMember(DisplayStatus.SEPARATION))
			// return false;
			// if (item.getStatus() == DisplayStatus.LOGINED && !IsLogined)
			// return false;
			// if (item.getStatus() == DisplayStatus.UNLOGIN && IsLogined)
			// return false;

			return true;
		}
	}

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	public class TabsAdapter extends ViewPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		final class TabInfo {
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(Class<?> _class, Bundle _args) {
				clss = _class;
				args = _args;
			}
		}

		public TabsAdapter(Activity activity, ViewPager pager) {
			super(activity.getFragmentManager());
			mContext = activity;
			mActionBar = activity.getActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
			TabInfo info = new TabInfo(clss, args);
			tab.setTag(info);
			tab.setTabListener(this);
			mTabs.add(info);
			notifyDataSetChanged();
			mActionBar.addTab(tab);
		}

		public void clearTabs() {
			mActionBar.removeAllTabs();
			mTabs.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(),
					info.args);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);

		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i) == tag) {
					mViewPager.setCurrentItem(i);
				}
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		protected void onPrimaryItemChanged(int position) {
			mCurrentFragment = getRegisteredFragment(position);

			mCurrentTabIndex = position;
			invalidateOptionsMenu();
			mDrawerLayout.closeDrawer(mDrawerListView);
		}
	}
}
