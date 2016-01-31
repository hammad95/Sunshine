package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    // String to store the current location
    private String mLocation;

    // Fragment tag for the forecast fragment
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    // To figure out whether or not to display the two-pane mode
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize mLocation to store the current location from preferences
        mLocation = Utility.getPreferredLocation(this);

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.container_content_detail) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_content_detail, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
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
        // If location was changed by the user, get the ForecastFragment and
        // call onLocationChanged, then set the new location to mLocation
        String userLocation = Utility.getPreferredLocation(this);

        if(userLocation != null && !userLocation.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().
                    findFragmentById(R.id.fragment_forecast);
            if(ff != null)
                ff.onLocationChanged();

            // Set mLocation to store the new location
            mLocation = userLocation;
        }

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

    @Override
    public void onItemSelected(Uri contentUri) {
        if(mTwoPane) {
            // In two-pane mode, replace the preexisting DetailActivityFragment
            // using a FragmentTransaction
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);

            DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
            detailActivityFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().
                    replace(R.id.container_content_detail, detailActivityFragment, DETAILFRAGMENT_TAG).
                    commit();
        }
        else {
            Intent detailActivityIntent = new Intent(this, DetailActivity.class).
                    setData(contentUri);
            startActivity(detailActivityIntent);
        }
    }
}
