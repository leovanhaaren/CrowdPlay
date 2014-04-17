package com.crowdplay.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crowdplay.app.R;
import com.crowdplay.app.model.Track;
import com.crowdplay.app.utils.Convert;

import java.util.ArrayList;

/**
 * Created by Leo on 17/03/14.
 */
public class TrackAdapter extends BaseAdapter {

    private Context          context;
    private ArrayList<Track> items;
    private LayoutInflater   inflater;

    public TrackAdapter(Context c, ArrayList<Track> e) {
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

    public void replaceList(ArrayList<Track> items) {
        items = items;
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
            v = inflater.inflate(R.layout.track_list_item, parent, false);
            v.setTag(R.id.title,    v.findViewById(R.id.title));
            v.setTag(R.id.artist,   v.findViewById(R.id.artist));
            v.setTag(R.id.duration, v.findViewById(R.id.duration));
            v.setTag(R.id.art,      v.findViewById(R.id.art));
        }

        TextView  title    = (TextView) v.getTag(R.id.title);
        TextView  artist   = (TextView) v.getTag(R.id.artist);
        TextView  duration = (TextView) v.getTag(R.id.duration);
        ImageView art      = (ImageView)v.getTag(R.id.art);

        Track track = getItem(position);

        title.setText(track.getTitle());
        artist.setText(track.getArtist());
        duration.setText(Convert.getDurationBreakdown(track.getDuration()));

        // Set album art
        if(track.getBitmap() != null)
            art.setImageBitmap(track.getBitmap());

        return v;
    }

}