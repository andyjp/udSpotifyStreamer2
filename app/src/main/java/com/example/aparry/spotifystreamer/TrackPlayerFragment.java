package com.example.aparry.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

public class TrackPlayerFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener
{
    private static final String LOG_TAG = "TrackPlayerFragment";

    private TrackPlayerService trackPlayerService;
    private boolean trackPlayerBound = false;
    private boolean isUserSeeking = false;
    ArrayList<Track> tracksList;
    int position = -1;
    View rootView;

    private ServiceConnection trackPlayerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            trackPlayerBound = true;

            TrackPlayerService.TrackPlayerBinder binder = (TrackPlayerService.TrackPlayerBinder) service;
            trackPlayerService = binder.getService();
            binder.setListener(new TrackPlayerService.BoundServiceListener() {
                @Override
                public void updateTrackPlayer(boolean newSong) {
                    if (trackPlayerBound) {
                        ImageButton playButton = (ImageButton) rootView.findViewById(R.id.track_player_play);

                        // update seek bar
                        final SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.track_player_seek_bar);
                        //int duration = trackPlayerService.mediaPlayer.getDuration();
                        seekBar.setMax(trackPlayerService.duration);
                        seekBar.setProgress(trackPlayerService.mediaPlayer.getCurrentPosition());

                        // Set the end time of the song
                        TextView endTime = (TextView) rootView.findViewById(R.id.track_player_max_time);
                        endTime.setText(String.format("%2d:%02d", trackPlayerService.duration / 60000, trackPlayerService.duration / 1000));

                        if (trackPlayerService.mediaPlayer.isPlaying()) {
                            // Create thread for updating the seek bar as track plays
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while (trackPlayerService.mediaPlayer.isPlaying() && trackPlayerBound) {
                                        // Set seek bar position
                                        if (!isUserSeeking) {
                                            seekBar.setProgress(trackPlayerService.mediaPlayer.getCurrentPosition());
                                        }
                                    }
                                }
                            }).start();

                            // Track is playing; make sure pause image is showing
                            if (playButton.getContentDescription() == getString(R.string.play)) {
                                Picasso.with(rootView.getContext())
                                        .load(android.R.drawable.ic_media_pause)
                                        .into(playButton);
                                playButton.setContentDescription(getString(R.string.pause));
                            }
                        } else {
                            // Track is not playing; make sure play image is showing
                            if (playButton.getContentDescription() == getString(R.string.pause)) {
                                Picasso.with(rootView.getContext())
                                        .load(android.R.drawable.ic_media_play)
                                        .into(playButton);
                                playButton.setContentDescription(getString(R.string.play));
                            }
                        }

                        if (newSong) {
                            setTrackInfo();
                        }
                    }
                }
            });
            trackPlayerService.boundServiceListener.updateTrackPlayer(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(LOG_TAG, "onServiceDisconnected() called");
            trackPlayerBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_track_player, container, false);

        Intent playIntent = new Intent(getActivity(), TrackPlayerService.class);

        if (tracksList == null) {
            tracksList = getArguments().getParcelableArrayList("TracksList");
            playIntent.putExtra("TracksList", tracksList);
        }
        if (position == -1) {
            position = getArguments().getInt("Position");
            playIntent.putExtra("Position", position);
        }
        if (savedInstanceState == null) {
            getActivity().startService(playIntent);
        }
        getActivity().bindService(playIntent, trackPlayerConnection, Context.BIND_AUTO_CREATE);

        // Set play button listener
        ImageButton playButton = (ImageButton) rootView.findViewById(R.id.track_player_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trackPlayerBound) {
                    if (trackPlayerService.mediaPlayer.isPlaying()) {
                        trackPlayerService.pauseSong();
                    } else {
                        trackPlayerService.playSong(null);
                    }
                }
            }
        });

        // Set previous button listener
        ImageButton prevButton = (ImageButton) rootView.findViewById(R.id.track_player_previous);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get previous track and start playing it
                if (trackPlayerBound) { trackPlayerService.prevSong(); }
            }
        });

        // Set next button listener
        ImageButton nextButton = (ImageButton) rootView.findViewById(R.id.track_player_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trackPlayerBound) { trackPlayerService.nextSong(); }
            }
        });

        // Set seek bar listener
        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.track_player_seek_bar);
        seekBar.setOnSeekBarChangeListener(this);

        return rootView;
    }

    @Override
    public void onStop() {
        // Unbind the service when the track player dialog is closed
        trackPlayerBound = false;
        getActivity().unbindService(trackPlayerConnection);
        super.onStop();
    }

    public void setTrackInfo() {
        TextView trackName = (TextView) rootView.findViewById(R.id.track_player_track_name);
        TextView albumName = (TextView) rootView.findViewById(R.id.track_player_album_name);
        ImageView albumImage = (ImageView) rootView.findViewById(R.id.track_player_album_image);
        TextView artistName = (TextView) rootView.findViewById(R.id.track_player_artist_name);

        if (trackPlayerBound) {
            tracksList = trackPlayerService.tracksList;
            position = trackPlayerService.position;
        }

        trackName.setText(tracksList.get(position).name);
        albumName.setText(tracksList.get(position).album.name);
        artistName.setText(tracksList.get(position).artists.get(0).name);
        if (tracksList.get(position).album.images.size() > 0) {
            Picasso.with(getActivity())
                    .load(tracksList.get(position).album.images.get(0).url)
                    .into(albumImage);
        } else {
            Picasso.with(getActivity())
                    .load(R.mipmap.ic_launcher)
                    .into(albumImage);
        }
    }

    // Functions for OnSeekBarChangeListener interface
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (trackPlayerBound) {
            try {
                TextView startTime = (TextView) rootView.findViewById(R.id.track_player_time);
                startTime.setText(String.format("%2d:%02d", progress / 60000, progress / 1000));
            } catch (NullPointerException e) {
                Log.v(LOG_TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isUserSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isUserSeeking = false;
        trackPlayerService.mediaPlayer.seekTo(seekBar.getProgress());
    }
}
