package com.jvrhenen.crowdplay.app;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.jvrhenen.crowdplay.app.Adapters.PlaylistListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.model.Room;
import com.jvrhenen.crowdplay.app.model.Track;

import java.io.IOException;
import java.util.ArrayList;


public class RoomPlayActivity extends Activity {

    private RoomsRepository roomsRepository;

    private int roomId;
    private Room room;

    private ArrayList<Track> tracks;

    private PlaylistListAdapter trackListViewAdapter;
    private ListView trackListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_play);

        roomId = getIntent().getExtras().getInt("roomId");
        roomsRepository =  new RoomsRepository(this);

        trackListView = (ListView)findViewById(R.id.listView);

        loadData();
        Log.i("Path", Environment.getExternalStorageDirectory().getPath());

        try {
            String url = "http://icecast.omroep.nl/3fm-bb-mp3"; // your URL here
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        room = roomsRepository.getRoom(roomId);
        tracks = new ArrayList<Track>(room.getTracks());

        trackListViewAdapter = new PlaylistListAdapter(this, tracks);
        trackListView.setAdapter(trackListViewAdapter);

        if(trackListViewAdapter.getCount() == 0) {
            trackListView.setVisibility(View.INVISIBLE);
        } else {
            trackListView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.room_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
