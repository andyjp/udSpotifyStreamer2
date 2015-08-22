package com.example.aparry.spotifystreamer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.app.SearchManager;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class MainActivityFragment extends Fragment {

    private static final String LOG_TAG = "MainActivityFragment";
    public ArrayList<Artist> artistList = new ArrayList<Artist>();
    public ArtistAdapter artistAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (getResources().getBoolean(R.bool.large_layout)) {
            ListView listView = (ListView) getActivity().findViewById(R.id.list_view_artist);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        artistAdapter = new ArtistAdapter(
                getActivity(),
                R.layout.list_artist_search_result,
                artistList
        );

        // Bind adapter to listview
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_artist);
        listView.setAdapter(artistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View trackListFrame = getActivity().findViewById(R.id.tracks_fragment);
                if (trackListFrame != null) {
                    TracksFragment tracksFragment = (TracksFragment) getFragmentManager().findFragmentById(R.id.tracks_fragment);
                    if (tracksFragment != null) {
                        ListView listView = (ListView) getActivity().findViewById(R.id.list_view_artist);
                        listView.setItemChecked(position, true);
                        tracksFragment.getTracks(artistAdapter.getItem(position).id);
                    }
                } else {
                    TracksFragment tracksFragment = new TracksFragment();

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("Artist", artistAdapter.getItem(position));
                    tracksFragment.setArguments(bundle);

                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, tracksFragment)
                            .addToBackStack(null)
                            .commit();
                }

            }
        });


        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) rootView.findViewById(R.id.artist_search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        return rootView;
    }

    public void newSearch (String query) {
        // Make sure there is an internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get artists
            FetchArtistTask search = new FetchArtistTask();
            search.execute(query);
            Log.v(LOG_TAG,"search execute");
        } else {
            Toast.makeText(
                    getActivity(),
                    R.string.internet_connection_error,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public class FetchArtistTask extends AsyncTask<String, Void, ArtistsPager> {

        private String queryString = "";

        @Override
        protected ArtistsPager doInBackground(String... params) {
            // Set the queryString value
            queryString = params[0];

            // Get artists using spotify api
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                return spotify.searchArtists(params[0]);
            } catch (RetrofitError e) {
                Log.v(LOG_TAG, SpotifyError.fromRetrofitError(e).toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            // Make sure artistPager is not null
            if (artistsPager != null) {
                // Check to make sure we have artist results
                if (artistsPager.artists.items.size() > 0) {
                    // Loop through the artists and add to artistAdapter
                    // Adding to artist adapter also adds to artistList
                    artistAdapter.clear();
                    for (Artist artist : artistsPager.artists.items) {
                        artistAdapter.add(artist);
                    }
                } else {
                    // Display an error message if no artists were returned
                    Toast.makeText(
                            getActivity(),
                            String.format(getString(R.string.no_artist_found), queryString),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            } else {
                Log.v(LOG_TAG, "ArtistPager is null");
            }
        }
    }
}
