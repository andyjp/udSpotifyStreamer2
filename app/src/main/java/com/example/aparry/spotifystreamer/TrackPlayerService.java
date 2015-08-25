package com.example.aparry.spotifystreamer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

public class TrackPlayerService extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener
{
    private static final String LOG_TAG = "TrackPlayerService";

    MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;

    public ArrayList<Track> tracksList;
    public int position;
    public int duration = 0;
    private final IBinder iBinder = new TrackPlayerBinder();
    BoundServiceListener boundServiceListener;

    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    Integer notificationId = 1;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case "com.example.aparry.spotifystreamer.PLAY":
                        if (mediaPlayer.isPlaying()) {
                            pauseSong();
                        } else {
                            playSong(null);
                        }
                        break;
                    case "com.example.aparry.spotifystreamer.NEXT":
                        nextSong();
                        break;
                    case "com.example.aparry.spotifystreamer.PREV":
                        prevSong();
                        break;
                    default:
                        break;
                }
            } else {
                tracksList = intent.getParcelableArrayListExtra("TracksList");
                position = intent.getIntExtra("Position", -1);
                if (tracksList != null && position > -1) {
                    playSong(tracksList.get(position));
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "testing", null, null);
        mediaSession.setActive(true);
    }

    public class TrackPlayerBinder extends Binder {
        public TrackPlayerService getService() {
            return TrackPlayerService.this;
        }
        public void setListener(BoundServiceListener listener) {
            boundServiceListener = listener;
            // Start sending progress if music is already playing when binding
            if (mediaPlayer.isPlaying()) { listener.updateTrackPlayer(true); }
        }
    }

    public interface BoundServiceListener {
        void updateTrackPlayer(boolean newSong);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service
        return iBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = mediaPlayer.getDuration();
        mediaPlayer.start();
        boundServiceListener.updateTrackPlayer(true);
        createNotification("play");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        boundServiceListener.updateTrackPlayer(false);
        createNotification("pause");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v(LOG_TAG, "mediaPlayer onError");
        return false;
    }

    public void playSong(Track newTrack) {
        // TODO: Make sure mediaPlayer, tracksList, and position are set
        if (newTrack != null) {
            // Reset mediaPlayer as this method will be used when initially playing a song and when playing
            // subsequent songs
            mediaPlayer.reset();

            String trackUri = newTrack.preview_url;
            try {
                mediaPlayer.setDataSource(trackUri);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error setting data source", e);
            }
            mediaPlayer.prepareAsync();
        } else {
            mediaPlayer.start();
            boundServiceListener.updateTrackPlayer(false);
            createNotification("play");
        }
    }

    public void pauseSong() {
        mediaPlayer.pause();
        boundServiceListener.updateTrackPlayer(false);
        createNotification("pause");
    }

    public void prevSong() {
        position--;
        if (position < 0) {
            position = tracksList.size() - 1;
        }
        playSong(tracksList.get(position));
    }

    public void nextSong() {
        position++;
        if (position > tracksList.size() - 1) {
            position = 0;
        }
        playSong(tracksList.get(position));
    }

    // Create target for dynamically loading album image into notification
    private com.squareup.picasso.Target target = new com.squareup.picasso.Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            notificationManager.notify(
                    notificationId,
                    notificationBuilder
                            .setLargeIcon(bitmap)
                            .build()
            );
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    public void createNotification(String type) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (settings.getBoolean("pref_notifications", true)) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Intent prevIntent = new Intent(this, TrackPlayerService.class);
            prevIntent.setAction("com.example.aparry.spotifystreamer.PREV");
            PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, prevIntent, 0);

            Intent playIntent = new Intent(this, TrackPlayerService.class);
            playIntent.setAction("com.example.aparry.spotifystreamer.PLAY");
            PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, 0);

            Intent nextIntent = new Intent(this, TrackPlayerService.class);
            nextIntent.setAction("com.example.aparry.spotifystreamer.NEXT");
            PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, 0);

            int playIcon = android.R.drawable.ic_media_play;
            if (type == "play") {
                playIcon = android.R.drawable.ic_media_pause;
            }

            notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                    .setStyle(new NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                            .setShowActionsInCompactView(1))
                    .setContentTitle(tracksList.get(position).artists.get(0).name)
                    .setContentText(tracksList.get(position).album.name + " - " + tracksList.get(position).name)
                    .setSmallIcon(playIcon)
                    .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
                    .addAction(playIcon, "Play", playPendingIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent);

            Picasso.with(getApplicationContext())
                    .load(tracksList.get(position).album.images.get(0).url)
                    .into(target);

            startForeground(notificationId, notificationBuilder.build());
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.v(LOG_TAG, "onTaskRemoved");
        //stop service
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

}
