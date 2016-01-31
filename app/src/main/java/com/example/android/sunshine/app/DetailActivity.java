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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {

            // In case the DetailActivity is launched, i.e., device is not
            // in dual-pane mode

            // Create a DetailActivityFragment and add it to the activity
            // using a FragmentTransaction

            // Get the data from the intent and set it as the argument
            // to the DetailActivityFragment
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());

            DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
            detailActivityFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_content_detail, new DetailActivityFragment())
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
    }
}