package com.example.aparry.spotifystreamer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArtistAdapter artistAdapter;
    private String searchQuery;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create artist array adapter
        artistAdapter = new ArtistAdapter(getActivity(),
                R.layout.list_artist_search_result,
                new ArrayList<Artist>()
        );

        // Bind adapter to listview
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_artist);
        listView.setAdapter(artistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Send artist id and artist name to tracks activity
                Intent detailIntent = new Intent(getActivity(), TracksActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artistAdapter.getItem(position).id)
                        .putExtra("com.example.aparry.spotifystreamer.EXTRA_SUBTITLE", artistAdapter.getItem(position).name);
                startActivity(detailIntent);

            }
        });

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) rootView.findViewById(R.id.artist_search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("SearchQuery", null);
            if (searchQuery != null) {
                newSearch(searchQuery);
            }
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("SearchQuery", searchQuery);
    }

    public void newSearch (String query) {
        searchQuery = query;
        // Make sure there is an internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get artists
            FetchArtistTask search = new FetchArtistTask();
            search.execute(query);
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
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            return spotify.searchArtists(params[0]);
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            if (artistsPager.artists.items.size() > 0) {
                List<Artist> artists = artistsPager.artists.items;
                artistAdapter.clear();
                artistAdapter.addAll(artists);
            } else {
                // Display an error message if no artists were returned
                Toast.makeText(
                        getActivity(),
                        String.format(getString(R.string.no_artist_found), queryString),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    public class ArtistAdapter extends ArrayAdapter<Artist> {

        private List<Artist> artists;

        public ArtistAdapter(Context context, int layoutId, List<Artist> artists) {
            super(context, layoutId, artists);
            this.artists = artists;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_artist_search_result, null);
            }

            TextView artistName = (TextView) convertView.findViewById(R.id.list_artist_search_result_textview);
            ImageView artistImage = (ImageView) convertView.findViewById(R.id.list_artist_search_result_imageview);

            artistName.setText(artists.get(position).name);
            if (artists.get(position).images.size() > 0) {
                Picasso.with(getContext())
                        .load(artists.get(position).images.get(0).url)
                        .into(artistImage);
            } else {
                Picasso.with(getContext())
                        .load(R.mipmap.ic_launcher)
                        .into(artistImage);
            }

            return convertView;
        }
    }
}
