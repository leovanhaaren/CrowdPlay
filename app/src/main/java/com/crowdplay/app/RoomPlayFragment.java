package com.crowdplay.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crowdplay.app.MusicService.MusicBinder;
import com.crowdplay.app.adapters.PlayListAdapter;
import com.crowdplay.app.data.RoomsRepository;
import com.crowdplay.app.data.TracksRepository;
import com.crowdplay.app.model.Room;
import com.crowdplay.app.model.Track;

import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;
import de.timroes.android.listview.EnhancedListView.OnDismissCallback;


public class RoomPlayFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "RoomPlayActivity";

    private ArrayList<Track> playlist;
    private RoomsRepository  roomsRepository;
    private TracksRepository tracksRepository;

    private EnhancedListView mListView;
    private PlayListAdapter  mAdapter;

    private int  mRoomId;
    private Room mRoom;

    private MusicService musicService;
    private Intent       playIntent;

    private boolean musicBound = false;

    private Handler  mHandler = new Handler();

    private TextView currentTitle;
    private TextView currentArtist;

    private ImageButton playButton;

    private Activity mContext;
    private View mView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.v(TAG, "Initializing room play");

        mContext = getActivity();
        mView    = getView();

        roomsRepository  = new RoomsRepository(getActivity());
        tracksRepository = new TracksRepository(getActivity());
        mRoom            = roomsRepository.getRoom(mRoomId);

        currentTitle  = (TextView)         mView.findViewById(R.id.currentTitle);
        currentArtist = (TextView)         mView.findViewById(R.id.currentArtist);
        playButton    = (ImageButton)      mView.findViewById(R.id.playButton);
        mListView     = (EnhancedListView) mView.findViewById(R.id.listView);

        playButton.setOnClickListener(this);
        mListView.setOnItemClickListener(this);

        playlist = new ArrayList<Track>(mRoom.getPlaylist());
        mAdapter = new PlayListAdapter(mContext, playlist);

        mListView.setAdapter(mAdapter);
        mListView.setDismissCallback(new OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {

                final Track item = (Track) mAdapter.getItem(position);
                mAdapter.remove(position);
                checkEmptyState();
                return new EnhancedListView.Undoable() {
                    // Reinsert the item to the adapter
                    @Override
                    public void undo() {
                        mAdapter.insert(position, item);
                        checkEmptyState();
                    }

                    // Delete item completely from your persistent storage
                    @Override
                    public void discard() {
                        tracksRepository.delete(item);
                        checkEmptyState();
                    }
                };
            }
        });
        mListView.enableSwipeToDismiss();
        mListView.setUndoStyle(EnhancedListView.UndoStyle.COLLAPSED_POPUP);

        // Display the mRoom's name as title for the mContext
        mContext.getActionBar().setTitle(mRoom.getName());

        // Check if we have any results
        checkEmptyState();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room_play_simple, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.room_play, menu);
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;

            musicService = binder.getService();
            musicBound   = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        if(playIntent == null){
            playIntent = new Intent(mContext, MusicService.class);
            playIntent.putExtra("roomId", mRoomId);
            mContext.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            mContext.startService(playIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mContext.stopService(playIntent);
        musicService = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.room_play_action_add:
                addMusic();

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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Track track  = mAdapter.getItem(position);
        musicService.setCurrentTrack(track);
    }

    public void onClick(View view) {
        playMusic();
    }

    public void playMusic() {
        if(musicService == null) return;

        // Play song if we can
        musicService.playSong();

        Track currentTrack = musicService.getCurrentTrack();
        if(currentTrack == null) return;

        currentTitle.setText(currentTrack.getTitle());
        currentTitle.setSelected(true);
        playButton.setImageResource(R.drawable.ic_action_pause);

        // check for already playing
        if(musicService.isPlaying()) {
            musicService.pause();

            currentTitle.setSelected(false);
            playButton.setImageResource(R.drawable.ic_action_play);
        } else {
            musicService.start();

            currentTitle.setSelected(true);
            playButton.setImageResource(R.drawable.ic_action_pause);
        }
    }

    public void addMusic() {
        FragmentManager fragmentManager = getFragmentManager();
        MusicOverviewFragment fragment  = new MusicOverviewFragment();

        fragment.setRoomId(mRoom.getId());
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    public void showEditDialog() {
        LayoutInflater inflater = mContext.getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.dialog_layout, (ViewGroup) mView.findViewById(R.id.dialogLayout));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        EditText name   = (EditText) dialoglayout.findViewById(R.id.name);

        builder.setTitle(R.string.room_play_dialog_title);
        name.setText(mRoom.getName());

        builder.setView(dialoglayout);builder.setPositiveButton(R.string.room_play_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText name   = (EditText) dialoglayout.findViewById(R.id.name);
                String roomName = name.getText().toString();

                if(roomName.length() == 0) {
                    Toast.makeText(mContext.getApplicationContext(), R.string.room_play_dialog_field_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                mRoom.setName(roomName);
                roomsRepository.update(mRoom);

                mContext.getActionBar().setTitle(mRoom.getName());
                Toast.makeText(mContext.getApplicationContext(), R.string.room_play_dialog_succes, Toast.LENGTH_SHORT).show();
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

    public void setRoomId(int mRoomId) {
        this.mRoomId = mRoomId;
    }
}
