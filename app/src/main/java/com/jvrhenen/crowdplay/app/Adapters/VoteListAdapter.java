package com.jvrhenen.crowdplay.app.adapters;

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
public class VoteListAdapter extends BaseAdapter {

    private Context          context;
    private ArrayList<Track> items;
    private LayoutInflater   inflater;

    public VoteListAdapter(Context c, ArrayList<Track> e) {
        inflater = LayoutInflater.from(c);
        context  = c;
        items = e;
    }

    public void remove(int position) {
        items.remove(position);
        notifyDataSetChanged();
    }

    public void insert(int position, Track item) {
        items.add(position, item);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Track getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            v = inflater.inflate(R.layout.vote_list_item, parent, false);
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