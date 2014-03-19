package com.jvrhenen.crowdplay.app.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Leo on 17/03/14.
 */
@DatabaseTable(tableName = "rooms")
public class Room {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;

    @DatabaseField
    private String owner;

    @DatabaseField
    private Date date;

    @DatabaseField(foreign = true, canBeNull = true)
    private Track currentTrack;

    @ForeignCollectionField
    private ForeignCollection<Track> tracks;

    public Room() {
        // ORMLite needs a no-arg constructor
    }

    public Room(String name, String androidId) {
        this.name  = name.toLowerCase();
        this.owner = androidId;
        this.date  = new Date();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ForeignCollection<Track> getTracks() {
        return tracks;
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
    }

}