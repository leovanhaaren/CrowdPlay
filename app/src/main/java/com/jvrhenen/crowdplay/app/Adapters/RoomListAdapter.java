package com.jvrhenen.crowdplay.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jvrhenen.crowdplay.app.R;
import com.jvrhenen.crowdplay.app.model.Room;

import java.util.ArrayList;

/**
 * Created by Leo on 17/03/14.
 */
public class RoomListAdapter extends BaseAdapter {

    private Context         context;
    private ArrayList<Room> rooms;
    private LayoutInflater  inflater;

    public RoomListAdapter(Context c, ArrayList<Room> e) {
        inflater = LayoutInflater.from(c);
        context  = c;
        rooms    = e;
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Object getItem(int position) {
        return rooms.get(position);
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
            v.setTag(R.id.room_list_item_name,  v.findViewById(R.id.room_list_item_name));
            v.setTag(R.id.room_list_item_queue, v.findViewById(R.id.room_list_item_queue));
            v.setTag(R.id.room_list_item_track, v.findViewById(R.id.room_list_item_track));
            v.setTag(R.id.room_list_item_art,   v.findViewById(R.id.room_list_item_art));
        }

        TextView  name  = (TextView) v.getTag(R.id.room_list_item_name);
        TextView  queue = (TextView) v.getTag(R.id.room_list_item_queue);
        TextView  track = (TextView) v.getTag(R.id.room_list_item_track);
        ImageView art   = (ImageView)v.getTag(R.id.room_list_item_art);

        Room room = (Room) getItem(position);

        name.setText(room.getName());
        queue.setText(room.getTracks().size() + " items in queue");
        //track.setText(room.getCurrentTrack());

        // Set album art
        // TODO: FIX ALBUM ARTWORK
        //int resourceId = context.getResources().getIdentifier(room.getArt().getIcon(), "drawable", context.getPackageName());
        //art.setImageResource(resourceId);

        return v;
    }

}