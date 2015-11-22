package com.trevorhalvorson.devjobs.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.google.gson.Gson;
import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.fragment.EditLocationDialog;
import com.trevorhalvorson.devjobs.fragment.JobListFragment;
import com.trevorhalvorson.devjobs.fragment.SavedSearchesFragment;
import com.trevorhalvorson.devjobs.model.Search;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements EditLocationDialog.EditLocationDialogListener,
        SavedSearchesFragment.SavedSearchSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SAVED_SEARCHES_KEY = "saved_searches_key";
    private static final String NUM_SAVED_SEARCHES_KEY = "num_saved_searches_key";

    private DrawerLayout mDrawerLayout;
    private SearchView mSearchView;
    private static SearchListener mListener;
    private ViewPager mViewPager;
    private List<Search> mSavedSearches;
    private String mLocationString;
    private String mQuery;

    public interface SearchListener {
        void search(String query, String location);
    }

    @Override
    public void onSearchSelected(Search savedSearch) {
        mQuery = savedSearch.getDescription();
        mLocationString = savedSearch.getLocation();
        mListener.search(mQuery, mLocationString);
    }

    public static void setListener(SearchListener listener) {
        mListener = listener;
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        mLocationString = inputText;
        Snackbar.make(
                findViewById(R.id.main_content),
                "Location set to " + mLocationString,
                Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        editor.putInt(NUM_SAVED_SEARCHES_KEY, mSavedSearches.size());
        int savedCount = 0;
        for (Search search : mSavedSearches) {
            String json = gson.toJson(search);
            editor.putString(SAVED_SEARCHES_KEY + savedCount, json);
            savedCount++;
        }
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditLocationDialog.setListener(this);
        SavedSearchesFragment.setListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        mSavedSearches = new ArrayList<>();

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();
        for (int i = 0; i < preferences.getInt(NUM_SAVED_SEARCHES_KEY, 0); i++) {
            String json = preferences.getString(SAVED_SEARCHES_KEY + i, "");
            Search search = gson.fromJson(json, Search.class);
            mSavedSearches.add(search);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new JobListFragment(), "Jobs");
        adapter.addFragment(new SavedSearchesFragment(), "Saved Searches");

        viewPager.setAdapter(adapter);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        mViewPager.setCurrentItem(item, smoothScroll);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.search_description));
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setCurrentItem(0, true);
                if (!query.isEmpty()) {
                    mSearchView.clearFocus();
                    mQuery = query;
                    mListener.search(mQuery, mLocationString);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_location:
                FragmentManager fm = getSupportFragmentManager();
                EditLocationDialog dialog = EditLocationDialog.newInstance();
                dialog.show(fm, "fragment_edit_location");
                return true;
            case R.id.action_save_search:
                saveSearch();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveSearch() {
        mSavedSearches.add(new Search(mQuery, mLocationString));
        setupViewPager(mViewPager);
    }
}
