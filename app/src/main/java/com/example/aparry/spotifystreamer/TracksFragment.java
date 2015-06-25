package com.example.aparry.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


public class TracksFragment extends Fragment {

    private TrackAdapter trackAdapter;

    public TracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);

        trackAdapter = new TrackAdapter(
                getActivity(), // The current context
                R.layout.list_tracks, // The name of the layout id
                new ArrayList<Track>()
        );

        // Bind adapter to listview
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_tracks);
        listView.setAdapter(trackAdapter);

        Intent intent = getActivity().getIntent();
        String artistId = intent.getStringExtra(intent.EXTRA_TEXT);
        this.getTracks(artistId);

        return rootView;
    }

    public void getTracks(String artistId) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            FetchTracksTask test = new FetchTracksTask();
            test.execute(artistId);
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
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String, Object> options = new HashMap<>();
            options.put("country", Locale.getDefault().getCountry());
            Tracks results = spotify.getArtistTopTrack(params[0], options);

            return results;
        }


        @Override
        protected void onPostExecute(Tracks tracks) {
            trackAdapter.clear();
            trackAdapter.addAll(tracks.tracks);
        }
    }

    public class TrackAdapter extends ArrayAdapter<Track> {

        private final List<Track> tracks;

        public TrackAdapter (Context context, int layoutId, List<Track> tracks) {
            super(context, layoutId, tracks);
            this.tracks = tracks;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_tracks, null);
            }

            TextView trackName = (TextView) convertView.findViewById(R.id.list_view_track_name);
            TextView albumName = (TextView) convertView.findViewById(R.id.list_view_album_name);
            ImageView albumImage = (ImageView) convertView.findViewById(R.id.list_view_album_image);

            trackName.setText(tracks.get(position).name);
            albumName.setText(tracks.get(position).album.name);

            if (tracks.get(position).album.images.size() > 0) {
                Picasso.with(getContext())
                        .load(tracks.get(position).album.images.get(0).url)
                        .into(albumImage);
            } else {
                Picasso.with(getContext())
                        .load(R.mipmap.ic_launcher)
                        .into(albumImage);
            }

            return convertView;
        }
    }
}
