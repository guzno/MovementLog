package se.magnulund.dev.movementlog.triprecognition;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

import se.magnulund.dev.movementlog.contracts.RawDataContract;
import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.rawdata.RawData;
import se.magnulund.dev.movementlog.trips.Trip;

public class TripRecognitionIntentService extends IntentService {

    private static final String TAG = "TripRecognitionIntentService";

    public static final int RESULT_TYPE_ACTIVITY_RECOGNITION = 0;
    public static final int RESULT_TYPE_BLUETOOTH_CONNECTION_CHANGE = 1;
    public static final int RESULT_TYPE_LOCATION_RESULT = 2;
    public static final int RESULT_TYPE_CHARGING_STATE_CHANGED = 3;

    public static final int START_LOCATION = 0;
    public static final int END_LOCATION = 1;

    private static final int POSSIBLE_CAR_TRIP_CONFIDENCE_THRESHOLD = 40;
    private static final int POSSIBLE_BIKE_TRIP_CONFIDENCE_THRESHOLD = 40;

    long timestamp;
    int currentResultType;

    public TripRecognitionIntentService() {
        super(TAG);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        timestamp = System.currentTimeMillis();

        if (ActivityRecognitionResult.hasResult(intent)) {

            currentResultType = RESULT_TYPE_ACTIVITY_RECOGNITION;

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            processActivityRecognitionResult(result);

        } else {
            Log.d(TAG, "Unhandled intent");
        }
    }

    // store data, analyze for trip start/end, initiate location requests and change update frequencies
    // TODO change update frequencies
    private void processActivityRecognitionResult(ActivityRecognitionResult result) {

        ArrayList<RawData> rawDataArrayList = storeAndReturnResultData(result.getProbableActivities());

        RawData mostProbableActivity = rawDataArrayList.get(0);

        assert mostProbableActivity != null;

        Trip onGoingTrip = TripLogContract.getLatestUnfinishedTrip(getContentResolver());

        int activityType = mostProbableActivity.getType();

        if (onGoingTrip != null) {

            //check if walking was detected and end the trip
            if (activityType == DetectedActivity.ON_FOOT) {

                endTrip(onGoingTrip, mostProbableActivity, true);

            }
        } else {

            switch (activityType) {

                case DetectedActivity.IN_VEHICLE:
                case DetectedActivity.ON_BICYCLE:

                    Trip startedTrip = new Trip(timestamp, activityType);

                    startTrip(startedTrip, mostProbableActivity, true);
                    break;

                case DetectedActivity.STILL:
                case DetectedActivity.TILTING:
                case DetectedActivity.UNKNOWN:

                    checkForPossibleTripStart(rawDataArrayList);
                    break;

                case DetectedActivity.ON_FOOT:
                default:
            }
        }
    }

    private ArrayList<RawData> storeAndReturnResultData(List<DetectedActivity> detectedActivities) {

        ArrayList<RawData> data = new ArrayList<>();

        for (int i = 0; i < detectedActivities.size(); i++) {

            RawData rawData = new RawData(detectedActivities.get(i));

            rawData.setTimestamp(timestamp);

            rawData.setRank(i);

            Uri dataEntry = RawDataContract.addEntry(this, rawData);

            assert dataEntry != null;
            assert dataEntry.getPathSegments() != null;

            rawData.setId(Integer.valueOf(dataEntry.getPathSegments().get(1)));

            data.add(rawData);
        }

        return data;
    }


    private void startTrip(Trip trip, RawData startedBy, boolean startConfirmed) {

        if (startConfirmed) {
            trip.setStartConfirmedByID(startedBy.getId());
        }

        trip.setStartedByID(startedBy.getId());

        trip.setStartedByType(currentResultType);

        TripLogContract.addTrip(getContentResolver(), trip);

        // TODO Get trip start location
    }

    private void endTrip(Trip trip, RawData endedBy, boolean endConfirmed) {

        trip.setEndTime(timestamp);

        if (endConfirmed) {
            trip.setEndConfirmedByID(endedBy.getId());
        }

        trip.setEndedByID(endedBy.getId());

        trip.setEndedByType(currentResultType);

        TripLogContract.updateTrip(getContentResolver(), trip);

        // TODO Get trip end location
    }

    private void checkForPossibleTripStart(ArrayList<RawData> rawDataArrayList) {

        for (int i = 1; i < rawDataArrayList.size(); i++) {

            RawData data = rawDataArrayList.get(i);

            if (isPossibleTripStart(data)) {
                Trip trip = new Trip(data.getTimestamp(), data.getType());
                startTrip(trip, data, false);
            }
        }
    }

    private boolean isPossibleTripStart(RawData data) {

        switch (data.getType()) {
            case DetectedActivity.IN_VEHICLE:
                return data.getConfidence() > POSSIBLE_CAR_TRIP_CONFIDENCE_THRESHOLD;
            case DetectedActivity.ON_BICYCLE:
                return data.getConfidence() > POSSIBLE_BIKE_TRIP_CONFIDENCE_THRESHOLD;
            default:
                return false;
        }
    }
}
