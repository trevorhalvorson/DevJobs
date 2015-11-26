package com.trevorhalvorson.devjobs.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.trevorhalvorson.devjobs.DividerItemDecoration;
import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.activity.MainActivity;
import com.trevorhalvorson.devjobs.model.Search;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Trevor Halvorson on 9/21/2015.
 */
public class SavedSearchesFragment extends Fragment
        implements MainActivity.AddSearchListener {
    private static final String TAG = SavedSearchesFragment.class.getSimpleName();

    private static final String SAVED_SEARCHES_KEY = "saved_searches_key";
    private static final String NUM_SAVED_SEARCHES_KEY = "num_saved_searches_key";

    private ViewPager mViewPager;

    public SavedSearchesFragment(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    public interface SavedSearchSelectedListener {
        void onSearchSelected(Search savedSearch);
    }

    public static void setListener(SavedSearchSelectedListener listener) {
        mListener = listener;
    }

    private static SavedSearchSelectedListener mListener;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private List<Search> mSearchList = new ArrayList<>();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity.setAddSearchListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_saved_searches, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.searchRecyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        setupAdapter();

        return view;
    }

    private void setupAdapter() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();
        for (int i = 0; i < preferences.getInt(NUM_SAVED_SEARCHES_KEY, 0); i++) {
            String json = preferences.getString(SAVED_SEARCHES_KEY + i, "");
            Search search = gson.fromJson(json, Search.class);
            mSearchList.add(search);
        }
        mAdapter = new Adapter(mSearchList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void removeSearch(Search searchToRemove) {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();

        for (int i = 0; i < preferences.getInt(NUM_SAVED_SEARCHES_KEY, 0); i++) {
            String json = preferences.getString(SAVED_SEARCHES_KEY + i, "");
            Search search = gson.fromJson(json, Search.class);
            if (search.equals(searchToRemove)) {
                preferences.edit().remove(preferences.getString(SAVED_SEARCHES_KEY + i, "")).apply();
            }
        }
    }

    @Override
    public void addSearch() {
        setupAdapter();
    }

    private class Holder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private Search mSearch;
        private TextView mDescTextView;
        private TextView mLocTextView;

        public Holder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mDescTextView = (TextView) itemView.findViewById(R.id.searchDescriptionTextView);
            mLocTextView = (TextView) itemView.findViewById(R.id.searchLocationTextView);
        }

        public void bindSearch(Search search) {
            mSearch = search;
            mDescTextView.setText(mSearch.getDescription());
            if (mSearch.getLocation() != null) {
                mLocTextView.setText(mSearch.getLocation());
            }

        }

        @Override
        public void onClick(View v) {
            mViewPager.setCurrentItem(0, true);
            mListener.onSearchSelected(mSearch);
        }
    }

    private class Adapter extends RecyclerView.Adapter<Holder> {

        private List<Search> mSearches = new ArrayList<>();

        public Adapter(List<Search> searches) {
            mSearches = searches;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_search, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            Search search = mSearches.get(position);
            holder.bindSearch(search);
        }

        @Override
        public int getItemCount() {
            return mSearches.size();
        }
    }
}
