package com.crowdplay.app.model;

import android.graphics.Bitmap;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Leo on 17/03/14.
 */
@DatabaseTable(tableName = "tracks")
public class Track {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private long trackId;

    @DatabaseField
    private String title;

    @DatabaseField
    private String artist;

    @DatabaseField
    private String album;

    @DatabaseField
    private long albumId;

    @DatabaseField
    private long duration;

    private Bitmap bitmap;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Room room;

    @ForeignCollectionField
    private ForeignCollection<Vote> votes;

    public Track() {
        // ORMLite needs a no-arg constructor
    }

    public Track(String title, String artist){
        this.title  = title;
        this.artist = artist;
    }

    public Track(long id, String title, String artist, String album, long albumId, long duration){
        this.trackId  = id;
        this.title    = title;
        this.artist   = artist;
        this.album    = album;
        this.albumId  = albumId;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public ForeignCollection<Vote> getVotes() {
        return votes;
    }

}