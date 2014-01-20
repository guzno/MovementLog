package se.magnulund.dev.movementlog;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

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
            // Set timestamp for this update
            long timestamp = System.currentTimeMillis();

            List<DetectedActivity> detectedActivities = result.getProbableActivities();

            // -- TODO analyze all detected activities??
            //ArrayList<DetectedMovement> detectedMovements = new ArrayList<DetectedMovement>();

            for (int i = 0; i < detectedActivities.size(); i++) {

                DetectedMovement detectedMovement = new DetectedMovement(detectedActivities.get(i));

                detectedMovement.setTimestamp(timestamp);

                detectedMovement.setRank(i);

                /*
                detectedMovements.add(detectedMovement);
                */

                // insert data into db
                Uri dataEntry = MovementDataContract.RawData.addEntry(this, detectedMovement);

                if (dataEntry != null && dataEntry.getPathSegments() != null) {
                    //Log.d(TAG, "woho! I entered data for: " + detectedMovement.getActivityName() + " with ID: " + dataEntry.getPathSegments().get(1));
                } else {
                    Log.d(TAG, "krep! Data entry failed!");
                }
            }

            // get the current trip if any
            Trip onGoingTrip = MovementDataContract.Trips.getLatestUnfinishedTrip(this);

            // get the activity type
            int activityType = mostProbableMovement.getType();

            if (onGoingTrip != null) { // if there is and ongoing trip check if it just ended
                if (activityType == DetectedActivity.ON_FOOT) {
                    onGoingTrip.setEndTime(timestamp);
                    MovementDataContract.Trips.updateTrip(this, onGoingTrip);
                }
            } else { // check if we just started a trip
                switch (activityType) {
                    case DetectedActivity.IN_VEHICLE:
                    case DetectedActivity.ON_BICYCLE:
                        // create a new Trip
                        Trip startedTrip = new Trip(timestamp, activityType);
                        // insert trip into db
                        Uri tripEntry = MovementDataContract.Trips.addTrip(this, startedTrip);

                        if (tripEntry != null && tripEntry.getPathSegments() != null) {
                            Log.d(TAG, "woho! I started a " + mostProbableMovement.getActivityName() + " trip with ID: " + tripEntry.getPathSegments().get(1));
                        } else {
                            Log.d(TAG, "krep! Trip entry failed!");
                        }
                        break;
                    case DetectedActivity.ON_FOOT:
                    case DetectedActivity.STILL:
                    case DetectedActivity.TILTING:
                    case DetectedActivity.UNKNOWN:
                    default:
                }
            }
        } else {
            Log.d(TAG, "Unhandled intent");
        }
    }
}
