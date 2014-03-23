package com.jvrhenen.crowdplay.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jvrhenen.crowdplay.app.adapters.RoomListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.listener.SwipeDismissListViewTouchListener;
import com.jvrhenen.crowdplay.app.model.Room;

import java.util.ArrayList;


public class RoomsOverviewActivity extends ActionBarActivity implements ListView.OnItemClickListener {

    private ArrayList<Room> rooms;
    private RoomsRepository roomsRepository;

    private ListView        roomListView;
    private RoomListAdapter roomListAdapter;

    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms_overview);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);

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

                                Toast.makeText(getApplicationContext(), "Removed Room", Toast.LENGTH_SHORT).show();
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
        Intent roomPlay = new Intent(this, RoomHostActivity.class);
        roomPlay.putExtra("roomId", room.getId());
        startActivity(roomPlay);
        this.overridePendingTransition(R.anim.animation_sub_enter, R.anim.animation_main_leave);
    }

    public void openRoomPlaylist(Room room) {
        Intent roomPlay = new Intent(this, RoomContributorActivity.class);
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
            showDialog();

            return true;
        }
        if (id == R.id.rooms_overview_action_refresh) {
            Toast.makeText(getApplicationContext(), R.string.rooms_overview_action_refresh_message, Toast.LENGTH_SHORT).show();

            menuItem = item;
            menuItem.setActionView(R.layout.progressbar);
            menuItem.expandActionView();
            TestTask task = new TestTask();
            task.execute("test");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Room room  = roomListAdapter.getItem(position);
        openRoomPlaylist(room);
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rooms_overview_dialog_title);

        // Make a custom text field
        final EditText name = new EditText(this);
        name.setHint(R.string.rooms_overview_dialog_field);
        name.setSingleLine();
        builder.setView(name);

        builder.setPositiveButton(R.string.rooms_overview_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String roomName  = name.getText().toString();

                if(roomName.length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.rooms_overview_dialog_field_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get android id to indentify room owner
                String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                Room room = new Room(roomName, androidId);
                roomsRepository.save(room);

                openRoom(room);
            }
        });
        builder.setNegativeButton(R.string.rooms_overview_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class TestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // Simulate something long running
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            menuItem.collapseActionView();
            menuItem.setActionView(null);

            Toast.makeText(getApplicationContext(), R.string.rooms_overview_action_refresh_noresults, Toast.LENGTH_SHORT).show();
        }
    };

}
