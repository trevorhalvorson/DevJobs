package com.trevorhalvorson.devjobs.fragment;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.trevorhalvorson.devjobs.DividerItemDecoration;
import com.trevorhalvorson.devjobs.GHJobsAPI;
import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.model.Job;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class JobListFragment extends StatedFragment
        implements EditLocationDialog.EditLocationDialogListener,
        SavedSearchesDialog.SavedSearchSelectedListener {

    private static final String TAG = JobListFragment.class.getSimpleName();
    private static final String KEY_SAVE_STATE = "KEY_JOB_LIST";
    private static final String ENDPOINT = "https://jobs.github.com";
    private SearchView mSearchView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private ProgressBar mProgressBarMargin, mProgressBar;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mRecyclerView;
    private ArrayList<Job> mJobArrayList;
    private ArrayList<String> mSavedSearches;
    private SharedPreferences mSharedPreferences;
    private Adapter mJobAdapter;
    private String mJobDescriptionString = "";
    private String mLocationString = "";
    private GHJobsAPI mAPI;
    private int mPageCount;
    private LinearLayoutManager mLayoutManager;
    private Callbacks mCallbacks;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onJobSelected(Job job);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    protected void onSaveState(Bundle outState) {
        super.onSaveState(outState);
        outState.putSerializable(KEY_SAVE_STATE, mJobArrayList);
    }

    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        super.onRestoreState(savedInstanceState);
        mJobArrayList = (ArrayList<Job>) savedInstanceState.getSerializable(KEY_SAVE_STATE);
        mJobAdapter = new Adapter(mJobArrayList);
        mRecyclerView.setAdapter(mJobAdapter);
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
        for (int i = 0; i < mSavedSearches.size(); i++) {
            editor.putString(String.valueOf(i), mSavedSearches.get(i));
        }
        editor.putInt("saved_searches_size", mSavedSearches.size());
        editor.apply();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        EditLocationDialog.setListener(this);
        SavedSearchesDialog.setListener(this);

        mSavedSearches = new ArrayList<>();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int savedSearchesSize = mSharedPreferences.getInt("saved_searches_size", 0);
        for (int i = 0; i < savedSearchesSize; i++) {
            mSavedSearches.add(i, mSharedPreferences.getString(String.valueOf(i), ""));
        }

        RestAdapter mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .build();
        mAPI = mRestAdapter.create(GHJobsAPI.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_job_list, container, false);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar_main);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_white_24dp));

        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);

        mProgressBarMargin = (ProgressBar) rootView.findViewById(R.id.job_list_progress_bar_margin);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.job_list_progress_bar);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.jobs_recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        if (navigationView != null) {
            setNavigationDrawer(navigationView);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.primary_dark));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearJobs();
                searchJobTask(Integer.toString(0), mJobDescriptionString, mLocationString);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_save_white_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mJobDescriptionString.equals("") && !mSavedSearches.contains(mJobDescriptionString)) {
                    mSavedSearches.add(mJobDescriptionString);
                    showSnackbar("\"" + mJobDescriptionString + "\"" + " added to saved searches.");
                } else if (mSavedSearches.contains(mJobDescriptionString)) {
                    showSnackbar("\"" + mJobDescriptionString + "\"" + " already added to saved searches.");
                } else {
                    showSnackbar(getString(R.string.empty_search_text));
                }
            }
        });

        if (savedInstanceState != null && mJobArrayList.size() != 0) {
            mJobArrayList = (ArrayList<Job>) savedInstanceState.getSerializable(KEY_SAVE_STATE);
            updateDisplay(mJobArrayList);
        } else {
            mJobArrayList = new ArrayList<>();
        }

        return rootView;
    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Job mJob;
        private TextView jobTitle, jobCompany, jobLocation, jobDate;

        public Holder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            jobTitle = (TextView) itemView.findViewById(R.id.jobTitleTextView);
            jobCompany = (TextView) itemView.findViewById(R.id.jobCompanyTextView);
            jobLocation = (TextView) itemView.findViewById(R.id.jobLocationTextView);
            jobDate = (TextView) itemView.findViewById(R.id.jobDateTextView);
        }

        public void bindJob(Job job) {
            mJob = job;
            jobTitle.setText(mJob.getTitle());
            jobCompany.setText(mJob.getCompany());
            jobLocation.setText(mJob.getLocation());
            jobDate.setText(getDateSpan(mJob.getCreated_at()).toString());
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onJobSelected(mJob);
        }
    }

    private class Adapter extends RecyclerView.Adapter<Holder> {

        private ArrayList<Job> mJobs = new ArrayList<>();

        public Adapter(ArrayList<Job> jobs) {
            mJobs = jobs;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_job, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            Job job = mJobs.get(position);
            holder.bindJob(job);
        }

        @Override
        public int getItemCount() {
            return mJobs.size();
        }
    }

    private CharSequence getDateSpan(String dateCreatedAtStr) {
        final DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
        final Date date;
        try {
            date = dateFormat.parse(dateCreatedAtStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        if (date.after(new Date())) {
            return getActivity().getString(R.string.just_posted);
        }
        return DateUtils.getRelativeTimeSpanString(date.getTime());
    }

    private void clearJobs() {
        if (!mJobArrayList.isEmpty()) {
            mJobArrayList.clear();
        }

    }

    public void searchJobTask(String page, String search, String location) {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mProgressBarMargin.setVisibility(View.VISIBLE);
        }

        mRecyclerView.stopScroll();
        mPageCount = 0;

        mAPI.getGHJobs(page, search, location, new Callback<ArrayList<Job>>() {
            @Override
            public void success(ArrayList<Job> jobs, Response response) {
                mJobArrayList.addAll(jobs);
                updateDisplay(mJobArrayList);
            }

            @Override
            public void failure(RetrofitError error) {
                showSnackbar(getString(R.string.retrofit_error_text));
                updateDisplay(mJobArrayList);
            }
        });

    }

    private void updateDisplay(final ArrayList<Job> jobs) {
        if (!jobs.isEmpty()) {
            mJobAdapter = new Adapter(jobs);
            mRecyclerView.setAdapter(mJobAdapter);
            setupScrollListenerRecyclerView();
        } else {
            showSnackbar(getString(R.string.no_jobs_text));
        }

        mProgressBarMargin.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);

    }

    private void setupScrollListenerRecyclerView() {
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
                        mProgressBar.setVisibility(View.VISIBLE);

                        mPageCount++;
                        mAPI.getGHJobs(Integer.toString(mPageCount), mSearchView.getQuery().toString(), mLocationString, new Callback<ArrayList<Job>>() {
                            @Override
                            public void success(ArrayList<Job> jobs, Response response) {
                                mJobArrayList.addAll(jobs);
                                mJobAdapter = new Adapter(mJobArrayList);
                                mJobAdapter.notifyDataSetChanged();
                                recyclerView.setAdapter(mJobAdapter);
                                recyclerView.scrollToPosition(pos);
                                mProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                showSnackbar(getString(R.string.retrofit_error_text));
                                mProgressBar.setVisibility(View.GONE);
                            }

                        });
                    }
                }
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
                                menuItem.setChecked(true);
                                mDrawerLayout.closeDrawers();
                                mRecyclerView.smoothScrollToPosition(0);
                                return true;
                            case R.id.nav_searches:
                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                SavedSearchesDialog dialog = SavedSearchesDialog.newInstance(mSavedSearches);
                                dialog.show(fm, "fragment_saved_searches");
                                mDrawerLayout.closeDrawers();
                                return true;
                            case R.id.settings:
                                SettingsFragment settingsFragment = SettingsFragment.newInstance();
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .addToBackStack(null)
                                        .replace(R.id.fragment_container, settingsFragment)
                                        .commit();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint(getActivity().getString(R.string.search_description));
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                mJobDescriptionString = query;
                clearJobs();
                searchJobTask(Integer.toString(0), mJobDescriptionString, mLocationString);
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
            case R.id.action_set_location:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                EditLocationDialog dialog = EditLocationDialog.newInstance();
                dialog.show(fm, "fragment_edit_location");
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSearchSelected(String savedSearch) {
        mJobDescriptionString = savedSearch;
        clearJobs();
        searchJobTask("0", savedSearch, mLocationString);
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        mLocationString = inputText;
        showSnackbar("Location set to \"" + mLocationString + "\"");
    }

    private void showSnackbar(String text) {
        Snackbar.make(mCoordinatorLayout, text, Snackbar.LENGTH_LONG).show();
    }
}
