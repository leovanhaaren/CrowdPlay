package com.crowdplay.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crowdplay.app.R;
import com.crowdplay.app.model.Room;
import com.crowdplay.app.model.Track;

import java.util.ArrayList;

/**
 * Created by Leo on 17/03/14.
 */
public class RoomListAdapter extends BaseAdapter {

    private Context         context;
    private ArrayList<Room> items;
    private LayoutInflater  inflater;

    public RoomListAdapter(Context c, ArrayList<Room> e) {
        inflater = LayoutInflater.from(c);
        context  = c;
        items = e;
    }

    public void remove(int position) {
        items.remove(position);
        notifyDataSetChanged();
    }

    public void insert(int position, Room item) {
        items.add(position, item);
        notifyDataSetChanged();
    }

    public void replaceList(ArrayList<Room> items) {
        items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Room getItem(int position) {
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
            v = inflater.inflate(R.layout.room_list_item, parent, false);
            v.setTag(R.id.room_list_item_name,    v.findViewById(R.id.room_list_item_name));
            v.setTag(R.id.room_list_item_queue,   v.findViewById(R.id.room_list_item_queue));
            v.setTag(R.id.room_list_item_playing, v.findViewById(R.id.room_list_item_playing));
            v.setTag(R.id.room_list_item_track,   v.findViewById(R.id.room_list_item_track));
            v.setTag(R.id.room_list_item_art,     v.findViewById(R.id.room_list_item_art));
        }

        TextView  name    = (TextView) v.getTag(R.id.room_list_item_name);
        TextView  queue   = (TextView) v.getTag(R.id.room_list_item_queue);
        TextView  playing = (TextView) v.getTag(R.id.room_list_item_playing);
        TextView  track   = (TextView) v.getTag(R.id.room_list_item_track);
        ImageView art     = (ImageView)v.getTag(R.id.room_list_item_art);

        Room  room         = getItem(position);
        Track currentTrack = room.getCurrentTrack();

        // Set room name
        name.setText(room.getName());

        // Set queue
        if(room.getPlaylist() != null || room.getPlaylist().size() == 0)
            queue.setText(room.getPlaylist().size() + " items in queue");
        else
            queue.setText("No items in queue");

        // Set current track
        if(currentTrack != null) {
            playing.setText("Currently playing");
            track.setText(currentTrack.getTitle());
        } else {
            playing.setText("");
            track.setText("");
        }

        // Set album art
        if(currentTrack != null)
            if(currentTrack.getBitmap() != null)
                art.setImageBitmap(currentTrack.getBitmap());

        return v;
    }

}