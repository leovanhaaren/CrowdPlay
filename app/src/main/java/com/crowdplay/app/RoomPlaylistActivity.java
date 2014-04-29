package com.crowdplay.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.crowdplay.app.adapters.VoteListAdapter;
import com.crowdplay.app.data.RoomsRepository;
import com.crowdplay.app.model.Room;
import com.crowdplay.app.model.Track;

import java.util.ArrayList;


public class RoomPlaylistActivity extends Activity implements OnItemClickListener {

    private static final String TAG = "RoomPlaylistActivity";

    private ArrayList<Track> tracks;
    private RoomsRepository  roomsRepository;

    private ListView        mListView;
    private VoteListAdapter mAdapter;

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
        tracks = new ArrayList<Track>(room.getPlaylist());

        mListView = (ListView)findViewById(R.id.listView);
        mAdapter = new VoteListAdapter(this, tracks);
        mListView.setAdapter(mAdapter);

        // Check if we have any results
        checkEmptyState();

        // Display the room's name as title for player activity
        getActionBar().setTitle(room.getName());
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh listview, in case we added a track
        tracks   = new ArrayList<Track>(room.getPlaylist());
        mAdapter = new VoteListAdapter(this, tracks);
        mListView.setAdapter(mAdapter);
        mListView.invalidate();

        // Check if we have any results
        checkEmptyState();

        Log.i(TAG, "Activity Life Cycle : onResume : Activity Resumed");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_playlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.room_play_action_add:
                FragmentManager fragmentManager = getFragmentManager();
                MusicOverviewFragment fragment  = new MusicOverviewFragment();

                fragment.setRoomId(room.getId());
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        //Track track  = mAdapter.getItem(position);
    }

    public void checkEmptyState() {
        mListView.setVisibility((mListView.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    public void addDemoTrack() {
        Track track = new Track("User track", "Artist");
        tracks.add(track);

        // Notify list for changes
        mAdapter.notifyDataSetChanged();
        checkEmptyState();

        Toast.makeText(getApplicationContext(), "Added demo Track", Toast.LENGTH_SHORT).show();
    }

}
