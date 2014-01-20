package se.magnulund.dev.movementlog;// Created by Gustav on 14/01/2014.

import android.database.Cursor;

import com.google.android.gms.location.DetectedActivity;

import se.magnulund.dev.movementlog.provider.MovementDataContract;

public class DetectedMovement extends DetectedActivity {
    private static final String TAG = "DetectedMovement";
    long timestamp;
    int confidence_rank;

    public DetectedMovement(int activityType, int confidence, long timestamp, int confidence_rank) {
        super(activityType, confidence);
        this.timestamp = timestamp;
        this.confidence_rank = confidence_rank;
    }

    public DetectedMovement(DetectedActivity detectedActivity){
        super(detectedActivity.getType(), detectedActivity.getConfidence());
    }

    public DetectedMovement(DetectedActivity detectedActivity, long timestamp, int confidence_rank){
        super(detectedActivity.getType(), detectedActivity.getConfidence());
        this.timestamp = timestamp;
        this.confidence_rank = confidence_rank;
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

    public static DetectedMovement fromCursor(Cursor cursor) {

        final int activityType = cursor.getInt(cursor.getColumnIndex(MovementDataContract.RawData.ACTIVITY_TYPE));

        final int confidence = cursor.getInt(cursor.getColumnIndex(MovementDataContract.RawData.CONFIDENCE));

        final long timestamp = cursor.getLong(cursor.getColumnIndex(MovementDataContract.RawData.TIMESTAMP));

        final int confidenceRank = cursor.getInt(cursor.getColumnIndex(MovementDataContract.RawData.CONFIDENCE_RANK));

        return new DetectedMovement(activityType, confidence, timestamp, confidenceRank);
    }

    public String getActivityName(){
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
