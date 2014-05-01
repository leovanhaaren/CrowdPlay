package com.crowdplay.app.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crowdplay.app.R;
import com.crowdplay.app.model.Track;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

/**
 * Created by Leo on 17/03/14.
 */
public class VoteListAdapter extends BaseAdapter {

    private Context          context;
    private ArrayList<Track> items;
    private LayoutInflater   inflater;
    private SparseBooleanArray mSelectedItemsIds;

    public VoteListAdapter(Context c, ArrayList<Track> e) {
        inflater = LayoutInflater.from(c);
        context  = c;
        items = e;
        mSelectedItemsIds = new SparseBooleanArray();
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
            v = inflater.inflate(R.layout.vote_list_item, parent, false);
            v.setTag(R.id.title,  v.findViewById(R.id.title));
            v.setTag(R.id.artist, v.findViewById(R.id.artist));
            v.setTag(R.id.art,    v.findViewById(R.id.art));
            v.setTag(R.id.vote,   v.findViewById(R.id.vote));
        }

        TextView  title  = (TextView)  v.getTag(R.id.title);
        TextView  artist = (TextView)  v.getTag(R.id.artist);
        ImageView art    = (ImageView) v.getTag(R.id.art);
        ImageView button = (ImageView) v.getTag(R.id.vote);

        Track track = getItem(position);

        title.setText(track.getTitle());
        artist.setText(track.getArtist());

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri imageUri = ContentUris.withAppendedId(sArtworkUri, track.getAlbumId());

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisc(true)
                .cacheInMemory(false)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        imageLoader.displayImage(imageUri.toString(), art, options);

        if(mSelectedItemsIds.get(position)) {
            button.setImageResource(R.drawable.ic_action_good_blue);
        } else {
            button.setImageResource(R.drawable.ic_action_good);
        }

        return v;
    }

    public void toggleVote(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public boolean isChecked(int position) {
        return mSelectedItemsIds.get(position);
    }

}