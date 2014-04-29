package com.crowdplay.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
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

    private ArrayList<Room> mRooms;
    private RoomsRepository mRoomsRepository;

    private EnhancedListView mListView;
    private RoomListAdapter  mAdapter;

    private MenuItem mMenuItem;

    private Activity mContext;
    private View     mView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.v(TAG, "Initializing room overview");

        mContext = getActivity();
        mView    = getView();

        mRoomsRepository = new RoomsRepository(mContext);
        mRooms = mRoomsRepository.getAll();

        mListView = (EnhancedListView) mView.findViewById(R.id.listView);
        mAdapter  = new RoomListAdapter(mContext, mRooms);
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
                        mRoomsRepository.delete(item);
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

        inflater.inflate(R.menu.room_overview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rooms_overview_action_add:
                showDialog();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Room room  = mAdapter.getItem(position);
        openRoom(room);
    }

    public void checkEmptyState() {
        mListView.setVisibility((mAdapter.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    public void showDialog() {
        LayoutInflater inflater = mContext.getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.dialog_layout, (ViewGroup) mView.findViewById(R.id.dialogLayout));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle(R.string.room_overview_dialog_title);
        builder.setView(dialoglayout);
        builder.setPositiveButton(R.string.room_overview_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText name   = (EditText) dialoglayout.findViewById(R.id.name);
                String roomName = name.getText().toString();

                if(roomName.length() == 0) {
                    Toast.makeText(mContext, R.string.room_overview_dialog_field_empty, Toast.LENGTH_SHORT).show();
                } else {
                    // Get android id to indentify room owner
                    String androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);

                    Room room = new Room(roomName, androidId);
                    mRoomsRepository.save(room);

                    openRoom(room);
                }
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

    public void openRoom(Room room) {
        FragmentManager  fragmentManager = getFragmentManager();
        RoomPlayFragment fragment        = new RoomPlayFragment();

        fragment.setRoomId(room.getId());
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

}
