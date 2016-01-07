/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_detail, new DetailActivityFragment())
                    .commit();
        }

        // Set Toolbar as default ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_detail_activity));

        // Get Support ActionBar
        ActionBar actionBar = getSupportActionBar();

        // Set home button to navigate to MainActivity and display app icon in ActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.drawable.ic_launcher);
        }

        // Receive the intent to be able to access the data
        Intent intent = getIntent();

        // To extract string from Intent data
        String forecastString;

        // Get the TextView
        TextView tvForecast = (TextView) findViewById(R.id.tvForecast);

        // Get the string from the Intent data
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            forecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
            tvForecast.setText(forecastString);
        }
    }

    // Getter method for string passed as Intent data
    
}