package com.trevorhalvorson.devjobs;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.trevorhalvorson.devjobs.model.Job;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class JobListFragment extends Fragment {

    private static final String TAG = JobListFragment.class.getSimpleName();
    private static final String ENDPOINT = "https://jobs.github.com";
    private RecyclerView recyclerView;
    private ArrayList<Job> jobList;
    private JobAdapter adapter;
    private Button searchButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String searchJobDescText, searchLocText;
    private EditText searchDesc, searchLoc;

    public JobListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_job_list, container, false);

        searchDesc = (EditText) rootView.findViewById(R.id.editDesc);
        searchLoc = (EditText) rootView.findViewById(R.id.editLoc);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.jobListView);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        searchButton = (Button) rootView.findViewById(R.id.searchButton);

        recyclerView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchJobDescText = searchDesc.getText().toString();
                searchLocText = searchLoc.getText().toString();
                searchJobTask(searchJobDescText, searchLocText);
            }
        });

        return rootView;
    }


    public void searchJobTask(String search, String location) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .build();

        GHJobsAPI api = adapter.create(GHJobsAPI.class);
        api.getGHJobs(search, location, new Callback<ArrayList<Job>>() {
            @Override
            public void success(ArrayList<Job> jobs, Response response) {
                jobList = jobs;
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
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setColorSchemeColors(
                    Color.BLUE, Color.RED, Color.BLACK);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshJobs();
                }
            });
            adapter = new JobAdapter(getActivity(), jobList);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            Toast.makeText(getActivity(), "Sorry, no jobs found", Toast.LENGTH_SHORT).show();
        }

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                jobList.remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        //TODO: Save job to device using SQLite
                        break;
                    case ItemTouchHelper.RIGHT:
                        //TODO: Remove job from current and future searches using jobId
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void refreshJobs() {
        searchJobTask(searchJobDescText, searchLocText);
        swipeRefreshLayout.setRefreshing(false);
    }

}
