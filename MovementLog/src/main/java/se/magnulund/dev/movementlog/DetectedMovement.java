package se.magnulund.dev.movementlog;// Created by Gustav on 14/01/2014.

import com.google.android.gms.location.DetectedActivity;

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

}
