package com.example.android.sunshine.app;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment to be used by MainActivity
 * Created by Hassan on 12/30/2015.
 */
public class ForecastFragment extends android.support.v4.app.Fragment {
    private ArrayAdapter<String> mForecastAdapter; // adapter to populate ListView

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // String array with fake data to populate the ListView
        String[] forecastArray = {
                "Today - Sunny - 88 / 63",
                "Tomorrow - Sunny - 68 / 54"
        };

        // Initialize a List of fake data from the string array
        List<String> weekForecast = new ArrayList<String>(
                Arrays.asList(forecastArray));

        // Add an ArrayAdapter used to populate the ListView
        mForecastAdapter = new ArrayAdapter<String>(
                this.getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview,
                weekForecast
        );

        // Retrieve ListView and set adapter
        ListView listView_forecast = (ListView) rootView.findViewById(
                R.id.listView_forecast);
        listView_forecast.setAdapter(mForecastAdapter);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        return rootView;
    }
}
