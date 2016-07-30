package com.example.android.sunshine.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

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
}
