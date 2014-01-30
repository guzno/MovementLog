package se.magnulund.dev.movementlog.location;// Created by Gustav on 30/01/2014.

import android.location.Location;
import android.os.SystemClock;

import se.magnulund.dev.movementlog.utils.DateTimeUtil;

public class LocationUtils {

    private static final String TAG = "LocationUtils";

    public static long getLocationAgeInSeconds(Location location){
        return getLocationAge(location, DateTimeUtil.NANOS_PER_SECOND);
    }

    public static long getLocationAgeInMillis(Location location){
        return getLocationAge(location, DateTimeUtil.NANOS_PER_MILLI);
    }

    private static long getLocationAge(Location location, long timeFormat){
        return Math.abs(SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / timeFormat;
    }
}
