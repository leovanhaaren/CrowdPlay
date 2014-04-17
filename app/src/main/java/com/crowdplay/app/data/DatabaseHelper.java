package com.crowdplay.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crowdplay.app.model.Room;
import com.crowdplay.app.model.Track;
import com.crowdplay.app.model.Vote;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by Leo on 17/03/14.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME    = "data.db";
    private static final int    DATABASE_VERSION = 1;

    private Dao<Vote,  Integer> votesDao  = null;
    private Dao<Track, Integer> tracksDao = null;
    private Dao<Room,  Integer> roomsDao  = null;

    private RuntimeExceptionDao<Vote,  Integer> votesRuntimeDao  = null;
    private RuntimeExceptionDao<Track, Integer> tracksRuntimeDao = null;
    private RuntimeExceptionDao<Room,  Integer> roomsRuntimeDao  = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, Vote.class);
            TableUtils.createTable(connectionSource, Track.class);
            TableUtils.createTable(connectionSource, Room.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }

        RuntimeExceptionDao<Vote,  Integer> votesDao  = getVotesDataDao();
        RuntimeExceptionDao<Track, Integer> tracksDao = getTracksDataDao();
        RuntimeExceptionDao<Room,  Integer> roomsDao  = getRoomsDataDao();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, Vote.class,  true);
            TableUtils.dropTable(connectionSource, Track.class, true);
            TableUtils.dropTable(connectionSource, Room.class,  true);

            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Vote, Integer> getVotesDao() throws SQLException {
        if (votesDao == null) {
            votesDao = getDao(Vote.class);
        }
        return votesDao;
    }

    public RuntimeExceptionDao<Vote, Integer> getVotesDataDao() {
        if (votesRuntimeDao == null) {
            votesRuntimeDao = getRuntimeExceptionDao(Vote.class);
        }
        return votesRuntimeDao;
    }

    public Dao<Track, Integer> getTracksDao() throws SQLException {
        if (tracksDao == null) {
            tracksDao = getDao(Track.class);
        }
        return tracksDao;
    }

    public RuntimeExceptionDao<Track, Integer> getTracksDataDao() {
        if (tracksRuntimeDao == null) {
            tracksRuntimeDao = getRuntimeExceptionDao(Track.class);
        }
        return tracksRuntimeDao;
    }

    public Dao<Room, Integer> getRoomsDao() throws SQLException {
        if (roomsDao == null) {
            roomsDao = getDao(Room.class);
        }
        return roomsDao;
    }

    public RuntimeExceptionDao<Room, Integer> getRoomsDataDao() {
        if (roomsRuntimeDao == null) {
            roomsRuntimeDao = getRuntimeExceptionDao(Room.class);
        }
        return roomsRuntimeDao;
    }

    @Override
    public void close() {
        super.close();

        votesDao  = null;
        tracksDao = null;
        roomsDao  = null;
    }

}