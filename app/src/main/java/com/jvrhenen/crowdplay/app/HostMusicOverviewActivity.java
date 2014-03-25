package com.jvrhenen.crowdplay.app;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jvrhenen.crowdplay.app.adapters.TrackAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.data.TracksRepository;
import com.jvrhenen.crowdplay.app.model.Room;
import com.jvrhenen.crowdplay.app.model.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HostMusicOverviewActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = "HostMusicOverviewActivity";

    private ArrayList<Track> tracks;

    private TracksRepository tracksRepository;
    private RoomsRepository  roomsRepository;

    private ListView     mListView;
    private TrackAdapter mAdapter;

    private int roomId;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_overview);

        roomId = getIntent().getExtras().getInt("roomId");

        tracksRepository = new TracksRepository(this);
        roomsRepository  = new RoomsRepository(this);

        room      = roomsRepository.getRoom(roomId);

        mListView = (ListView)findViewById(R.id.listView);

        tracks = new ArrayList<Track>();

        retrieveTracksFromDevice();

        //sort alphabetically by title
        Collections.sort(tracks, new Comparator<Track>() {
            public int compare(Track a, Track b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        mAdapter = new TrackAdapter(this, tracks);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        // Check if we have any results
        checkEmptyState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                this.overridePendingTransition(R.anim.animation_main_enter, R.anim.animation_sub_leave);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Track track  = mAdapter.getItem(position);
        track.setRoom(room);

        tracksRepository.create(track);

        this.overridePendingTransition(R.anim.animation_main_enter, R.anim.animation_sub_leave);
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    public void checkEmptyState() {
        mListView.setVisibility((mAdapter.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    //method to retrieve song info from device
    public void retrieveTracksFromDevice(){
        //query external audio
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        //iterate over results if valid
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int idColumn       = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn    = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn   = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn    = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            //add songs to list
            do {
                long   id     = musicCursor.getLong(idColumn);
                String title  = musicCursor.getString(titleColumn);
                String artist = musicCursor.getString(artistColumn);
                String album  = musicCursor.getString(albumColumn);
                long duration = musicCursor.getLong(durationColumn);

                Track track = new Track(id, title, artist, album, duration);
                tracks.add(track);
            }
            while (musicCursor.moveToNext());
        }
    }

}
