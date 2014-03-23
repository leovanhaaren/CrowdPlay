package com.jvrhenen.crowdplay.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.jvrhenen.crowdplay.app.adapters.VoteListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.model.Room;
import com.jvrhenen.crowdplay.app.model.Track;

import java.util.ArrayList;


public class RoomPlaylistActivity extends Activity {

    private ArrayList<Track>    tracks;
    private RoomsRepository     roomsRepository;

    private ListView        playlistView;
    private VoteListAdapter voteListAdapter;

    private int roomId;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_playlist);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        roomId = getIntent().getExtras().getInt("roomId");

        roomsRepository = new RoomsRepository(this);

        room   = roomsRepository.getRoom(roomId);
        tracks = new ArrayList<Track>(room.getTracks());

        playlistView           = (ListView)findViewById(R.id.listView);
        voteListAdapter = new VoteListAdapter(this, tracks);
        playlistView.setAdapter(voteListAdapter);

        // Check if we have any results
        checkEmptyState();

        // Display the room's name as title for player activity
        getActionBar().setTitle(room.getName());
    }

    public void checkEmptyState() {
        playlistView.setVisibility((playlistView.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    public void addDemoTrack() {
        Track track = new Track("User track", "Artist");
        tracks.add(track);

        // Notify list for changes
        voteListAdapter.notifyDataSetChanged();
        checkEmptyState();

        Toast.makeText(getApplicationContext(), "Added demo Track", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_playlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                this.overridePendingTransition(R.anim.animation_main_enter, R.anim.animation_sub_leave);
                return true;
            case R.id.room_play_action_add:
                addDemoTrack();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
