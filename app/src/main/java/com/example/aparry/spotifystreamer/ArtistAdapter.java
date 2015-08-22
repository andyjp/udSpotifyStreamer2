package com.example.aparry.spotifystreamer;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by AParry on 6/27/2015.
 */
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
//Log.v("ArtistAdapter", "set text and image");
        artistName.setText(artists.get(position).name);
        if (artists.get(position).images.size() > 0) {
            Picasso.with(getContext())
                    .load(artists.get(position).images.get(0).url)
                    .into(artistImage);
        }
       /* if (artists.get(position).imageUrl != null) {
            Picasso.with(getContext())
                    .load(artists.get(position).imageUrl)
                    .into(artistImage);
        } */
        else {
            Picasso.with(getContext())
                    .load(R.mipmap.ic_launcher)
                    .into(artistImage);
        }

        return convertView;
    }
}
