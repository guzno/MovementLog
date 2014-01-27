package se.magnulund.dev.movementlog.utils;// Created by Gustav on 26/01/2014.

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {

    private static final String TAG = "DateTimeUtil";

    public static final int NANOS_PER_MILLI = 1000;
    public static final int NANOS_PER_SECOND = 1000 * NANOS_PER_MILLI;
    public static final int NANOS_PER_MINUTE = 60 * NANOS_PER_SECOND;

    public static final int MILLIS_PER_SECOND = 1000;
    public static final int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    public static final int MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    public static final int MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    public static final String DATE_MONTHNAME_DAY = "MMM d";
    public static final String TIME_HOUR_MINUTE = "kk:mm";

    public static String getDateTimeString(long timestamp, String format) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

        Date date = new Date(timestamp);

        return simpleDateFormat.format(date);
    }

    public static String getDurationString(long start, long end, boolean withSeconds) {

        long duration = end - start; // duration in s

        int days = (int)  Math.floor(duration / MILLIS_PER_DAY);

        duration = duration - days * MILLIS_PER_DAY;

        String durationString = (days > 0) ? Integer.valueOf(days) + " d " : "";

        int hours = (int) Math.floor(duration / MILLIS_PER_HOUR);

        duration = duration - hours * MILLIS_PER_HOUR;

        durationString += (hours > 0) ? Integer.valueOf(hours) + " h " : "";

        int minutes = (int)  Math.floor(duration / MILLIS_PER_MINUTE);

        duration = duration - minutes * MILLIS_PER_MINUTE;

        durationString += (minutes > 0) ? Integer.valueOf(minutes) + " min " : "";

        if (withSeconds) {

            int seconds = (int)  Math.floor(duration / MILLIS_PER_SECOND);

            durationString += (seconds > 0) ? Integer.valueOf(minutes) + " s " : "";
        }

        return durationString;
    }
}

