package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.android.sunshine.app.gcm.RegistrationIntentService;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

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
            getSupportActionBar().setElevation(0f); // To eliminate the shadow of the ActionBar
                                                    // on the ForecastFragment in phones
        }

        // Notify ForecastFragment to show the special
        // today list item in the layout if not in two-pane mode
        ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragment_forecast);
        forecastFragment.setShowSpecialTodayItem(!mTwoPane);

        // Notify ForecastFragment whether device is in two-pane mode
        forecastFragment.setDeviceMode(mTwoPane);

        // Initialize SunshineSyncAdapter
        SunshineSyncAdapter.initializeSyncAdapter(this);

        // If Google Play Services is up to date, we'll want to register GCM. If it is not, we'll
        // skip the registration and this device will not receive any downstream messages from
        // our fake server. Because weather alerts are not a core feature of the app, this should
        // not affect the behavior of the app, from a user perspective.
        if (checkPlayServices()) {
            // Because this is the initial creation of the app, we'll want to be certain we have
            // a token. If we do not, then we will start the IntentService that will register this
            // application with GCM.
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if (!sentToken) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
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

            DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().
                    findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(userLocation);
            }

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

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
