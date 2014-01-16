package se.magnulund.dev.movementlog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Calendar;

import se.magnulund.dev.movementlog.provider.MovementDataContract;

public class ActivityRecognitionIntentService extends IntentService {

    private static final String TAG = "ActivityRecognitionIntentService";

    public ActivityRecognitionIntentService() {
        super(TAG);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {

            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            int timestamp = (int) Calendar.getInstance().getTimeInMillis();

            DetectedMovement detectedMovement = new DetectedMovement(mostProbableActivity, timestamp, 0);

            Uri dataEntry = MovementDataContract.RawData.addEntry(this, detectedMovement);

            if (dataEntry != null && dataEntry.getPathSegments() != null){
                Log.d(TAG, "woho! I data entered with ID: " + dataEntry.getPathSegments().get(1));
            } else {
                Log.d(TAG, "krep! Data entry failed!");
            }

            /*
             * Get the probability that this activity is the
             * the user's actual activity
             */
            //int confidence = mostProbableActivity.getConfidence();
            /*
             * Get an integer describing the type of activity
             */
            //int activityType = mostProbableActivity.getType();
            //String activityName = getNameFromType(activityType);
            /*
             * At this point, you have retrieved all the information
             * for the current update. You can display this
             * information to the user in a notification, or
             * send it to an Activity or Service in a broadcast
             * Intent.
             */

            //Log.d(TAG, "woho! I has datas! " + confidence + " and name " + activityName);
        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */
        }
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
