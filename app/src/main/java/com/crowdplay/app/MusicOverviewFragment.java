package com.crowdplay.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.crowdplay.app.adapters.TrackAdapter;
import com.crowdplay.app.data.RoomsRepository;
import com.crowdplay.app.data.TracksRepository;
import com.crowdplay.app.model.Room;
import com.crowdplay.app.model.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MusicOverviewFragment extends Fragment implements AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener {

    private static final String TAG = "MusicOverviewActivity";

    private ArrayList<Track> tracks;

    private TracksRepository tracksRepository;
    private RoomsRepository  roomsRepository;

    private MusicService     musicService;

    private ListView     mListView;
    private TrackAdapter mAdapter;

    private Activity mContext;
    private View mView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.v(TAG, "Initializing music overview");

        mContext = getActivity();
        mView    = getView();

        if(mContext instanceof MainActivity) {
            musicService = ((MainActivity) mContext).getMusicService();
        }

        tracksRepository = new TracksRepository(getActivity());
        roomsRepository  = new RoomsRepository(getActivity());

        mListView = (ListView) mView.findViewById(R.id.listView);

        tracks = new ArrayList<Track>();

        retrieveTracksFromDevice();

        //sort alphabetically by title
        Collections.sort(tracks, new Comparator<Track>() {
            public int compare(Track a, Track b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        mAdapter = new TrackAdapter(getActivity(), tracks);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        // Only allow host to select multiple tracks
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);

        // Check if we have any results
        checkEmptyState();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_overview, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.music_overview, menu);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Room room = musicService.getRoom();
        if(room == null) {
            room = new Room(android.os.Build.MODEL, musicService.getDeviceID());
            roomsRepository.save(room);
        }

        Track track  = mAdapter.getItem(position);
        track.setRoom(room);

        tracksRepository.create(track);

        openRoom(room);
    }

    public void checkEmptyState() {
        mListView.setVisibility((mAdapter.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    //method to retrieve song info from device
    public void retrieveTracksFromDevice(){
        //query external audio
        ContentResolver musicResolver = getActivity().getContentResolver();

        Uri    musicUri  = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String order     = MediaStore.Audio.Media.TITLE    + " ASC";

        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, order);

        //iterate over results if valid
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int idColumn       = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn    = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn   = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn    = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int albumIdColumn  = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            //add songs to list
            do {
                long   id     = musicCursor.getLong(idColumn);
                String title  = musicCursor.getString(titleColumn);
                String artist = musicCursor.getString(artistColumn);
                String album  = musicCursor.getString(albumColumn);
                long duration = musicCursor.getLong(durationColumn);
                long albumId  = musicCursor.getLong(albumIdColumn);

                Track track = new Track(id, title, artist, album, albumId, duration);

                tracks.add(track);
            }
            while (musicCursor.moveToNext());
        }
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        final int checkedCount = mListView.getCheckedItemCount();
        mode.setTitle(checkedCount + " selected");
        mAdapter.toggleSelection(position);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                addTracksToRoom(musicService.getRoom());

                String message = mContext.getResources().getString(R.string.music_overview_context_add_selection, mListView.getCheckedItemCount());
                Toast.makeText(mContext.getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                mode.finish();
                openRoom(musicService.getRoom());

                return true;
            case R.id.host:
                // TODO: change this back to device id
                Room room = new Room(android.os.Build.MODEL, "123");
                roomsRepository.save(room);

                addTracksToRoom(room);

                mode.finish();
                openRoom(room);

                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        if(!musicService.hasRoom())
            inflater.inflate(R.menu.music_overview_context_add, menu);
        else
            inflater.inflate(R.menu.music_overview_context_host, menu);

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public void addTracksToRoom(Room room) {
        SparseBooleanArray selected = mAdapter.getSelectedIds();
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)) {
                Track track  = mAdapter.getItem(selected.keyAt(i));
                track.setRoom(room);

                tracksRepository.create(track);
            }
        }
    }

    public void openRoom(Room room) {
        // Load selected room into service for playback
        musicService.loadRoom(room);

        // Check if we own the current room
        if(musicService.isHost()) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new RoomPlayFragment())
                    .addToBackStack(null)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new RoomPlaylistFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

}
