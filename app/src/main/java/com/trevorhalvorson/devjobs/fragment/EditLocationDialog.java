package com.trevorhalvorson.devjobs.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.trevorhalvorson.devjobs.R;

/**
 * Created by Trevor on 9/6/2015.
 */
public class EditLocationDialog extends DialogFragment implements TextView.OnEditorActionListener {

    public interface EditLocationDialogListener {
        void onFinishEditDialog(String inputText);
    }

    private EditText mEditText;

    public EditLocationDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_location, container);
        mEditText = (EditText) view.findViewById(R.id.location_edit_text);
        getDialog().setTitle(R.string.location);
        mEditText.requestFocus();
        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            EditLocationDialogListener activity = (EditLocationDialogListener) getActivity();
            activity.onFinishEditDialog(mEditText.getText().toString());
            this.dismiss();
            return true;
        }
        return false;
    }
}