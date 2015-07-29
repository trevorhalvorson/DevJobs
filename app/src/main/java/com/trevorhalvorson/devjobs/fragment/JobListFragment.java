package com.trevorhalvorson.devjobs.fragment;


import android.content.Intent;
import android.os.Bundle;
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
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
    private static final String ENDPOINT = "https://jobs.github.com";
    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ArrayList<Job> jobList, savedJobs;
    private ArrayList<String> hiddenJobList, savedJobList;
    private JobAdapter adapter;
    private FloatingActionButton searchButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String searchJobDescText, searchLocText;
    private EditText searchDesc, searchLoc;
    private Job swipedJob;
    private AppCompatActivity activity;
    private DrawerLayout drawerLayout;

    public JobListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_job_list, container, false);

        jobList = new ArrayList<>();
        savedJobs = new ArrayList<>();
        hiddenJobList = new ArrayList<>();
        savedJobList = new ArrayList<>();
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        searchDesc = (EditText) rootView.findViewById(R.id.editDesc);
        searchLoc = (EditText) rootView.findViewById(R.id.editLoc);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.jobListView);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        searchButton = (FloatingActionButton) rootView.findViewById(R.id.searchButton);
        searchButton.setImageResource(R.drawable.ic_search);
        toolbar.setTitle(getString(R.string.app_name));
        activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        recyclerView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!jobList.isEmpty())
                    jobList.clear();
                searchJobDescText = searchDesc.getText().toString();
                searchLocText = searchLoc.getText().toString();
                searchJobTask(searchJobDescText, searchLocText);
            }
        });

        return rootView;
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.saved_jobs:
                                jobList.clear();
                                jobList.addAll(savedJobs);
                                recyclerView.setAdapter(adapter);
                                menuItem.setChecked(true);
                                drawerLayout.closeDrawers();
                                return true;
                            case R.id.nav_home:
                                jobList.clear();
                                recyclerView.setAdapter(adapter);
                                searchJobTask(searchJobDescText, searchLocText);
                                menuItem.setChecked(true);
                                drawerLayout.closeDrawers();
                                return true;
                            case R.id.settings:
                                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                                startActivity(intent);
                                menuItem.setChecked(true);
                                drawerLayout.closeDrawers();
                                return true;
                            default:
                                drawerLayout.closeDrawers();
                                return true;
                        }

                    }
                });
    }

    public void searchJobTask(String search, String location) {
        recyclerView.setVisibility(View.GONE);
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .build();

        GHJobsAPI api = adapter.create(GHJobsAPI.class);
        api.getGHJobs(search, location, new Callback<ArrayList<Job>>() {
            @Override
            public void success(ArrayList<Job> jobs, Response response) {
                for (int i = 0; i < jobs.size(); i++) {
                    if (!hiddenJobList.contains(jobs.get(i).getId()) &
                            !savedJobList.contains(jobs.get(i).getId())) {
                        jobList.add(jobs.get(i));
                    }
                }
                updateDisplay();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure: " + error.toString());
            }
        });
    }

    private void updateDisplay() {
        if (!jobList.isEmpty()) {
            setUpSwipeRefresh();
            adapter = new JobAdapter(getActivity(), jobList);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
            swipeRefreshLayout.setRefreshing(false);
        } else {
            Snackbar.make(coordinatorLayout, "No jobs found", Snackbar.LENGTH_LONG).show();
        }
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int pos = viewHolder.getAdapterPosition();
                swipedJob = jobList.get(pos);
                switch (direction) {
                    // Save Job
                    case ItemTouchHelper.LEFT:
                        savedJobList.add(jobList.get(viewHolder.getAdapterPosition()).getId());
                        savedJobs.add(swipedJob);
                        Snackbar.make(coordinatorLayout, R.string.saved_job_sb, Snackbar.LENGTH_LONG).setAction(R.string.sb_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                savedJobList.remove(savedJobList.size() - 1);
                                savedJobs.remove(swipedJob);
                                jobList.add(pos, swipedJob);
                                adapter.notifyItemInserted(pos);
                                recyclerView.scrollToPosition(pos);
                            }
                        }).show();
                        break;
                    //Hide Job
                    case ItemTouchHelper.RIGHT:
                        hiddenJobList.add(jobList.get(viewHolder.getAdapterPosition()).getId());
                        Snackbar.make(coordinatorLayout, R.string.hide_job_sb, Snackbar.LENGTH_LONG).setAction(R.string.sb_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hiddenJobList.remove(hiddenJobList.size() - 1);
                                jobList.add(pos, swipedJob);
                                adapter.notifyItemInserted(pos);
                                recyclerView.scrollToPosition(pos);
                            }
                        }).show();
                        break;
                }
                jobList.remove(pos);
                adapter.notifyItemRemoved(pos);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setUpSwipeRefresh() {
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.md_blue_grey_700));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                jobList.clear();
                searchJobTask(searchJobDescText, searchLocText);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_restore_jobs:
                jobList.clear();
                hiddenJobList.clear();
                searchJobTask(searchJobDescText, searchLocText);
                return true;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
