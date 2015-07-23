package com.trevorhalvorson.devjobs;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class JobDetailFragment extends Fragment {
    private static final String TAG = JobDetailFragment.class.getSimpleName();

    public JobDetailFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_job_detail, container, false);
        final Bundle extras = getActivity().getIntent().getExtras();
        Button goToJobBtn = (Button) rootView.findViewById(R.id.goToJobButton);
        goToJobBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(extras.getString(JobDetailActivity.EXTRA_URL)));
                startActivity(intent);
            }
        });
        TextView tv = (TextView) rootView.findViewById(R.id.jobDescriptionTextView);
        tv.setText(Html.fromHtml(extras.getString(JobDetailActivity.EXTRA_DESCRIPTION)));

        return rootView;
    }


}
