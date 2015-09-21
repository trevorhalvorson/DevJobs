package com.trevorhalvorson.devjobs.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.trevorhalvorson.devjobs.DividerItemDecoration;
import com.trevorhalvorson.devjobs.GHJobsAPI;
import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.activity.SettingsActivity;
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


public class JobListFragment extends StatedFragment implements EditLocationDialog.EditLocationDialogListener {

    private static final String TAG = JobListFragment.class.getSimpleName();
    private static final String KEY_SAVE_STATE = "KEY_JOB_LIST";
    private static final String ENDPOINT = "https://jobs.github.com";
    private SearchView mSearchView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mRecyclerView;
    private ArrayList<Job> mJobArrayList;
    private ArrayList<String> mSavedSearches;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
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

        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);

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
                getResources().getColor(R.color.md_blue_grey_700));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTextDoSearch();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_save_white_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mJobDescriptionString.equals("")) {
                    mSavedSearches.add(mJobDescriptionString);
                } else {
                    Snackbar.make(mCoordinatorLayout, R.string.empty_search_text, Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });


        //Handle orientation change
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
                        mPageCount++;
                        mAPI.getGHJobs(Integer.toString(mPageCount), mSearchView.getQuery().toString(), "" /*mLocationString*/, new Callback<ArrayList<Job>>() {
                            @Override
                            public void success(ArrayList<Job> jobs, Response response) {
                                Log.d(TAG, "Response URL: " + response.getUrl());
                                mJobArrayList.addAll(jobs);
                                mJobAdapter = new Adapter(mJobArrayList);
                                mJobAdapter.notifyDataSetChanged();
                                recyclerView.setAdapter(mJobAdapter);
                                recyclerView.scrollToPosition(pos);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Snackbar.make(mCoordinatorLayout, R.string.retrofit_error_text, Snackbar.LENGTH_LONG).show();
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
        searchJobTask(Integer.toString(0), mJobDescriptionString, mLocationString);
    }

    public void searchJobTask(String page, String search, String location) {
        mRecyclerView.stopScroll();
        mPageCount = 0;
        Log.i(TAG, "searchJobTask ");

        mAPI.getGHJobs(page, search, location, new Callback<ArrayList<Job>>() {
            @Override
            public void success(ArrayList<Job> jobs, Response response) {
                Log.i(TAG, "success ");
                mJobArrayList.addAll(jobs);
                updateDisplay(mJobArrayList);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "failure " + error);
                Snackbar.make(mCoordinatorLayout, R.string.retrofit_error_text, Snackbar.LENGTH_LONG).show();
                updateDisplay(mJobArrayList);
            }
        });

    }

    private void updateDisplay(final ArrayList<Job> jobs) {
        if (!jobs.isEmpty()) {
            Log.i(TAG, "updateDisplay ");
            mJobAdapter = new Adapter(jobs);
            mRecyclerView.setAdapter(mJobAdapter);
            setupScrollListenerRecyclerView();
        } else {
            Snackbar.make(mCoordinatorLayout, R.string.no_jobs_text, Snackbar.LENGTH_LONG).show();
        }

        mSwipeRefreshLayout.setRefreshing(false);

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
                searchJobTask("0", mJobDescriptionString, mLocationString);
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
                EditLocationDialog dialog = new EditLocationDialog();
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
    public void onFinishEditDialog(String inputText) {
        mLocationString = inputText;
        Log.i(TAG, "onFinishEditDialog " + mLocationString);
    }
}
