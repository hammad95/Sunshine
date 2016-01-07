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
import android.widget.Toast;

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
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
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

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        // Get the name of the class for the Log messages
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        // Makes an Http "GET" request to the OWM API
        // and retrieves a JSON string of weather data.
        // Then calls a method to parse the data and
        // returns an array of forecast strings to
        // onPostExecute()
        @Override
        protected String[] doInBackground(String... params) {
            // If no zip code, return null
            if(params.length == 0)
                return null;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            // Array to hold the parsed JSON weather data
            String[] weatherForecastArray = null;

            // Values for query param keys
            String zipCode = params[0];  // Postal code can be retrieved from params[0]
            String format = "json";
            String units = "metric";
            int numDays = 7;

            // Query parameter keys needed to construct uri
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String ZIP_PARAM = "zip";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String API_KEY_PARAM = "appid";

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                // Build uri by appending params using URI.Builder
                Uri uri = Uri.parse(BASE_URL).buildUpon().
                        appendQueryParameter(ZIP_PARAM, zipCode).
                        appendQueryParameter(FORMAT_PARAM, format).
                        appendQueryParameter(UNITS_PARAM, units).
                        appendQueryParameter(DAYS_PARAM, Integer.toString(numDays)).
                        appendQueryParameter(API_KEY_PARAM, OPEN_WEATHER_API_KEY).
                        build();

                // Assign uri to url to be used to open a connection
                URL url = new URL(uri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("LOG_TAG", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("LOG_TAG", "Error closing stream", e);
                    }
                }
            }

            // Parse JSON data and store in an array
            try {
                weatherForecastArray = getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Return a string array of weather forecast data
            return weatherForecastArray;
        }

        // Checks if returned array holds data.
        // If it does, then it clears the ArrayAdapter
        // and adds each string from the array one by one
        // into the ArrayAdapter
        @Override
        protected void onPostExecute(String[] weatherForecastArray) {
            if(weatherForecastArray != null) {
                mForecastAdapter.clear();
                for(String forecastPerDay : weatherForecastArray)
                    mForecastAdapter.add(forecastPerDay);
            }
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);

                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
 * so for convenience we're breaking it out into its own method now.
 */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // Get the selected temperature units from SharedPreferences
            String units = defaultSharedPreferences.getString(getString(R.string.pref_temp_units_key),
                    getString(R.string.pref_temp_units_default_val));

            Log.v(LOG_TAG, "*******************Value of returned Preference: " + units);

            // If selected unit is "Fahrenheit", covert to Fahrenheit
            if(units.equals(getString(R.string.pref_units_fahrenheit))) {
                high = high * 1.8 + 32;
                low = low * 1.8 + 32;
            }

            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private void convertToFahrenheit(Double temp) {


            return;
        }
    }

    // Open Weather API Key to be used in the uri
    protected static final String OPEN_WEATHER_API_KEY = "7f1777cc20ba2ac8b65cfafd8145b58c";

    // ArrayAdapter used to populate the ListView
    private ArrayAdapter<String> mForecastAdapter;

    // Default SharedPreferences
    private SharedPreferences defaultSharedPreferences;
}
