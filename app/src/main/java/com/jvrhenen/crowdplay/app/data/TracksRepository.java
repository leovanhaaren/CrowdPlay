package com.jvrhenen.crowdplay.app.data;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.jvrhenen.crowdplay.app.model.Track;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Leo on 17/03/14.
 */
public class TracksRepository {

    private DatabaseHelper db;
    private Dao<Track, Integer> tracksDao;

    public TracksRepository(Context ctx) {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            db = dbManager.getHelper(ctx);
            tracksDao = db.getTracksDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public int create(Track track) {
        try {
            return tracksDao.create(track);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int update(Track track) {
        try {
            return tracksDao.update(track);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int delete(Track track)
    {
        try {
            return tracksDao.delete(track);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Dao.CreateOrUpdateStatus save(Track track)
    {
        try {
            return tracksDao.createOrUpdate(track);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Track> getAll()
    {
        try {
            return tracksDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}