package se.magnulund.dev.movementlog;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

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
            // Set timestamp for this update
            long timestamp = System.currentTimeMillis();

            List<DetectedActivity> detectedActivities = result.getProbableActivities();

            // -- TODO analyze all detected activities??

            for (int i = 0; i < detectedActivities.size(); i++) {

                DetectedMovement detectedMovement = new DetectedMovement(detectedActivities.get(i));

                detectedMovement.setTimestamp(timestamp);

                detectedMovement.setRank(i);

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
                    // TODO Get trip end location
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
                        // TODO Get trip start location
                        break;

                    case DetectedActivity.STILL:
                    case DetectedActivity.TILTING:
                    case DetectedActivity.UNKNOWN:
                        ContentResolver resolver = getContentResolver();

                        String selection = MovementDataContract.RawData.TIMESTAMP + " = ? AND (" + MovementDataContract.RawData.ACTIVITY_TYPE + " = ? OR " + MovementDataContract.RawData.ACTIVITY_TYPE + " = ?)";

                        String[] selectionArgs = {Long.toString(timestamp), Integer.toString(DetectedActivity.IN_VEHICLE), Integer.toString(DetectedActivity.ON_BICYCLE)};

                        Cursor cursor = resolver.query(MovementDataContract.RawData.CONTENT_URI, MovementDataContract.RawData.DEFAULT_PROJECTION, selection, selectionArgs, MovementDataContract.RawData.DEFAULT_SORT_ORDER);

                        if (cursor != null && cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            DetectedMovement possibleTripStart = DetectedMovement.fromCursor(cursor);

                            if (possibleTripStart.getConfidence() > 30) {
                                Toast.makeText(this, "You seem to have started an " + possibleTripStart.getActivityName() + " trip.", Toast.LENGTH_SHORT).show();
                                // TODO Write possible tripstart to db (marked "unconfirmed")
                                // TODO Get tentative trip start location
                            }
                        }
                        break;
                    case DetectedActivity.ON_FOOT:
                    default:
                }
            }
        } else {
            Log.d(TAG, "Unhandled intent");
        }
    }
}
