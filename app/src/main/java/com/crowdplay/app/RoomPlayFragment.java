package com.crowdplay.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.crowdplay.app.MusicService.MusicBinder;
import com.crowdplay.app.adapters.PlayListAdapter;
import com.crowdplay.app.data.RoomsRepository;
import com.crowdplay.app.model.Room;
import com.crowdplay.app.model.Track;
import com.crowdplay.app.utils.Util;

import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;
import de.timroes.android.listview.EnhancedListView.OnDismissCallback;


public class RoomPlayFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "RoomPlayActivity";

    private ArrayList<Track> playlist;
    private RoomsRepository  roomsRepository;

    private EnhancedListView mListView;
    private PlayListAdapter  mAdapter;

    private int  mRoomId;
    private Room mRoom;

    private MusicService musicService;
    private Intent       playIntent;

    private boolean musicBound = false;

    private Handler  mHandler = new Handler();

    private TextView currentTitle;
    private TextView currentTime;
    private TextView totalTime;

    private SeekBar  seekBar;

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
        mRoomId  = getArguments().getInt("roomId");

        roomsRepository = new RoomsRepository(getActivity());
        mRoom           = roomsRepository.getRoom(mRoomId);

        currentTitle = (TextView)         mView.findViewById(R.id.currentTitle);
        currentTime  = (TextView)         mView.findViewById(R.id.currentTime);
        totalTime    = (TextView)         mView.findViewById(R.id.totalTime);
        seekBar      = (SeekBar)          mView.findViewById(R.id.seekBar);
        playButton   = (ImageButton)      mView.findViewById(R.id.playButton);
        mListView    = (EnhancedListView) mView.findViewById(R.id.listView);

        playlist = new ArrayList<Track>(mRoom.getPlaylist());
        mAdapter = new PlayListAdapter(mContext, playlist);

        mListView.setAdapter(mAdapter);
        seekBar.setOnSeekBarChangeListener(this);
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
                    @Override
                    public void discard() {
                        //roomsRepository.delete(item);
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
        return inflater.inflate(R.layout.fragment_room_play, container, false);
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

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration   = musicService.getDuration();
            long currentDuration = musicService.getPosition();

            // Displaying Total Duration time
            totalTime.setText("" + Util.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            currentTime.setText("" + Util.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(Util.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            seekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration   = musicService.getDuration();
        int currentPosition = Util.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        musicService.seek(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh listview, in case we added a track
        playlist = new ArrayList<Track>(mRoom.getPlaylist());
        mAdapter = new PlayListAdapter(mContext, playlist);
        mListView.setAdapter(mAdapter);
        mListView.invalidate();

        // Check if we have any results
        checkEmptyState();
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
            case R.id.room_play_action_edit:
                //showEditDialog();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkEmptyState() {
        mListView.setVisibility((mListView.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    public void playMusic(View view) {
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

        // set Progress bar values
        seekBar.setProgress(0);
        seekBar.setMax(100);

        //updateProgressBar();
    }

    /*public void addMusic(View view) {
        Intent intent = new Intent(this, ContributorMusicOverviewActivity.class);
        intent.putExtra("mRoomId", mRoomId);
        startActivity(intent);
    }

    public void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.room_play_dialog_title);

        // Make a custom text field
        final EditText name = new EditText(mContext);
        name.setHint(R.string.room_play_dialog_field);
        name.setSingleLine();
        name.setText(mRoom.getName());
        builder.setView(name);

        builder.setPositiveButton(R.string.room_play_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String roomName  = name.getText().toString();

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
    }*/

}
