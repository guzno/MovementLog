package se.magnulund.dev.movementlog.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.contracts.RawDataContract;
import se.magnulund.dev.movementlog.rawdata.RawData;
import se.magnulund.dev.movementlog.trips.Trip;

public class ActivityRecognitionIntentService extends IntentService {

    private static final String TAG = "ActivityRecognitionIntentService";

    private static final int POSSIBLE_TRIP_CONFIDENCE_THRESHOLD = 30;

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

        if (ActivityRecognitionResult.hasResult(intent)) {        // If the incoming intent contains an update

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);            // Get the update

            long timestamp = System.currentTimeMillis();            // Set timestamp for this update

            List<DetectedActivity> detectedActivities = result.getProbableActivities();            // Get detected activities

            RawData mostProbableActivity = null;            // the most probable activity

            ArrayList<RawData> rawDatas = new ArrayList<RawData>();

            // -- TODO analyze all detected activities??

            for (int i = 0; i < detectedActivities.size(); i++) {

                RawData rawData = new RawData(detectedActivities.get(i));

                if (i == 0) {
                    mostProbableActivity = rawData;
                }

                rawData.setTimestamp(timestamp);

                rawData.setRank(i);

                rawDatas.add(rawData);

                Uri dataEntry = RawDataContract.addEntry(this, rawData);                // insert data into db

                if (dataEntry == null || dataEntry.getPathSegments() == null) {
                    Log.d(TAG, "krep! Data entry failed!");
                }
            }

            if (mostProbableActivity != null) {

                Trip onGoingTrip = TripLogContract.getLatestUnfinishedTrip(this);            // get the current trip if any

                int activityType = mostProbableActivity.getType();            // get the activity type

                if (onGoingTrip != null) { // if there is an ongoing trip check if walking was detected and end the trip
                    if (activityType == DetectedActivity.ON_FOOT) {
                        onGoingTrip.setEndTime(timestamp);
                        TripLogContract.updateTrip(this, onGoingTrip);
                        // TODO Get trip end location
                    }
                } else { // check if we just started a trip
                    switch (activityType) {
                        case DetectedActivity.IN_VEHICLE:
                        case DetectedActivity.ON_BICYCLE:
                            // create a new Trip
                            Trip startedTrip = new Trip(timestamp, activityType);

                            startedTrip.setConfirmed(true);
                            // insert trip into db
                            Uri tripEntry = TripLogContract.addTrip(this, startedTrip);

                            if (tripEntry != null && tripEntry.getPathSegments() != null) {
                                Log.d(TAG, "woho! I started a " + mostProbableActivity.getActivityName() + " trip with ID: " + tripEntry.getPathSegments().get(1));
                            } else {
                                Log.d(TAG, "krep! Trip entry failed!");
                            }
                            // TODO Get trip start location
                            break;

                        case DetectedActivity.STILL:
                        case DetectedActivity.TILTING:
                        case DetectedActivity.UNKNOWN:
                            for (int i = 1; i < rawDatas.size(); i++) {
                                RawData data = rawDatas.get(i);
                                if (isPossibleTripStart(data)) {
                                    Toast.makeText(this, "You seem to have started an " + data.getActivityName() + " trip.", Toast.LENGTH_SHORT).show();
                                    Trip possibleTrip = new Trip(data.getTimestamp(), data.getType());
                                    TripLogContract.addTrip(this, possibleTrip);
                                    // TODO Get possible trip start location
                                }
                            }


                            break;
                        case DetectedActivity.ON_FOOT:
                        default:
                    }
                }
            }
        } else {
            Log.d(TAG, "Unhandled intent");
        }
    }

    private boolean isPossibleTripStart(RawData potentialTripStart){

        return (potentialTripStart.getType() == DetectedActivity.IN_VEHICLE || potentialTripStart.getType() == DetectedActivity.ON_BICYCLE)
                && potentialTripStart.getConfidence() > POSSIBLE_TRIP_CONFIDENCE_THRESHOLD;
    }
}
