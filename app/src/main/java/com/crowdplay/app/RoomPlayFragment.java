package com.crowdplay.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crowdplay.app.adapters.PlayListAdapter;
import com.crowdplay.app.data.RoomsRepository;
import com.crowdplay.app.data.TracksRepository;
import com.crowdplay.app.model.Track;

import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;
import de.timroes.android.listview.EnhancedListView.OnDismissCallback;


public class RoomPlayFragment extends Fragment implements View.OnClickListener, OnDismissCallback {

    private static final String TAG = "RoomPlayFragment";

    private ArrayList<Track> playlist;
    private RoomsRepository  roomsRepository;
    private TracksRepository tracksRepository;

    private MusicService     musicService;

    private EnhancedListView mListView;
    private PlayListAdapter  mAdapter;

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

        if(mContext instanceof MainActivity) {
            musicService = ((MainActivity) mContext).getMusicService();
        }

        roomsRepository  = new RoomsRepository(getActivity());
        tracksRepository = new TracksRepository(getActivity());

        currentTitle  = (TextView)         mView.findViewById(R.id.currentTitle);
        currentArtist = (TextView)         mView.findViewById(R.id.currentArtist);
        playButton    = (ImageButton)      mView.findViewById(R.id.playButton);
        mListView     = (EnhancedListView) mView.findViewById(R.id.listView);

        playButton.setOnClickListener(this);

        playlist = new ArrayList<Track>(musicService.getRoom().getPlaylist());
        mAdapter = new PlayListAdapter(mContext, playlist);

        mListView.setAdapter(mAdapter);
        mListView.setDismissCallback(this);
        mListView.enableSwipeToDismiss();
        mListView.setUndoStyle(EnhancedListView.UndoStyle.COLLAPSED_POPUP);
        mListView.setRequireTouchBeforeDismiss(false);

        // Display the mRoom's name as title for the mContext
        mContext.getActionBar().setTitle(musicService.getRoom().getName());

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

    public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {

        final Track item = (Track) mAdapter.getItem(position);
        mAdapter.remove(position);
        checkEmptyState();

        return new EnhancedListView.Undoable() {

            @Override
            public void undo() {
                mAdapter.insert(position, item);
                checkEmptyState();
            }

            @Override
            public void discard() {
                tracksRepository.delete(item);
                checkEmptyState();
            }
        };
    }

    public void onClick(View view) {
        playMusic();
    }

    public void initPlaylist() {
        if(musicService == null) return;

        musicService.initPlaylist();

        Track currentTrack = musicService.getCurrentTrack();

        currentTitle.setSelected(true);
        currentArtist.setSelected(true);
        currentTitle.setText(currentTrack.getTitle());
        currentArtist.setText(currentTrack.getArtist());
    }

    public void playMusic() {
        if(musicService == null) return;

        if(musicService.getCurrentTrack() == null) {
            Toast.makeText(mContext.getApplicationContext(), R.string.room_play_emptystate_title, Toast.LENGTH_SHORT).show();
            initPlaylist();

            return;
        }

        // check for already playing
        if(musicService.isPlaying()) {
            musicService.pause();

            currentTitle.setSelected(false);
            currentArtist.setSelected(false);
            currentTitle.setText(musicService.getCurrentTrack().getTitle());
            currentArtist.setText(musicService.getCurrentTrack().getArtist());
            playButton.setImageResource(R.drawable.ic_action_play);
        } else {
            musicService.start();

            currentTitle.setSelected(true);
            currentArtist.setSelected(true);
            currentTitle.setText(musicService.getCurrentTrack().getTitle());
            currentArtist.setText(musicService.getCurrentTrack().getArtist());
            playButton.setImageResource(R.drawable.ic_action_pause);
        }
    }

    public void addMusic() {
        FragmentManager fragmentManager = getFragmentManager();
        MusicOverviewFragment fragment  = new MusicOverviewFragment();

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
        name.setText(musicService.getRoom().getName());

        builder.setView(dialoglayout);builder.setPositiveButton(R.string.room_play_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText name   = (EditText) dialoglayout.findViewById(R.id.name);
                String roomName = name.getText().toString();

                if(roomName.length() == 0) {
                    Toast.makeText(mContext.getApplicationContext(), R.string.room_play_dialog_field_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                musicService.getRoom().setName(roomName);
                roomsRepository.update(musicService.getRoom());

                mContext.getActionBar().setTitle(musicService.getRoom().getName());
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

}
