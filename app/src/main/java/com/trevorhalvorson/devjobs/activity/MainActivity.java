package com.trevorhalvorson.devjobs.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.fragment.JobListFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new JobListFragment())
                    .commit();
        }

    }
}
