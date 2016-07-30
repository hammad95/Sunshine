package com.example.android.sunshine.app;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Hassan on 7/30/2016.
 */
public class LocationEditTextPreference extends EditTextPreference {

    public static final int DEFAULT_MIN_LOCATION_LENGTH = 3;
    private int mMinLength;

    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Get the styled attribute for LocationEditTextPreference
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0,
                0
        );
        try {
            mMinLength = ta.getInt(R.styleable.LocationEditTextPreference_minLength,
                    DEFAULT_MIN_LOCATION_LENGTH);
        } finally {
            ta.recycle();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        // Get the edit text field inside this preference
        EditText et = getEditText();
        et.addTextChangedListener(new TextWatcher() {   // add a text change listener
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            // After text has changed
            @Override
            public void afterTextChanged(Editable s) {
                Dialog d = getDialog();                     // get the dialog
                if(d instanceof AlertDialog) {              // if dialog is alert dialog,
                    AlertDialog ad = (AlertDialog) d;       // cast it to AlertDialog
                    Button positiveButton = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                    if(s.length() < mMinLength)             // if length less than desired
                        positiveButton.setEnabled(false);   // disable button
                    else
                        positiveButton.setEnabled(true);    // else, enable buton
                }
            }
        });
    }
}
