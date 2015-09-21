package com.trevorhalvorson.devjobs.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.model.Job;


public class JobDetailFragment extends Fragment {
    private static final String ARG_JOB_KEY = "job_key";


    public static JobDetailFragment newInstance(Job job) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_JOB_KEY, job);

        JobDetailFragment fragment = new JobDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private boolean webViewPref;
    private FloatingActionButton mFloatingActionButton;
    private TextView mDescriptionTextView;
    private Toolbar mToolbar;
    private AppCompatActivity mAppCompatActivity;
    private String mJobUrl;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        webViewPref = prefs.getBoolean(getString(R.string.wv_key), true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_job_detail, container, false);

        final Job job = (Job) getArguments().getSerializable(ARG_JOB_KEY);

        mFloatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mDescriptionTextView = (TextView) rootView.findViewById(R.id.description_text_view);
        mAppCompatActivity = (AppCompatActivity) getActivity();
        mAppCompatActivity.setSupportActionBar(mToolbar);
        mAppCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAppCompatActivity.getSupportActionBar().setHomeButtonEnabled(true);
        mAppCompatActivity.getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        mAppCompatActivity.getSupportActionBar().setTitle(job.getTitle());
        mDescriptionTextView.setText(Html.fromHtml(job.getDescription()));
        mJobUrl = job.getUrl();
        mFloatingActionButton.setImageResource(R.drawable.ic_web);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!webViewPref) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(job.getUrl()));
                    startActivity(intent);
                } else {
                    Fragment jobWebViewFragment = JobWebViewFragment.newInstance(mJobUrl);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, jobWebViewFragment)
                            .commit();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mJobUrl != null) {
            shareActionProvider.setShareIntent(shareIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mAppCompatActivity.getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    mAppCompatActivity.getSupportFragmentManager().popBackStack();
                } else {
                    getActivity().onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent shareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mJobUrl);
        return shareIntent;
    }
}
