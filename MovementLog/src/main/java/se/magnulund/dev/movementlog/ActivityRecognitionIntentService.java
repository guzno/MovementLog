package se.magnulund.dev.movementlog;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import java.util.ArrayList;
import java.util.List;

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
            DetectedMovement mostProbableMovement = new DetectedMovement(result.getMostProbableActivity());

            long timestamp = System.currentTimeMillis();

            List<DetectedActivity> detectedActivities = result.getProbableActivities();

            ArrayList<DetectedMovement> detectedMovements = new ArrayList<DetectedMovement>();

            for (int i = 0; i < detectedActivities.size(); i++){

                DetectedMovement detectedMovement = new DetectedMovement(detectedActivities.get(i));

                detectedMovement.setTimestamp(timestamp);

                detectedMovement.setRank(i);

                detectedMovements.add(detectedMovement);

                Uri dataEntry = MovementDataContract.RawData.addEntry(this, detectedMovement);

                if (dataEntry != null && dataEntry.getPathSegments() != null){
                    Log.d(TAG, "woho! I entered data for: " + detectedMovement.getActivityName() + " with ID: " + dataEntry.getPathSegments().get(1));
                } else {
                    Log.d(TAG, "krep! Data entry failed!");
                }
            }

            Trip onGoingTrip = MovementDataContract.Trips.getLatestUnfinishedTrip(this);

            int activityType = mostProbableMovement.getType();

            if (onGoingTrip != null) {
                if (activityType == DetectedActivity.ON_FOOT) {
                    onGoingTrip.setEndTime(timestamp);
                    MovementDataContract.Trips.updateTrip(this, onGoingTrip);
                }
            } else {

                switch (activityType){
                    case DetectedActivity.IN_VEHICLE:
                    case DetectedActivity.ON_BICYCLE:
                        Trip startedTrip = new Trip(timestamp, activityType);
                        Uri tripEntry = MovementDataContract.Trips.addTrip(this, startedTrip);
                        if (tripEntry != null && tripEntry.getPathSegments() != null){
                            Log.d(TAG, "woho! I started a " + mostProbableMovement.getActivityName() + " trip with ID: " + tripEntry.getPathSegments().get(1));
                        } else {
                            Log.d(TAG, "krep! Data entry failed!");
                        }
                        break;
                    default:
                }
            }
        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */
        }
    }
}
