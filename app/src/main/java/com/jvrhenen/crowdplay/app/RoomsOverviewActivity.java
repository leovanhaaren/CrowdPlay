package com.jvrhenen.crowdplay.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jvrhenen.crowdplay.app.adapters.RoomListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.data.TracksRepository;
import com.jvrhenen.crowdplay.app.listener.SwipeDismissListViewTouchListener;
import com.jvrhenen.crowdplay.app.model.Room;
import com.jvrhenen.crowdplay.app.model.Track;

import java.util.ArrayList;
import java.util.Date;


public class RoomsOverviewActivity extends ActionBarActivity implements ListView.OnItemClickListener {

    private ArrayList<Room> rooms;
    private RoomsRepository roomsRepository;

    private ListView        roomListView;
    private RoomListAdapter roomListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms_overview);

        roomsRepository = new RoomsRepository(this);
        rooms           = roomsRepository.getAll();

        roomListView    = (ListView)findViewById(R.id.listView);
        roomListAdapter = new RoomListAdapter(this, rooms);
        roomListView.setAdapter(roomListAdapter);

        roomListView.setOnItemClickListener(this);

        // Check if we have any results
        checkEmptyState();

        // Init Swipe to dismiss
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        roomListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    Room room = roomListAdapter.getItem(position);

                                    roomsRepository.delete(room);
                                    rooms.remove(room);
                                }

                                // Notify list for changes
                                roomListAdapter.notifyDataSetChanged();
                                checkEmptyState();

                                Toast.makeText(getApplicationContext(), "Removed Room", Toast.LENGTH_LONG).show();
                            }
                        });
        roomListView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        roomListView.setOnScrollListener(touchListener.makeScrollListener());
    }

    public void checkEmptyState() {
        roomListView.setVisibility((roomListAdapter.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    public void openRoom(Room room) {
        Intent roomPlay = new Intent(this, RoomPlayActivity.class);
        roomPlay.putExtra("roomId", room.getId());
        startActivity(roomPlay);
        this.overridePendingTransition(R.anim.animation_sub_enter, R.anim.animation_main_leave);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rooms_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.rooms_overview_action_add) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.rooms_overview_dialog_title);

            final EditText name = new EditText(this);
            name.setHint(R.string.rooms_overview_dialog_field);
            name.setSingleLine();
            builder.setView(name);

            builder.setPositiveButton(R.string.rooms_overview_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.i("Create Room", ""+name.getText());
                    String roomName = name.getText().toString();
                    if(roomName.length() > 0) {
                        Room room = new Room();
                        room.setName(roomName);
                        room.setDate(new Date());

                        roomsRepository.save(room);
                        rooms.add(room);

                        // Notify list of changes
                        roomListAdapter.notifyDataSetChanged();
                        checkEmptyState();

                        Track track1 = new Track();
                        track1.setArtist("Jorick");
                        track1.setTitle("Mooi nummer");
                        track1.setRoom(room);

                        Track track2 = new Track();
                        track2.setArtist("Leo");
                        track2.setTitle("Heel mooi nummer");
                        track2.setRoom(room);

                        TracksRepository repo = new TracksRepository(getApplication());
                        repo.save(track1);
                        repo.save(track2);
                    } else {
                        Toast.makeText(getApplicationContext(), "Vul een Room naam in", Toast.LENGTH_LONG).show();
                    }
//                    Toast.makeText(getApplicationContext(), "TODO: Create Room", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.rooms_overview_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        if (id == R.id.rooms_overview_action_refresh) {
            Toast.makeText(getApplicationContext(), "Searching for rooms", Toast.LENGTH_SHORT).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Room room  = roomListAdapter.getItem(position);
        openRoom(room);
    }

}
