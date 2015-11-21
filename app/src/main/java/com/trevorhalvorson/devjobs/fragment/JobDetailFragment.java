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
import android.util.Log;
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

    private boolean mWebViewPref;
    private AppCompatActivity mActivity;
    private String mJobUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mWebViewPref = prefs.getBoolean(getString(R.string.wv_key), true);
        Log.i("TAG", "onCreate " + mWebViewPref);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_job_detail, container, false);

        Job job = (Job) getArguments().getSerializable(ARG_JOB_KEY);
        mJobUrl = job.getUrl();

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.detail_toolbar);
        TextView descriptionTextView = (TextView) rootView.findViewById(R.id.description_text_view);
        mActivity = (AppCompatActivity) getActivity();
        mActivity.setSupportActionBar(toolbar);
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActivity.getSupportActionBar().setHomeButtonEnabled(true);
        mActivity.getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        mActivity.getSupportActionBar().setTitle(job.getTitle());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        descriptionTextView.setText(Html.fromHtml(job.getDescription()));

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
                if (mActivity.getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    mActivity.getSupportFragmentManager().popBackStack();
                } else {
                    getActivity().onBackPressed();
                }
                return true;
            case R.id.menu_item_web:
                if (!mWebViewPref) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(mJobUrl));
                    startActivity(intent);
                } else {
                    Fragment jobWebViewFragment = JobWebViewFragment.newInstance(mJobUrl);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.viewpager, jobWebViewFragment)
                            .commit();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Intent shareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mJobUrl);
        return shareIntent;
    }
}
