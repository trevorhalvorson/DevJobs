package com.trevorhalvorson.devjobs.activity;

import android.support.v4.app.Fragment;

import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.fragment.JobDetailFragment;
import com.trevorhalvorson.devjobs.fragment.JobListFragment;
import com.trevorhalvorson.devjobs.model.Job;

public class JobListActivity extends SingleFragmentActivity
        implements JobListFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new JobListFragment();
    }

    @Override
    public void onJobSelected(Job job) {
        Fragment jobDetailFragment = JobDetailFragment.newInstance(job);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, jobDetailFragment)
                .commit();
    }
}
