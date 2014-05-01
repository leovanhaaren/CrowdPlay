package com.crowdplay.app;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import com.crowdplay.app.comparators.VoteComparator;
import com.crowdplay.app.data.RoomsRepository;
import com.crowdplay.app.model.Room;
import com.crowdplay.app.model.Track;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leo on 25/03/14.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = "MusicService";

    private MediaPlayer     player;

    private RoomsRepository  roomsRepository;

    private Room room;

    private final IBinder musicBind = new MusicBinder();

    private static final int NOTIFY_ID = 1;

    private WifiP2pManager mManager;
    private Channel        mChannel;

    public void onCreate(){
        super.onCreate();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        registerService();

        player           = new MediaPlayer();
        roomsRepository  = new RoomsRepository(this);

        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public int onStartCommand (Intent intent, int flags, int startId){
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

    public void unloadRoom() {
        this.room = null;
    }

    public void loadRoom(Room room) {
        this.room = roomsRepository.getRoom(room.getId());
    }

    public void reloadCurrentRoom() {
        if(room == null) return;

        loadRoom(room);
    }

    public boolean isHost() {
        return room.getOwner().equals(getDeviceID());
    }

    public boolean hasRoom() {
        return (room != null);
    }

    public String getDeviceID() {
        return Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void initPlaylist(){
        player.reset();

        Track track = getNextTrack();

        if (track != null) {
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTrackId());

            try {
                player.setDataSource(getApplicationContext(), trackUri);
                setCurrentTrack(track);

                player.prepareAsync();
                player.pause();

                Log.i(TAG, "Prepaired song : " + track.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "Error setting data source", e);
            }
        } else {
            Log.e(TAG, "No song to play");
        }
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        deleteTrack(getCurrentTrack());

        Track nextTrack = getNextTrack();

        if (nextTrack != null) {
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, nextTrack.getTrackId());

            try {
                player.setDataSource(getApplicationContext(), trackUri);
                setCurrentTrack(nextTrack);

                player.prepareAsync();

                Log.i(TAG, "Playing song : " + nextTrack.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "Error setting data source", e);
            }
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

    @Override
    public void onDestroy() {
        //stopForeground(true);
    }

    public void setCurrentTrack(Track track){
        reloadCurrentRoom();
        room.setCurrentTrack(track);

        roomsRepository.update(room);
    }

    public Track getCurrentTrack(){
        room   = roomsRepository.getRoom(room.getId());
        Track track = room.getCurrentTrack();

        return track;
    }

    public Collection<Track> getRepoPlaylist(){
        room   = roomsRepository.getRoom(room.getId());
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
        room   = roomsRepository.getRoom(room.getId());

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

    public Room getRoom() {
        return room;
    }

    private void registerService() {
        //  Create a string map containing information about your service.
        Map record = new HashMap();
        record.put("listenport", String.valueOf("999"));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Started local WiFi service");
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            }
        });
    }

}
