package com.jvrhenen.crowdplay.app.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import io.segment.android.models.Track;

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
    private Date date;

    @ForeignCollectionField
    private ForeignCollection<Track> tracks;

    public Room() {
        // ORMLite needs a no-arg constructor
    }

    public Room(String name){
        this.name = name;
        this.date = new Date();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

}