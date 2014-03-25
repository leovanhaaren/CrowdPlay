package com.jvrhenen.crowdplay.app.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by Leo on 25/03/14.
 */
public class Convert {

    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long hours   = TimeUnit.MILLISECONDS.toHours(millis);
        millis      -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis      -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if(hours > 0)
            sb.append(String.format("%02d", hours) +":");
        if(minutes > 0)
            sb.append(String.format("%02d", minutes) +":");
        if(seconds > 0)
            sb.append(String.format("%02d", seconds));

        return(sb.toString());
    }

}
