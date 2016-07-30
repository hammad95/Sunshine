package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.widget.TextView;

/**
 * Fragment to be used by MainActivity
 * Created by Hassan on 12/30/2015.
 */
public class ForecastFragment extends android.support.v4.app.Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
                   SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    // ListView to display weather forecast data
    private ListView listView_forecast;

    // ArrayAdapter used to populate the ListView
    private ForecastAdapter mForecastAdapter;

    // Default SharedPreferences
    private SharedPreferences defaultSharedPreferences;

    // CursorLoader ID
    private final int FORECAST_LOADER = 100;

    // ListView position to be retrieved from savedInstanceState
    private int mPosition;

    // Key for the position of the ListView for saving in savedInstanceState Bundle
    private final String POSITION_KEY = "POSITION";

    // Decides whether the special today list item should be shown
    private boolean mShowSpecialTodayItem;

    // Device mode
    private boolean mTwoPane;

    static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create a new ForecastAdapter, which is a CursorAdapter
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        // Call setShowSpecialTodayItem() to ensure that as soon as an instance
        // of ForecastAdapter is instantiated, the appropriate value is passed
        mForecastAdapter.setShowSpecialTodayItem(mShowSpecialTodayItem);

        // Retrieve ListView and set adapter
        listView_forecast = (ListView) rootView.findViewById(
                R.id.listView_forecast);
        // Before setting the adapter, set an empty view to the list
        View emptyView = rootView.findViewById(R.id.tvForecastListEmpty);
        listView_forecast.setEmptyView(emptyView);
        // Set the adapter
        listView_forecast.setAdapter(mForecastAdapter);

        // Add onItemClickListener to ListView
        listView_forecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).
                            onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                }
                // Save the position of the ListView
                mPosition = position;
            }
        });

        // Retrieve the position of the ListView from the Bundle if the
        // saveInstanceState is not null and it contains the position
        if(savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY))
            mPosition = savedInstanceState.getInt(POSITION_KEY);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        // Initialize the CursorLoader
        getActivity().getSupportLoaderManager().initLoader(FORECAST_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Clear the menu first to avoid duplicates
        menu.clear();

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this.getContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_refresh) {
//            // Call update weather to fetch weather from OWM
//            updateWeather();
//            return true;
//        }

        if(id == R.id.action_map) {
            // Call helper method to show the preferred location on a map
            openPreferredLocationInMap();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outstate) {
        // When tablet rotates, the current position of the ListView needs
        // to be saved so that it can be restored upon creating a new
        // activity. If no position was selected, mPosition will be equal
        // to ListView.INVALID_POSITION, so check for that first
        if(mPosition != ListView.INVALID_POSITION) {
            outstate.putInt(POSITION_KEY, mPosition);
        }
        super.onSaveInstanceState(outstate);
    }

    @Override
    public void onResume() {

        // Register an onPreferenceChangeListener to the default SharedPreferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        super.onResume();
    }

    @Override
    public void onPause() {

        // Deregister an onPreferenceChangeListener to the default SharedPreferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }

    public void onLocationChanged() {
        // Update the weather
        updateWeather();

        // Restart the loader
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    // Sends a broadcast using an AlarmManager to be receive by SunshineService.AlarmReceiver
    private void updateWeather() {

        // To start the sync, call SunshineSyncAdapter.syncImmediately()
        SunshineSyncAdapter.syncImmediately(getContext());

    }

    // Starts an implicit intent to show the user's preferred location on a map
    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != mForecastAdapter ) {
            Cursor c = mForecastAdapter.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    // Set whether to ask ForecastAdapter to display special today item
    // This method might be first called from onCreateView()
    // Also, mForecastAdapter might be null because MainActivity's
    // onCreate() will be called before this fragment's onCreatView()
    // so we check whether its null
    public void setShowSpecialTodayItem(boolean value) {
        mShowSpecialTodayItem = value;
        if(mForecastAdapter != null)
            mForecastAdapter.setShowSpecialTodayItem(mShowSpecialTodayItem);
    }

    // Sets the value of mTwoPane based on the device mode
    public void setDeviceMode(boolean twoPaneMode) {
        mTwoPane = twoPaneMode;
    }

    // CurosorLoader callback methods

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Compare the id of the loader

        switch(id) {
            case FORECAST_LOADER:

                String locationSetting = Utility.getPreferredLocation(getActivity());

                // Sort order:  Ascending, by date.
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        locationSetting, System.currentTimeMillis());

                return new CursorLoader(this.getContext(),
                        weatherForLocationUri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        sortOrder);

            default:

                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Move the query results into the adapter causing the associated ListView
        // to redisplay its contents
        mForecastAdapter.changeCursor(data);

        // Scroll to saved ListView position if tablet is rotated
        if(mPosition != ListView.INVALID_POSITION && mTwoPane)
            listView_forecast.smoothScrollToPosition(mPosition);

        // Checks if adapter is empty and sets the appropriate string
        // to the emptyView of the ListView
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Delete the adapter' reference to the cursor to prevent mo=emory leak
        mForecastAdapter.changeCursor(null);
    }

    public void updateEmptyView() {
        // If mForecastAdapter is empty
        if(mForecastAdapter.getCount() == 0) {
            TextView emptyView = (TextView) getView().findViewById(R.id.tvForecastListEmpty);
            if(emptyView != null) {
                // adapter could be empty because location is invalid
                int errorString = R.string.forecast_list_empty;
                // Get the location status from SharedPreferences
                @SunshineSyncAdapter.LocationStatus final int LOCATION_STATUS =
                        Utility.getLocationStatus(getContext());
                switch (LOCATION_STATUS) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        errorString = R.string.forecast_list_empty_server_down;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        errorString = R.string.forecast_list_empty_server_error;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        errorString = R.string.forecast_list_empty_invalid_location;
                        break;
                    default:
                        // Or it could be empty because no network connection
                        if(!Utility.isConnected(getContext())) {
                            errorString = R.string.forecast_list_empty_network_error;
                        }
                }
                // Set the appropriate string to the emptyView based on the error reason
                emptyView.setText(getString(errorString));
            }
        }
    }

    // Listens for changes to SharedPreferences
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Call updateEmptyView whenever the location status preference changes
        if(key.equals(getString(R.string.pref_location_status_key)))
            updateEmptyView();
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}
