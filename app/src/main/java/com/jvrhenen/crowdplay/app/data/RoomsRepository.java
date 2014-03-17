package com.jvrhenen.crowdplay.app.data;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.jvrhenen.crowdplay.app.model.Room;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Leo on 17/03/14.
 */
public class RoomsRepository {

    private DatabaseHelper db;
    private Dao<Room, Integer> roomsDao;

    public RoomsRepository(Context ctx) {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            db = dbManager.getHelper(ctx);
            roomsDao = db.getRoomsDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public int create(Room room) {
        try {
            return roomsDao.create(room);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int update(Room room) {
        try {
            return roomsDao.update(room);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int delete(Room room)
    {
        try {
            return roomsDao.delete(room);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Room> getAll()
    {
        try {
            return roomsDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}