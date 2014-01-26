package se.magnulund.dev.movementlog.utils;// Created by Gustav on 26/01/2014.

import android.content.Intent;
import android.location.Location;
import android.net.Uri;

public class mIntentBuilder {
    private static final String TAG = "mIntentBuilder";

    public static Intent getMapsIntent(String locationLabel, Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        String uriBegin = "geo:" + latitude + "," + longitude;
        String query = latitude + "," + longitude + "(" + locationLabel + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);

        return new Intent(android.content.Intent.ACTION_VIEW, uri);
    }
}
