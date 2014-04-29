package com.crowdplay.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crowdplay.app.adapters.TrackAdapter;
import com.crowdplay.app.data.RoomsRepository;
import com.crowdplay.app.data.TracksRepository;
import com.crowdplay.app.model.Room;
import com.crowdplay.app.model.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MusicOverviewFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "MusicOverviewActivity";

    private ArrayList<Track> tracks;

    private TracksRepository tracksRepository;
    private RoomsRepository  roomsRepository;

    private ListView     mListView;
    private TrackAdapter mAdapter;

    private int  roomId;
    private Room room;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.v(TAG, "Initializing music overview");

        View v = getView();

        tracksRepository = new TracksRepository(getActivity());
        roomsRepository  = new RoomsRepository(getActivity());

        Log.v(TAG, "Room id: " + roomId);
        room      = roomsRepository.getRoom(roomId);

        mListView = (ListView) v.findViewById(R.id.listView);

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
        Track track  = mAdapter.getItem(position);
        track.setRoom(room);

        tracksRepository.create(track);

        Log.v(TAG, "Added track: " + track.getTitle() + " to room: " + roomId);

        FragmentManager fragmentManager = getFragmentManager();
        RoomPlayFragment fragment       = new RoomPlayFragment();

        fragment.setRoomId(room.getId());
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
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

                /*Uri sArtworkUri     = Uri.parse("content://media/external/audio/albumart");
                Uri uri             = ContentUris.withAppendedId(sArtworkUri, albumId);
                ContentResolver res = getActivity().getContentResolver();

                try {
                    InputStream in      = res.openInputStream(uri);
                    Bitmap artwork      = BitmapFactory.decodeStream(in);

                    track.setBitmap(artwork);

                } catch(FileNotFoundException e) {
                    Log.v(TAG, e.toString());
                }*/

                tracks.add(track);
            }
            while (musicCursor.moveToNext());
        }
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

}
