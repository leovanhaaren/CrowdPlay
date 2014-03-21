package com.jvrhenen.crowdplay.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.jvrhenen.crowdplay.app.adapters.PlaylistListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.listener.SwipeDismissListViewTouchListener;
import com.jvrhenen.crowdplay.app.model.Room;
import com.jvrhenen.crowdplay.app.model.Track;

import java.util.ArrayList;


public class RoomPlayActivity extends Activity {

    private ArrayList<Track>    tracks;
    private RoomsRepository     roomsRepository;

    private ListView            playlistView;
    private PlaylistListAdapter playlistViewAdapter;

    private int roomId;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_play);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        roomId = getIntent().getExtras().getInt("roomId");

        roomsRepository = new RoomsRepository(this);

        room   = roomsRepository.getRoom(roomId);
        tracks = new ArrayList<Track>(room.getTracks());

        playlistView        = (ListView)findViewById(R.id.listView);
        playlistViewAdapter = new PlaylistListAdapter(this, tracks);
        playlistView.setAdapter(playlistViewAdapter);

        // Check if we have any results
        checkEmptyState();

        // Display the room's name as title for player activity
        getActionBar().setTitle(room.getName());

        // Init Swipe to dismiss
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        playlistView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    Track track = playlistViewAdapter.getItem(position);

                                    room.getTracks().remove(track);
                                    tracks.remove(track);
                                }

                                // Notify list for changes
                                playlistViewAdapter.notifyDataSetChanged();
                                checkEmptyState();

                                Toast.makeText(getApplicationContext(), "Removed Track", Toast.LENGTH_SHORT).show();
                            }
                        });
        playlistView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        playlistView.setOnScrollListener(touchListener.makeScrollListener());
    }

    public void checkEmptyState() {
        playlistView.setVisibility((playlistView.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    public void addDemoTrack() {
        Track track = new Track("Test track", "Artist");
        tracks.add(track);

        // Notify list for changes
        playlistViewAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), "Added demo Track", Toast.LENGTH_SHORT).show();
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
            case R.id.room_play_action_add:
                addDemoTrack();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
