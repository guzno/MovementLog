package se.magnulund.dev.movementlog.rawdata;// Created by Gustav on 14/01/2014.

import android.database.Cursor;

import com.google.android.gms.location.DetectedActivity;

import se.magnulund.dev.movementlog.contracts.RawDataContract;

public class RawData extends DetectedActivity {
    private static final String TAG = "Columns";
    long timestamp;
    int confidence_rank;
    int id;

    public RawData(int id, int activityType, int confidence, long timestamp, int confidence_rank) {
        super(activityType, confidence);
        this.id = id;
        this.timestamp = timestamp;
        this.confidence_rank = confidence_rank;
    }

    public RawData(DetectedActivity detectedActivity) {
        super(detectedActivity.getType(), detectedActivity.getConfidence());
    }

    public RawData(DetectedActivity detectedActivity, int id, long timestamp, int confidence_rank) {
        super(detectedActivity.getType(), detectedActivity.getConfidence());
        this.id = id;
        this.timestamp = timestamp;
        this.confidence_rank = confidence_rank;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getRank() {
        return confidence_rank;
    }

    public void setRank(int confidence_rank) {
        this.confidence_rank = confidence_rank;
    }

    public static RawData fromCursor(Cursor cursor) {

        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(RawDataContract.Columns._ID));

        final int activityType = cursor.getInt(cursor.getColumnIndexOrThrow(RawDataContract.Columns.ACTIVITY_TYPE));

        final int confidence = cursor.getInt(cursor.getColumnIndexOrThrow(RawDataContract.Columns.CONFIDENCE));

        final long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(RawDataContract.Columns.TIMESTAMP));

        final int confidenceRank = cursor.getInt(cursor.getColumnIndexOrThrow(RawDataContract.Columns.CONFIDENCE_RANK));

        return new RawData(id, activityType, confidence, timestamp, confidenceRank);
    }

    /**
     * Get the name of the detected activity type
     *
     * @return A user-readable name for the type
     */
    public String getActivityName() {
        return getNameFromType(this.getType());
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
}
