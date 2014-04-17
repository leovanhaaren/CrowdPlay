package com.crowdplay.app.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Leo on 17/03/14.
 */
@DatabaseTable(tableName = "votes")
public class Vote {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Track track;

    @DatabaseField
    private String owner;

    @DatabaseField
    private Date date;

    public Vote() {
        // ORMLite needs a no-arg constructor
    }

    public Vote(Track track, String androidId) {
        this.track = track;
        this.owner = androidId;
        this.date  = new Date();
    }

    public int getId() {
        return id;
    }

    public Track getTrack() {
        return track;
    }

    public String getOwner() {
        return owner;
    }

    public Date getDate() {
        return date;
    }

}