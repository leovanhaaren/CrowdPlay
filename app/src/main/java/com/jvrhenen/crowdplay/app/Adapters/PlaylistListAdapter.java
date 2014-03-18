package com.jvrhenen.crowdplay.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jvrhenen.crowdplay.app.R;
import com.jvrhenen.crowdplay.app.model.Track;

import java.util.ArrayList;

/**
 * Created by Leo on 17/03/14.
 */
public class PlaylistListAdapter extends BaseAdapter {

    private Context          context;
    private ArrayList<Track> tracks;
    private LayoutInflater   inflater;

    public PlaylistListAdapter(Context c, ArrayList<Track> e) {
        inflater = LayoutInflater.from(c);
        context  = c;
        tracks   = e;
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Track getItem(int position) {
        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            v = inflater.inflate(R.layout.playlist_list_item, parent, false);
            v.setTag(R.id.playlist_list_item_title,  v.findViewById(R.id.playlist_list_item_title));
            v.setTag(R.id.playlist_list_item_artist, v.findViewById(R.id.playlist_list_item_artist));
            v.setTag(R.id.playlist_list_item_art,    v.findViewById(R.id.playlist_list_item_art));
        }

        TextView  title  = (TextView) v.getTag(R.id.playlist_list_item_title);
        TextView  artist = (TextView) v.getTag(R.id.playlist_list_item_artist);
        ImageView art    = (ImageView)v.getTag(R.id.playlist_list_item_art);

        Track track = getItem(position);

        title.setText(track.getTitle());
        artist.setText(track.getArtist());

        // Set album art
        // TODO: FIX ALBUM ARTWORK
        //int resourceId = context.getResources().getIdentifier(track.getArt().getIcon(), "drawable", context.getPackageName());
        //art.setImageResource(resourceId);

        return v;
    }

}