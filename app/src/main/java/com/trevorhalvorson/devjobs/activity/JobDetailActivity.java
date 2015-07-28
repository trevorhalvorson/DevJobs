package com.trevorhalvorson.devjobs.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.fragment.JobDetailFragment;

public class JobDetailActivity extends AppCompatActivity {
    private static final String TAG = JobDetailActivity.class.getSimpleName();
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_COMPANY = "company";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobdetail);

        Bundle extras = getIntent().getExtras();

        if (savedInstanceState == null) {
            JobDetailFragment fragment = new JobDetailFragment();
            fragment.setArguments(extras);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
