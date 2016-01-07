package com.example.android.sunshine.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;

/**
 * A placeholder fragment to populate DetailActivity
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); /// NO MENU AS LONG AS THIS IS COMMENTED OUT:
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Clear the Menu first to avoid duplicates
        menu.clear();

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Get a reference to the ShareActionProvider
        MenuItem action_share = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(action_share);
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

        if(id == R.id.action_share) {
            startShareIntent();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "What's Up?");
        mShareActionProvider.setShareIntent(intent);

        // First check if there is an app available on the device to handle the intent
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
    }

    // For use in creating an implicit share intent
    private ShareActionProvider mShareActionProvider;

}
