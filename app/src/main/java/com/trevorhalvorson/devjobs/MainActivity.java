package com.trevorhalvorson.devjobs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.trevorhalvorson.devjobs.model.Job;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ENDPOINT = "https://jobs.github.com";
    private RecyclerView recyclerView;
    private ArrayList<Job> jobList;
    private JobAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText searchDesc = (EditText) findViewById(R.id.editDesc);
        final EditText searchLoc = (EditText) findViewById(R.id.editLoc);
        recyclerView = (RecyclerView) findViewById(R.id.jobListView);
        recyclerView.setVisibility(View.GONE);
        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchJobDesc = searchDesc.getText().toString();
                String searchLocText = searchLoc.getText().toString();
                searchJobTask(searchJobDesc, searchLocText);
            }
        });

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
            adapter = new JobAdapter(getApplicationContext(), jobList);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            Toast.makeText(MainActivity.this, "Sorry, no jobs found", Toast.LENGTH_SHORT).show();
        }

    }


}
