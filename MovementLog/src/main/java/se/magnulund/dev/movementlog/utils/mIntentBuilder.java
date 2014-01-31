package se.magnulund.dev.movementlog.utils;// Created by Gustav on 26/01/2014.

import android.content.Intent;
import android.net.Uri;

import se.magnulund.dev.movementlog.location.TripCoords;

public class mIntentBuilder {
    private static final String TAG = "mIntentBuilder";

    public static Intent getMapsIntent(String locationLabel, TripCoords tripCoords) {
        double latitude = tripCoords.getLatitude();
        double longitude = tripCoords.getLongitude();

        String uriBegin = "geo:" + latitude + "," + longitude;
        String query = latitude + "," + longitude + "(" + locationLabel + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);

        return new Intent(android.content.Intent.ACTION_VIEW, uri);
    }
}
