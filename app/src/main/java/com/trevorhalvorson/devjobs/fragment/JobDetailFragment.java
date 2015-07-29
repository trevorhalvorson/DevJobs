package com.trevorhalvorson.devjobs.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Button;
import android.widget.TextView;

import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.activity.JobDetailActivity;


public class JobDetailFragment extends Fragment {
    private static final String TAG = JobDetailFragment.class.getSimpleName();
    private SharedPreferences prefs;
    private Bundle extras;
    private boolean webViewPref;
    private Button webButton;
    private TextView jobDescription;
    private Toolbar toolbar;
    private AppCompatActivity activity;
    private ShareActionProvider shareActionProvider;
    private String jobUrl;

    public JobDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        webViewPref = prefs.getBoolean(getString(R.string.wv_key), true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_job_detail, container, false);
        webButton = (Button) rootView.findViewById(R.id.webButton);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        jobDescription = (TextView) rootView.findViewById(R.id.jobDescriptionTextView);

        extras = getActivity().getIntent().getExtras();

        activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        activity.getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(extras.getString(JobDetailActivity.EXTRA_TITLE));
        jobDescription.setText(Html.fromHtml(extras.getString(JobDetailActivity.EXTRA_DESCRIPTION)));
        jobUrl = extras.getString(JobDetailActivity.EXTRA_URL);
        webButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!webViewPref) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(extras.getString(JobDetailActivity.EXTRA_URL)));
                    startActivity(intent);
                } else {
                    getFragmentManager().beginTransaction().replace(R.id.detailContainer,
                            new JobWebView()).addToBackStack(null).commit();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (jobUrl != null) {
            shareActionProvider.setShareIntent(shareIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (activity.getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    activity.getSupportFragmentManager().popBackStack();
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, jobUrl);
        return shareIntent;
    }
}
