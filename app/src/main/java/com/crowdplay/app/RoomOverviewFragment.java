package com.crowdplay.app;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.crowdplay.app.adapters.RoomListAdapter;
import com.crowdplay.app.data.RoomsRepository;
import com.crowdplay.app.model.Room;

import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;
import de.timroes.android.listview.EnhancedListView.OnDismissCallback;


public class RoomOverviewFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "RoomOverviewFragment";

    private ArrayList<Room> rooms;
    private RoomsRepository roomsRepository;

    private EnhancedListView mListView;
    private RoomListAdapter  mAdapter;

    private MenuItem menuItem;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.v(TAG, "Initializing room overview");

        View v = getView();

        roomsRepository = new RoomsRepository(getActivity());
        rooms           = roomsRepository.getAll();

        mListView = (EnhancedListView) v.findViewById(R.id.listView);
        mAdapter = new RoomListAdapter(getActivity(), rooms);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);

        // Set the callback that handles dismisses.
        mListView.setDismissCallback(new OnDismissCallback() {

            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {

                final Room item = (Room) mAdapter.getItem(position);
                mAdapter.remove(position);
                return new EnhancedListView.Undoable() {
                    // Reinsert the item to the adapter
                    @Override
                    public void undo() {
                        mAdapter.insert(position, item);
                    }

                    // Delete item completely from your persistent storage
                    @Override
                    public void discard() {
                        roomsRepository.delete(item);
                    }
                };
            }
        });
        mListView.enableSwipeToDismiss();
        mListView.setUndoStyle(EnhancedListView.UndoStyle.MULTILEVEL_POPUP);
        mListView.setUndoHideDelay(5000);

        // Check if we have any results
        checkEmptyState();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room_overview, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.room_play, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rooms_overview_action_add:
                showDialog();

                return true;
            case R.id.rooms_overview_action_refresh:
                Toast.makeText(getActivity(), R.string.room_overview_action_refresh_message, Toast.LENGTH_SHORT).show();

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
        Room room  = mAdapter.getItem(position);
        //openRoomPlaylist(room);
    }

    public void checkEmptyState() {
        mListView.setVisibility((mAdapter.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    /*public void openRoom(Room room) {
        Intent intent = new Intent(getActivity(), RoomPlayActivity.class);
        intent.putExtra("roomId", room.getId());
        startActivity(intent);
        //this.overridePendingTransition(R.anim.animation_sub_enter, R.anim.animation_main_leave);
    }

    public void openRoomPlaylist(Room room) {
        Intent roomPlay = new Intent(this, RoomPlaylistActivity.class);
        roomPlay.putExtra("roomId", room.getId());
        startActivity(roomPlay);
        //this.overridePendingTransition(R.anim.animation_sub_enter, R.anim.animation_main_leave);
    }*/

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.room_overview_dialog_title);

        // Make a custom text field
        final EditText name = new EditText(getActivity());
        name.setHint(R.string.room_overview_dialog_field);
        name.setSingleLine();
        builder.setView(name);

        builder.setPositiveButton(R.string.room_overview_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String roomName  = name.getText().toString();

                if(roomName.length() == 0) {
                    Toast.makeText(getActivity(), R.string.room_overview_dialog_field_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get android id to indentify room owner
                String androidId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

                Room room = new Room(roomName, androidId);
                roomsRepository.save(room);

                //openRoom(room);
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

            Toast.makeText(getActivity(), R.string.room_overview_action_refresh_noresults, Toast.LENGTH_SHORT).show();
        }
    };

}
