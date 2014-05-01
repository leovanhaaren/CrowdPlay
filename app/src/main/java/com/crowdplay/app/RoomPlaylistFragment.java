package com.crowdplay.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crowdplay.app.adapters.VoteListAdapter;
import com.crowdplay.app.data.TracksRepository;
import com.crowdplay.app.model.Track;

import java.util.ArrayList;


public class RoomPlaylistFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "RoomPlaylistFragment";

    private ArrayList<Track> playlist;
    private TracksRepository tracksRepository;

    private MusicService     musicService;

    private ListView         mListView;
    private VoteListAdapter  mAdapter;

    private Activity mContext;
    private View     mView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.v(TAG, "Initializing room playlist");

        mContext = getActivity();
        mView    = getView();

        if(mContext instanceof MainActivity) {
            musicService = ((MainActivity) mContext).getMusicService();
        }

        tracksRepository = new TracksRepository(getActivity());

        mListView     = (ListView)    mView.findViewById(R.id.listView);

        playlist = new ArrayList<Track>(musicService.getRoom().getPlaylist());
        mAdapter = new VoteListAdapter(mContext, playlist);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        // Display the mRoom's name as title for the mContext
        mContext.getActionBar().setTitle(musicService.getRoom().getName());

        checkEmptyState();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room_playlist, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.room_playlist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.room_playlist_action_add:
                addMusic();

                return true;
            case R.id.room_playlist_action_disconnect:
                musicService.unloadRoom();

                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new RoomOverviewFragment())
                        .commit();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkEmptyState() {
        mListView.setVisibility((mListView.getCount() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mAdapter.toggleVote(position);
    }

    public void addMusic() {
        FragmentManager fragmentManager = getFragmentManager();
        MusicOverviewFragment fragment  = new MusicOverviewFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

}
