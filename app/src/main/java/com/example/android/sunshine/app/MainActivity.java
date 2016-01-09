package com.example.android.sunshine.app;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(LOG_TAG, "############## onCreate() called ####################");

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }

        // Get Support ActionBar
        ActionBar actionBar = getSupportActionBar();

        // Set home button to display app icon in ActionBar
        ///////////****************DOESN'T WORK*******************//////////////////
        if(actionBar != null) {
            actionBar.setLogo(R.drawable.ic_launcher);
            actionBar.setDisplayUseLogoEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v(LOG_TAG, "############## onStart() called ####################");

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(LOG_TAG, "############## onResume() called ####################");

    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v(LOG_TAG, "############## onPause() called ####################");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.v(LOG_TAG, "############## onStop() called ####################");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.v(LOG_TAG, "############## onDestroy() called ####################");
    }
}
