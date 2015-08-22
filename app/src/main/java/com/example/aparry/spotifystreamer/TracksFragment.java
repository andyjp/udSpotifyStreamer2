package com.example.aparry.spotifystreamer;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

public class TracksFragment extends Fragment {

    private static final String LOG_TAG = "TracksFragment";
    public ArrayList<Track> tracksList = new ArrayList<Track>();
    public TrackAdapter trackAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);

        trackAdapter = new TrackAdapter(
                getActivity(), // The current context
                R.layout.list_tracks, // The name of the layout id
                tracksList
        );

        // On large layouts the artist getTracks is called directly from MainActivityFragment
        // On small layouts the artist is passed to this fragment and we need to call getTracks here
        if (trackAdapter.getCount() < 1 && !getResources().getBoolean(R.bool.large_layout)) {
            Artist artist = getArguments().getParcelable("Artist");
            if (artist != null) {
                this.getTracks(artist.id);
            }
        }

        // Bind adapter to listview
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_tracks);
        listView.setAdapter(trackAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FragmentManager fragmentManager = getFragmentManager();
                TrackPlayerFragment trackPlayerFragment = new TrackPlayerFragment();

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("TracksList", tracksList);
                bundle.putInt("Position", position);
                trackPlayerFragment.setArguments(bundle);

                if (getResources().getBoolean(R.bool.large_layout)) {
                    Log.v("TracksFragment", "large layout option");
                    trackPlayerFragment.show(fragmentManager, "dialog");
                } else {
                    Log.v("TracksFragment", "small layout option");
                    fragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.main_fragment, trackPlayerFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return rootView;
    }

    public void getTracks(String artistId) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (artistId != null) {
                FetchTracksTask test = new FetchTracksTask();
                test.execute(artistId);
            }
        } else {
            Toast.makeText(
                    getActivity(),
                    R.string.internet_connection_error,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public class FetchTracksTask extends AsyncTask<String, Void, Tracks> {
        @Override
        protected Tracks doInBackground(String... params) {
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                return spotify.getArtistTopTrack(params[0], Locale.getDefault().getCountry());
            } catch (RetrofitError e) {
                Log.v(LOG_TAG, SpotifyError.fromRetrofitError(e).toString());
                Toast.makeText(
                        getActivity(),
                        R.string.tracks_retrieve_error,
                        Toast.LENGTH_SHORT
                ).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            if (tracks != null) {
                if (tracks.tracks.size() > 0) {
                    trackAdapter.clear();
                    for (Track track : tracks.tracks) {
                        trackAdapter.add(track);
                    }
                } else {
                    Toast.makeText(
                            getActivity(),
                            R.string.no_tracks_found,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            } else {
                Log.v(LOG_TAG, "Tracks is null");
            }
        }
    }

}
