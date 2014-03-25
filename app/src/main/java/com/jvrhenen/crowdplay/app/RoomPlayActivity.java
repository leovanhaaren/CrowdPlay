package com.jvrhenen.crowdplay.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.jvrhenen.crowdplay.app.adapters.PlayListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.model.Room;
import com.jvrhenen.crowdplay.app.model.Track;

import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;
import de.timroes.android.listview.EnhancedListView.OnDismissCallback;


public class RoomPlayActivity extends Activity {

    private static final String TAG = "RoomPlayActivity";

    private ArrayList<Track> playlist;
    private RoomsRepository  roomsRepository;

    private EnhancedListView mListView;
    private PlayListAdapter  mAdapter;

    private int roomId;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_play);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        roomId = getIntent().getExtras().getInt("roomId");

        roomsRepository = new RoomsRepository(this);

        room     = roomsRepository.getRoom(roomId);
        playlist = new ArrayList<Track>(room.getPlaylist());

        mListView = (EnhancedListView)findViewById(R.id.listView);
        mAdapter = new PlayListAdapter(this, playlist);
        mListView.setAdapter(mAdapter);

        // Set the callback that handles dismisses.
        mListView.setDismissCallback(new OnDismissCallback() {

            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {

                final Track item = (Track) mAdapter.getItem(position);
                mAdapter.remove(position);
                return new EnhancedListView.Undoable() {
                    // Reinsert the item to the adapter
                    @Override
                    public void undo() {
                        mAdapter.insert(position, item);
                    }
                    // Delete item completely from your persistent storage
                    @Override public void discard() {
                        //roomsRepository.delete(item);
                    }
                };
            }
        });
        mListView.enableSwipeToDismiss();
        mListView.setUndoStyle(EnhancedListView.UndoStyle.COLLAPSED_POPUP);
        mListView.setUndoHideDelay(3000);

        // Display the room's name as title for the activity
        getActionBar().setTitle(room.getName());

        // Check if we have any results
        checkEmptyState();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh listview, in case we added a track
        playlist = new ArrayList<Track>(room.getPlaylist());
        mAdapter = new PlayListAdapter(this, playlist);
        mListView.setAdapter(mAdapter);
        mListView.invalidate();

        // Check if we have any results
        checkEmptyState();

        Log.i(TAG, "Activity Life Cycle : onResume : Activity Resumed");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);

                this.overridePendingTransition(R.anim.animation_main_enter, R.anim.animation_sub_leave);
                return true;
            case R.id.room_play_action_edit:
                showEditDialog();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkEmptyState() {
        mListView.setVisibility((mListView.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    public void addMusic(View view) {
        Intent intent = new Intent(this, ContributorMusicOverviewActivity.class);
        intent.putExtra("roomId", roomId);
        startActivityForResult(intent, 1);

        this.overridePendingTransition(R.anim.animation_sub_enter, R.anim.animation_main_leave);
    }

    public void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.room_play_dialog_title);

        // Make a custom text field
        final EditText name = new EditText(this);
        name.setHint(R.string.room_play_dialog_field);
        name.setSingleLine();
        name.setText(room.getName());
        builder.setView(name);

        builder.setPositiveButton(R.string.room_play_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String roomName  = name.getText().toString();

                if(roomName.length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.room_play_dialog_field_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                room.setName(roomName);
                roomsRepository.update(room);

                getActionBar().setTitle(room.getName());
                Toast.makeText(getApplicationContext(), R.string.room_play_dialog_succes, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.room_overview_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
