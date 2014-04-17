package com.crowdplay.app.comparators;

import com.crowdplay.app.model.Track;

import java.util.Comparator;

/**
 * Created by Leo on 17/04/14.
 */
public class VoteComparator implements Comparator<Track>
{
    public int compare(Track t1, Track t2) {
        return t1.getVotes().size() - t2.getVotes().size();
    }
}