package com.trevorhalvorson.devjobs;

import com.google.gson.annotations.SerializedName;
import com.trevorhalvorson.devjobs.model.Job;

import java.util.ArrayList;

/**
 * Created by Trevor on 7/8/2015.
 */
public class JobData {
    @SerializedName("id")
    private String id;
    @SerializedName("create_at")
    private String created_at;
    @SerializedName("title")
    private String title;
    @SerializedName("location")
    private String location;
    @SerializedName("type")
    private String type;
    @SerializedName("description")
    private String description;
    private final ArrayList<Job> jobs;

    public JobData(ArrayList<Job> jobs, String id, String created_at, String title,
                   String location, String type, String description) {
        this.jobs = jobs;
        this.id = id;
        this.created_at = created_at;
        this.title = title;
        this.location = location;
        this.type = type;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Job> getJobs() {
        return jobs;
    }
}
