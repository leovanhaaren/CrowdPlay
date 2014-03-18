package com.jvrhenen.crowdplay.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.jvrhenen.crowdplay.app.Adapters.PlaylistListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.model.*;

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
