package se.magnulund.dev.movementlog.location;// Created by Gustav on 26/01/2014.

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

public class TripCoords {
    private static final String TAG = "TripCoords";

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String ACCURACY = "accuracy";
    private static final String TIMESTAMP = "timestamp";
    private static final String TIMESTAMP_NANOS = "timestamp_nanos";
    private static final String PROVIDER = "provider";

    double latitude;
    double longitude;
    float accuracy;
    long timestamp;
    long timestampNanos;
    String provider;

    public TripCoords() {
    }

    public static TripCoords fromLocation(Location location) {

        TripCoords tripCoords = new TripCoords();

        tripCoords.setLatitude(location.getLatitude());

        tripCoords.setLongitude(location.getLongitude());

        tripCoords.setAccuracy(location.getAccuracy());

        tripCoords.setTimestamp(location.getTime());

        tripCoords.setTimestampNanos(location.getElapsedRealtimeNanos());

        tripCoords.setProvider(location.getProvider());

        return tripCoords;
    }

    public static TripCoords fromJSONstring(String json) throws JSONException {

        TripCoords tripCoords = new TripCoords();

        JSONObject jsonObj = new JSONObject(json);

        tripCoords.setLatitude(jsonObj.getDouble(LATITUDE));
        tripCoords.setLongitude(jsonObj.getDouble(LONGITUDE));
        tripCoords.setAccuracy((float) jsonObj.getDouble(ACCURACY));
        tripCoords.setTimestamp(jsonObj.getLong(TIMESTAMP));
        tripCoords.setTimestampNanos(jsonObj.getLong(TIMESTAMP_NANOS));
        tripCoords.setProvider(jsonObj.getString(PROVIDER));

        return tripCoords;

    }

    public float getAccuracy() {
        return accuracy;
    }

    private void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public Location toLocation() {

        Location location = new Location(this.getProvider());

        location.setLatitude(this.getLatitude());
        location.setLongitude(this.getLongitude());
        location.setAccuracy(this.getAccuracy());
        location.setTime(this.getTimestamp());
        location.setElapsedRealtimeNanos(this.getTimestampNanos());

        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    private void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }

    private void setTimestampNanos(long timestampNanos) {
        this.timestampNanos = timestampNanos;
    }

    public String getProvider() {
        return provider;
    }

    private void setProvider(String provider) {
        this.provider = provider;
    }

    public String toJSONString() throws JSONException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(LATITUDE, this.getLatitude());
        jsonObject.put(LONGITUDE, this.getLongitude());
        jsonObject.put(ACCURACY, (double) this.getAccuracy());
        jsonObject.put(TIMESTAMP, this.getTimestamp());
        jsonObject.put(TIMESTAMP_NANOS, this.getTimestampNanos());
        jsonObject.put(PROVIDER, this.getProvider());

        return jsonObject.toString();

    }
}
