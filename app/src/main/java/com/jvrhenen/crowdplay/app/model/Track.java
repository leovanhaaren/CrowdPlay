package com.jvrhenen.crowdplay.app.model;

import com.j256.ormlite.field.DatabaseField;
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
    private String artist;

    @DatabaseField
    private String title;

    @DatabaseField
    private String album;

    @DatabaseField
    private long duration;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Room room;

    public Track() {
        // ORMLite needs a no-arg constructor
    }

    public Track(String title, String artist){
        this.title  = title;
        this.artist = artist;
    }

    public int getId() {
        return id;
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

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}