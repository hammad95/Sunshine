package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Fragment to be used by MainActivity
 * Created by Hassan on 12/30/2015.
 */
public class ForecastFragment extends android.support.v4.app.Fragment {

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Add an ArrayAdapter used to populate the ListView
        mForecastAdapter = new ArrayAdapter<String>(
                this.getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview,
                new ArrayList<String>()
        );

        // Retrieve ListView and set adapter
        ListView listView_forecast = (ListView) rootView.findViewById(
                R.id.listView_forecast);
        listView_forecast.setAdapter(mForecastAdapter);

        // Add onItemClickListener to ListView
        listView_forecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailActivityIntent = new Intent(getActivity(), DetailActivity.class);
                detailActivityIntent.putExtra(Intent.EXTRA_TEXT,
                mForecastAdapter.getItem(position));
                startActivity(detailActivityIntent);
            }
        });

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        return rootView;
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            // Call update weather to fetch weather from OWM
            updateWeather();
            return true;
        }

        if(id == R.id.action_map) {
            // Call helper method to show the preferred location on a map
            showPreferredLocationOnMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        // Get default SharedPreferences and store the key/value pair
        // for ZIP code to execute the AsyncTask with
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                this.getContext()
        );
        String zipcode = defaultSharedPreferences.getString(
                getString(R.string.pref_location_key), getString(R.string.pref_location_default_val)
        );

        // Create an instance of FetchWeatherTask and send the retrieved String
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getContext(), mForecastAdapter);
        fetchWeatherTask.execute(zipcode);
    }

    // Starts an implicit intent to show the user's preferred location on a map
    private void showPreferredLocationOnMap() {
        String zipcode = defaultSharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default_val));

        // Query parameter keys
        final String QUERY_PARAM = "q";

        Uri uri = Uri.parse("geo:0,0?").buildUpon().
                appendQueryParameter(QUERY_PARAM, zipcode).
                build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        // First check if there is an app available on the device to handle the intent
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
    }

    // ArrayAdapter used to populate the ListView
    private ArrayAdapter<String> mForecastAdapter;

    // Default SharedPreferences
    private SharedPreferences defaultSharedPreferences;
}
