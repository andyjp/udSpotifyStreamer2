package com.example.aparry.spotifystreamer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by AParry on 6/27/2015.
 */
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
