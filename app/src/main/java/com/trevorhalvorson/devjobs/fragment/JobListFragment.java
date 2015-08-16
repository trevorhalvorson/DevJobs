package com.trevorhalvorson.devjobs.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trevorhalvorson.devjobs.DividerItemDecoration;
import com.trevorhalvorson.devjobs.GHJobsAPI;
import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.activity.SettingsActivity;
import com.trevorhalvorson.devjobs.adapter.JobAdapter;
import com.trevorhalvorson.devjobs.model.Job;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class JobListFragment extends Fragment {

    private static final String TAG = JobListFragment.class.getSimpleName();
    private static final String KEY_SAVE_STATE = "KEY_JOB_LIST";
    private static final String ENDPOINT = "https://jobs.github.com";
    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayList<Job> mJobArrayList;
    private ArrayList<String> mHiddenJobList;
    private JobAdapter mJobAdapter;
    private FloatingActionButton mFloatingActionButton;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mJobDescriptionString;
    private String mLocationString;
    private EditText mLocationEditText;
    private Job mSwipedJob;
    private AppCompatActivity mAppCompatActivity;
    private DrawerLayout mDrawerLayout;
    private SharedPreferences mSharedPreferences;
    private ProgressBar mHorizontalProgressBar;
    private ProgressBar mProgressBar;
    private GHJobsAPI mAPI;
    private RestAdapter mRestAdapter;
    private int mPageCount;
    private LinearLayoutManager mLayoutManager;
    private SearchView mSearchView;

    public JobListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .build();
        mAPI = mRestAdapter.create(GHJobsAPI.class);
        mHiddenJobList = new ArrayList<>();

        for (int i = 0; ; ++i) {
            final String hiddenId = mSharedPreferences.getString("hidden_id" + String.valueOf(i), "");
            if (!hiddenId.equals("")) {
                mHiddenJobList.add(i, hiddenId);
            } else {
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SAVE_STATE, mJobArrayList);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        for (int i = 0; i < mHiddenJobList.size(); ++i) {
            editor.putString("hidden_id" + String.valueOf(i), mHiddenJobList.get(i));
        }
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_job_list, container, false);


        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.jobs_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mFloatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mHorizontalProgressBar = (ProgressBar) rootView.findViewById(R.id.horizontal_progress_bar);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

        setupToolBar();
        setupRecyclerView();
        setUpSwipeRefresh();

        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        if (navigationView != null) {
            setNavigationDrawer(navigationView);
        }

        mLocationEditText = (EditText) rootView.findViewById(R.id.location_edit_text);
        mLocationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mLocationEditText.clearFocus();
                    getTextDoSearch();
                }
                return false;
            }
        });

        mFloatingActionButton.setImageResource(R.drawable.ic_search);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.onActionViewExpanded();
                mSearchView.requestFocus();
                mSearchView.setIconified(false);
            }
        });

        //Handle orientation change
        if (savedInstanceState != null && mJobArrayList.size() != 0) {
            mJobArrayList = (ArrayList<Job>) savedInstanceState.getSerializable(KEY_SAVE_STATE);
            updateDisplay(mJobArrayList);
        } else {
            mJobArrayList = new ArrayList<>();
            mRecyclerView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void setupToolBar() {
        mToolbar.setTitle(getString(R.string.app_name));
        mAppCompatActivity = (AppCompatActivity) getActivity();
        mAppCompatActivity.setSupportActionBar(mToolbar);
        mAppCompatActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        mAppCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mAppCompatActivity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mAppCompatActivity, LinearLayoutManager.VERTICAL));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Reached bottom of recyclerView
                if (!recyclerView.canScrollVertically(1)) {
                    final int pos = mLayoutManager.findFirstVisibleItemPosition();

                    // Check if a full page of jobs was retrieved
                    if (mJobArrayList.size() % 50 == 0 && mJobArrayList.size() != 0) {

                        mHorizontalProgressBar.setVisibility(View.VISIBLE);
                        mPageCount++;
                        mAPI.getGHJobs(Integer.toString(mPageCount), mSearchView.getQuery().toString(), mLocationString, new Callback<ArrayList<Job>>() {
                            @Override
                            public void success(ArrayList<Job> jobs, Response response) {
                                Log.d(TAG, "Response URL: " + response.getUrl());
                                for (int i = 0; i < jobs.size(); i++) {
                                    if (!mHiddenJobList.contains(jobs.get(i).getId())) {
                                        mJobArrayList.add(jobs.get(i));
                                    }
                                }
                                mHorizontalProgressBar.setVisibility(View.GONE);
                                mJobAdapter = new JobAdapter(getActivity(), mJobArrayList);
                                mJobAdapter.notifyDataSetChanged();
                                recyclerView.setAdapter(mJobAdapter);
                                recyclerView.scrollToPosition(pos);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Snackbar.make(mCoordinatorLayout, "Please check network connection and try again", Snackbar.LENGTH_LONG).show();
                            }

                        });
                    }
                }
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int pos = viewHolder.getAdapterPosition();
                mSwipedJob = mJobArrayList.get(pos);
                mHiddenJobList.add(mJobArrayList.get(viewHolder.getAdapterPosition()).getId());
                Snackbar.make(mCoordinatorLayout, R.string.hide_job_sb, Snackbar.LENGTH_LONG).setAction(R.string.sb_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mHiddenJobList.remove(mHiddenJobList.size() - 1);
                        mJobArrayList.add(pos, mSwipedJob);
                        mJobAdapter.notifyItemInserted(pos);
                        mRecyclerView.scrollToPosition(pos);
                    }
                }).show();
                mJobArrayList.remove(pos);
                mJobAdapter.notifyItemRemoved(pos);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void setUpSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.md_blue_grey_700));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTextDoSearch();
            }
        });
    }

    private void setNavigationDrawer(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        onPause();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                mToolbar.setTitle(R.string.app_name);
                                mLocationEditText.setVisibility(View.VISIBLE);
                                mFloatingActionButton.setVisibility(View.VISIBLE);
                                mSwipeRefreshLayout.setEnabled(true);
                                getTextDoSearch();
                                menuItem.setChecked(true);
                                mDrawerLayout.closeDrawers();
                                return true;
                            case R.id.settings:
                                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                                startActivity(intent);
                                menuItem.setChecked(true);
                                mDrawerLayout.closeDrawers();
                                return true;
                            default:
                                mDrawerLayout.closeDrawers();
                                return true;
                        }

                    }
                });
    }

    private void getTextDoSearch() {
        if (!mJobArrayList.isEmpty()) {
            mJobArrayList.clear();
        }
        mLocationString = mLocationEditText.getText().toString();
        mJobDescriptionString = mSearchView.getQuery().toString();
        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
        searchJobTask(Integer.toString(0), mJobDescriptionString, mLocationString);
    }

    public void searchJobTask(String page, String search, String location) {
        mRecyclerView.stopScroll();
        mPageCount = 0;

        mAPI.getGHJobs(page, search, location, new Callback<ArrayList<Job>>() {
            @Override
            public void success(ArrayList<Job> jobs, Response response) {
                Log.i(TAG, "Response URL: " + response.getUrl());
                for (int i = 0; i < jobs.size(); i++) {
                    if (!mHiddenJobList.contains(jobs.get(i).getId())) {
                        mJobArrayList.add(jobs.get(i));
                    }
                }
                updateDisplay(mJobArrayList);
            }

            @Override
            public void failure(RetrofitError error) {
                Snackbar.make(mCoordinatorLayout, "Please check network connection and try again", Snackbar.LENGTH_LONG).show();
                updateDisplay(mJobArrayList);
            }
        });

    }

    private void updateDisplay(final ArrayList<Job> jobs) {

        if (!jobs.isEmpty()) {
            mLocationEditText.setVisibility(View.GONE);
            mJobAdapter = new JobAdapter(mAppCompatActivity, jobs);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mJobAdapter);
        } else {
            Snackbar.make(mCoordinatorLayout, "No jobs found", Snackbar.LENGTH_LONG).show();
        }

        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint(mAppCompatActivity.getString(R.string.search_description));
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mLocationEditText.setVisibility(View.VISIBLE);
            }
        });
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationEditText.setVisibility(View.VISIBLE);
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "Search view submit");
                mJobDescriptionString = query;
                mLocationEditText.requestFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_restore_jobs:
                mHiddenJobList.clear();
                getTextDoSearch();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
