package com.jvrhenen.crowdplay.app;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import com.jvrhenen.crowdplay.app.model.Track;

import java.util.ArrayList;

/**
 * Created by Leo on 25/03/14.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = "MusicService";

    private MediaPlayer player;

    private ArrayList<Track> playlist;

    private Track currentTrack;

    private final IBinder musicBind = new MusicBinder();

    public void onCreate(){
        super.onCreate();

        currentTrack = null;

        player = new MediaPlayer();

        initMusicPlayer();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setPlayList(ArrayList<Track> playlist){
        this.playlist = playlist;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();

        return false;
    }

    //play a song
    public void playSong(){
        player.reset();

        long trackId = currentTrack.getTrackId();

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
            Log.i(TAG, "Playing song : " + trackUri);
        }
        catch(Exception e){
            Log.e(TAG, "Error setting data source", e);
        }
        player.prepareAsync();
    }

    //set the song
    public void setTrack(Track track){
        currentTrack = track;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

}
