package com.jvrhenen.crowdplay.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jvrhenen.crowdplay.app.MusicService.MusicBinder;
import com.jvrhenen.crowdplay.app.adapters.PlayListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.model.Room;
import com.jvrhenen.crowdplay.app.model.Track;
import com.jvrhenen.crowdplay.app.utils.Util;

import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;
import de.timroes.android.listview.EnhancedListView.OnDismissCallback;


public class RoomPlayActivity extends Activity implements AdapterView.OnItemClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "RoomPlayActivity";

    private ArrayList<Track> playlist;
    private RoomsRepository  roomsRepository;

    private EnhancedListView mListView;
    private PlayListAdapter  mAdapter;

    private int roomId;
    private Room room;

    private MusicService musicService;
    private Intent       playIntent;

    private boolean musicBound = false;

    private Handler  mHandler = new Handler();

    private TextView currentTitle;
    private TextView currentTime;
    private TextView totalTime;

    private SeekBar  seekBar;

    private ImageButton prevButton;
    private ImageButton playButton;
    private ImageButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_play);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        roomId = getIntent().getExtras().getInt("roomId");

        roomsRepository = new RoomsRepository(this);

        room     = roomsRepository.getRoom(roomId);
        playlist = new ArrayList<Track>(room.getPlaylist());

        currentTitle = (TextView)findViewById(R.id.currentTitle);
        currentTime  = (TextView)findViewById(R.id.currentTime);
        totalTime    = (TextView)findViewById(R.id.totalTime);

        seekBar      = (SeekBar)findViewById(R.id.seekBar);

        prevButton   = (ImageButton)findViewById(R.id.prevButton);
        playButton   = (ImageButton)findViewById(R.id.playButton);
        nextButton   = (ImageButton)findViewById(R.id.nextButton);

        mListView = (EnhancedListView)findViewById(R.id.listView);
        mAdapter  = new PlayListAdapter(this, playlist);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

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

        // Display the room's name as title for the activity
        getActionBar().setTitle(room.getName());

        // Check if we have any results
        checkEmptyState();

        playButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                if(musicService.getPlayer().isPlaying()){
                    if(musicService.getPlayer() != null){
                        musicService.getPlayer().pause();

                        currentTitle.setSelected(false);
                        playButton.setImageResource(R.drawable.ic_action_play);
                    }
                }else{
                    // Resume song
                    if(musicService.getPlayer() != null){
                        musicService.getPlayer().start();

                        currentTitle.setSelected(true);
                        playButton.setImageResource(R.drawable.ic_action_pause);
                    }
                }

            }
        });
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;

            musicService = binder.getService();

            musicService.setPlayList(playlist);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Track track = mAdapter.getItem(position);
        if(musicService != null && track != null) {
            musicService.setTrack(track);
            musicService.playSong();

            currentTitle.setSelected(true);
            playButton.setImageResource(R.drawable.ic_action_pause);
        }

        // set Progress bar values
        seekBar.setProgress(0);
        seekBar.setMax(100);

        currentTitle.setText(track.getTitle());

        // Updating progress bar
        updateProgressBar();
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration   = musicService.getPlayer().getDuration();
            long currentDuration = musicService.getPlayer().getCurrentPosition();

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

    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration   = musicService.getPlayer().getDuration();
        int currentPosition = Util.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        musicService.getPlayer().seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
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
    protected void onDestroy() {
        super.onDestroy();

        stopService(playIntent);
        musicService = null;
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
