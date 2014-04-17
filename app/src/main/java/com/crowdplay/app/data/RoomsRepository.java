package com.crowdplay.app.data;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.crowdplay.app.model.Room;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Leo on 17/03/14.
 */
public class RoomsRepository {

    private DatabaseHelper db;
    private Dao<Room, Integer> roomsDao;

    public RoomsRepository(Context ctx) {
        try {
            DatabaseManager dbManager = new DatabaseManager();

            db       = dbManager.getHelper(ctx);
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

    public Dao.CreateOrUpdateStatus save(Room room)
    {
        try {
            return roomsDao.createOrUpdate(room);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Room> getAll()
    {
        try {
            return (ArrayList<Room>)roomsDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Room getRoom(int id)
    {
        try {
            return (Room)roomsDao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}