package com.crowdplay.app.data;

import android.content.Context;

import com.crowdplay.app.model.Vote;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Leo on 17/03/14.
 */
public class VotesRepository {

    private DatabaseHelper db;
    private Dao<Vote, Integer> votesDao;

    public VotesRepository(Context ctx) {
        try {
            DatabaseManager dbManager = new DatabaseManager();

            db       = dbManager.getHelper(ctx);
            votesDao = db.getVotesDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public int create(Vote vote) {
        try {
            return votesDao.create(vote);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int update(Vote vote) {
        try {
            return votesDao.update(vote);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int delete(Vote vote)
    {
        try {
            return votesDao.delete(vote);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Dao.CreateOrUpdateStatus save(Vote vote)
    {
        try {
            return votesDao.createOrUpdate(vote);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Vote> getAll()
    {
        try {
            return votesDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}