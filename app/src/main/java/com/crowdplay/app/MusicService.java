package com.crowdplay.app;

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

import com.crowdplay.app.comparators.VoteComparator;
import com.crowdplay.app.data.RoomsRepository;
import com.crowdplay.app.model.Room;
import com.crowdplay.app.model.Track;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Leo on 25/03/14.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = "MusicService";

    private MediaPlayer     player;
    private RoomsRepository roomsRepository;

    private int  roomId;
    private Room room;

    private final IBinder musicBind = new MusicBinder();

    private static final int NOTIFY_ID = 1;

    // indicates the state our service:
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
                    // paused in this state if we don't have audio focus. But we stay in this state
                    // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    };

    public void onCreate(){
        super.onCreate();

        player          = new MediaPlayer();
        roomsRepository = new RoomsRepository(this);

        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public int onStartCommand (Intent intent, int flags, int startId){

        roomId = intent.getExtras().getInt("roomId");
        room   = roomsRepository.getRoom(roomId);

        return START_STICKY;
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

    public void playSong(){
        player.reset();

        Track track = getCurrentTrack();

        if (track != null) {
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTrackId());

            try {
                player.setDataSource(getApplicationContext(), trackUri);

                Log.i(TAG, "Playing song : " + track.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "Error setting data source", e);
            }
            player.prepareAsync();
        } else {
            Log.e(TAG, "No song to play");
        }
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        // Remove current track from playlist
        deleteTrack(getCurrentTrack());

        Track nextTrack = getNextTrack();

        if (nextTrack != null) {
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, nextTrack.getTrackId());

            try {
                player.setDataSource(getApplicationContext(), trackUri);
                setCurrentTrack(nextTrack);

                Log.i(TAG, "Playing song : " + nextTrack.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "Error setting data source", e);
            }
            player.prepareAsync();
        } else {
            Log.e(TAG, "No new songs to play");
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v(TAG, "Playback Error");
        mp.reset();

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();

        //notification
        /*Intent notIntent = new Intent(this, RoomPlayActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                //.setSmallIcon(R.drawable.play)
                .setTicker(currentTrack.getTitle())
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(currentTrack.getTitle());
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);*/
    }

    public void setCurrentTrack(Track track){
        room   = roomsRepository.getRoom(roomId);
        room.setCurrentTrack(track);

        // Update changes
        roomsRepository.update(room);
    }

    public Track getCurrentTrack(){
        room   = roomsRepository.getRoom(roomId);
        return room.getCurrentTrack();
    }

    public Collection<Track> getRepoPlaylist(){
        room   = roomsRepository.getRoom(roomId);
        return room.getPlaylist();
    }

    public ArrayList<Track> getSortedPlaylist() {
        ArrayList<Track> playlist = new ArrayList<Track>(getRepoPlaylist());

        if(playlist.size() > 0) {
            // Sort the playlist by votes
            Collections.sort(playlist, new VoteComparator());
            // Return list
            return playlist;
        } else {
            return null;
        }
    }

    public Track getNextTrack() {
        ArrayList<Track> playlist = getSortedPlaylist();

        if(playlist != null) {
            // Return track with most votes
            return playlist.get(0);
        } else {
            return null;
        }
    }

    public void deleteTrack(Track track) {
        room   = roomsRepository.getRoom(roomId);

        // Remove the track from playlist
        room.getPlaylist().remove(track);

        // Update current track
        room.setCurrentTrack(null);

        // Update changes
        roomsRepository.update(room);
    }

    public int getDuration(){
        return player.getDuration();
    }

    public int getPosition() {
        return player.getCurrentPosition();
    }

    public void seek(int position){
        player.seekTo(position);
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void pause(){
        player.pause();
    }

    public void start(){
        player.start();
    }

    @Override
    public void onDestroy() {
        //stopForeground(true);
    }

}
