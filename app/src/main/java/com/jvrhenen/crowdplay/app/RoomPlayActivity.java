package com.jvrhenen.crowdplay.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.jvrhenen.crowdplay.app.Adapters.PlaylistListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.model.Room;
import com.jvrhenen.crowdplay.app.model.Track;

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
        getActionBar().setDisplayHomeAsUpEnabled(true);

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

        // Display the room's name as title for player activity
        getActionBar().setTitle(room.getName());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                this.overridePendingTransition(R.anim.animation_main_enter, R.anim.animation_sub_leave);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
