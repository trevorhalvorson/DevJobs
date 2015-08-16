package com.trevorhalvorson.devjobs.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.activity.JobDetailActivity;
import com.trevorhalvorson.devjobs.model.Job;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
    private static final String TAG = JobAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private ArrayList<Job> mJobs = new ArrayList<>();

    public JobAdapter(Context context, ArrayList<Job> jobs) {
        mLayoutInflater = LayoutInflater.from(context);
        this.mJobs = jobs;
    }

    @Override
    public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.job_row, parent, false);
        JobViewHolder holder = new JobViewHolder(view);
        mContext = parent.getContext();

        return holder;
    }

    @Override
    public void onBindViewHolder(final JobViewHolder holder, final int position) {
        final Job currentJob = mJobs.get(position);
        holder.itemView.isClickable();
        holder.jobTitle.setText(currentJob.getTitle());
        holder.jobCompany.setText(currentJob.getCompany());
        holder.jobLocation.setText(currentJob.getLocation());
        holder.jobDate.setText(getDateSpan(currentJob.getCreated_at()).toString());
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, JobDetailActivity.class);
                intent.putExtra(JobDetailActivity.EXTRA_TITLE, currentJob.getTitle());
                intent.putExtra(JobDetailActivity.EXTRA_DESCRIPTION, currentJob.getDescription());
                intent.putExtra(JobDetailActivity.EXTRA_COMPANY, currentJob.getCompany());
                intent.putExtra(JobDetailActivity.EXTRA_LOCATION, currentJob.getLocation());
                intent.putExtra(JobDetailActivity.EXTRA_DATE, getDateSpan(currentJob.getCreated_at()));
                intent.putExtra(JobDetailActivity.EXTRA_URL, currentJob.getUrl());
                intent.putExtra(JobDetailActivity.EXTRA_ID, currentJob.getId());
                mContext.startActivity(intent);
            }


        });
    }

    @Override
    public int getItemCount() {
        return mJobs.size();
    }

    class JobViewHolder extends RecyclerView.ViewHolder {

        private TextView jobTitle, jobCompany, jobLocation, jobDate;

        public JobViewHolder(View itemView) {
            super(itemView);

            jobTitle = (TextView) itemView.findViewById(R.id.jobTitleTextView);
            jobCompany = (TextView) itemView.findViewById(R.id.jobCompanyTextView);
            jobLocation = (TextView) itemView.findViewById(R.id.jobLocationTextView);
            jobDate = (TextView) itemView.findViewById(R.id.jobDateTextView);
        }
    }

    private CharSequence getDateSpan(String dateCreatedAtStr) {
        final DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
        final Date date;
        try {
            date = dateFormat.parse(dateCreatedAtStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        if (date.after(new Date())) {
            return mContext.getString(R.string.just_posted);
        }
        return DateUtils.getRelativeTimeSpanString(date.getTime());
    }

}
