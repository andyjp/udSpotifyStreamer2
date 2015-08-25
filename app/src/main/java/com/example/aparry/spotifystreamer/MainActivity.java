package com.example.aparry.spotifystreamer;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();
    public boolean trackPlayerOpened = false;
    public String spotifyUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null && !getResources().getBoolean(R.bool.large_layout)) {
            // Add the fragment FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.main_fragment, new MainActivityFragment())
                    .commit();
        }

        if (savedInstanceState != null) {
            trackPlayerOpened = savedInstanceState.getBoolean("trackPlayerOpened");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            MainActivityFragment mainActivityFragment = (MainActivityFragment) getFragmentManager().findFragmentById(R.id.main_fragment);
            mainActivityFragment.newSearch(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem nowPlayigBtn = menu.findItem(R.id.now_playing_button);
        Button btn = (Button) MenuItemCompat.getActionView(nowPlayigBtn);
        btn.setText(getResources().getString(R.string.now_playing));
        btn.setVisibility(View.GONE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                TrackPlayerFragment trackPlayerFragment = (TrackPlayerFragment) fragmentManager.findFragmentByTag("TrackPlayerFragment");
                if (trackPlayerFragment == null) {
                    trackPlayerFragment = new TrackPlayerFragment();

                    Bundle bundle = new Bundle();
                    bundle.putBoolean("noTrackInfo", true);
                    trackPlayerFragment.setArguments(bundle);
                }
                if (getResources().getBoolean(R.bool.large_layout)) {
                    trackPlayerFragment.show(fragmentManager, "dialog");
                } else {
                    fragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.main_fragment, trackPlayerFragment, "TrackPlayerFragment")
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.v(LOG_TAG, "onPrepareOptionsMenu");
        if (trackPlayerOpened) {
            menu.findItem(R.id.now_playing_button).setVisible(true);
            menu.findItem(R.id.share_track).setVisible(true);
        }

        MenuItem menuItem = menu.findItem(R.id.share_track);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(
                    new Intent(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_TEXT, spotifyUrl)
                            .setType("text/plain")
            );
        } else {
            Log.v(LOG_TAG, "Share provider null");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (getResources().getBoolean(R.bool.large_layout)) {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                //SettingsFragment settingsFragment = new SettingsFragment();
            } else {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("trackPlayerOpened", trackPlayerOpened);
        super.onSaveInstanceState(outState);
    }
}
