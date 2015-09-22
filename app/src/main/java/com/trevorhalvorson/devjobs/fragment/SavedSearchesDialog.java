package com.trevorhalvorson.devjobs.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.trevorhalvorson.devjobs.R;

import java.util.ArrayList;

/**
 * Created by trevo on 9/21/2015.
 */
public class SavedSearchesDialog extends DialogFragment
        implements AdapterView.OnItemClickListener {
    private static final String SAVED_SEARCHES_KEY = "saved_searches_key";

    public static SavedSearchesDialog newInstance(ArrayList<String> strings) {
        Bundle args = new Bundle();
        args.putStringArrayList(SAVED_SEARCHES_KEY, strings);

        SavedSearchesDialog fragment = new SavedSearchesDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public interface SavedSearchSelectedListener {
        void onSearchSelected(String savedSearch);
    }

    public static void setListener(SavedSearchSelectedListener listener) {
        mListener = listener;
    }

    private static SavedSearchSelectedListener mListener;
    private ListView mListView;
    private ArrayList<String> mStringArrayList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_saved_searches, container, false);

        mListView = (ListView) view.findViewById(R.id.list);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mStringArrayList = getArguments().getStringArrayList(SAVED_SEARCHES_KEY);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, mStringArrayList);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onSearchSelected(mStringArrayList.get(position));
        dismiss();
    }
}
