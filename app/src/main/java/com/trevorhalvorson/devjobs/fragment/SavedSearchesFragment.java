package com.trevorhalvorson.devjobs.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.trevorhalvorson.devjobs.R;
import com.trevorhalvorson.devjobs.model.Search;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Trevor Halvorson on 9/21/2015.
 */
public class SavedSearchesFragment extends Fragment
        implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private static final String SAVED_SEARCHES_KEY = "saved_searches_key";
    private static final String NUM_SAVED_SEARCHES_KEY = "num_saved_searches_key";

    public interface SavedSearchSelectedListener {
        void onSearchSelected(Search savedSearch);
    }

    public static void setListener(SavedSearchSelectedListener listener) {
        mListener = listener;
    }

    private static SavedSearchSelectedListener mListener;
    private ListView mListView;
    private List<Search> mSearchList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_saved_searches, container, false);

        mListView = (ListView) view.findViewById(R.id.list);
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();
        for (int i = 0; i < preferences.getInt(NUM_SAVED_SEARCHES_KEY, 0); i++) {
            String json = preferences.getString(SAVED_SEARCHES_KEY + i, "");
            Search search = gson.fromJson(json, Search.class);
            mSearchList.add(search);
        }

        setAdapter();

        mListView.setOnItemClickListener(this);
        return view;
    }

    private void setAdapter() {
        ArrayAdapter<Search> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, mSearchList);

        mListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onSearchSelected(mSearchList.get(position));

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        removeSearch(mSearchList.get(position));
        mSearchList.remove(position);
        setAdapter();
        return true;
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
}
