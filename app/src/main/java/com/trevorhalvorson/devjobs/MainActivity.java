package com.trevorhalvorson.devjobs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private LinearLayout searchView;
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
        searchView = (LinearLayout) findViewById(R.id.linearLayout);
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
            searchView.setVisibility(View.GONE);
        } else {
            Toast.makeText(MainActivity.this, "Sorry, no jobs found", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                searchView.setVisibility(View.VISIBLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
