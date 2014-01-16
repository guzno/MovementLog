package se.magnulund.dev.movementlog;// Created by Gustav on 14/01/2014.

import android.database.Cursor;

import com.google.android.gms.location.DetectedActivity;

import se.magnulund.dev.movementlog.provider.MovementDataContract;

public class DetectedMovement extends DetectedActivity {
    private static final String TAG = "DetectedMovement";
    public int timestamp;
    public int confidence_rank;

    public DetectedMovement(int activityType, int confidence, int timestamp, int confidence_rank) {
        super(activityType, confidence);
        this.timestamp = timestamp;
        this.confidence_rank = confidence_rank;
    }

    public DetectedMovement(DetectedActivity detectedActivity){
        super(detectedActivity.getType(), detectedActivity.getConfidence());
    }

    public DetectedMovement(DetectedActivity detectedActivity, int timestamp, int confidence_rank){
        super(detectedActivity.getType(), detectedActivity.getConfidence());
        this.timestamp = timestamp;
        this.confidence_rank = confidence_rank;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
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

        final int timestamp = cursor.getInt(cursor.getColumnIndex(MovementDataContract.RawData.TIMESTAMP));

        final int confidenceRank = cursor.getInt(cursor.getColumnIndex(MovementDataContract.RawData.CONFIDENCE_RANK));

        return new DetectedMovement(activityType, confidence, timestamp, confidenceRank);
    }

}
