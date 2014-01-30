package se.magnulund.dev.movementlog.services;

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
import se.magnulund.dev.movementlog.utils.Constants;
import se.magnulund.dev.movementlog.utils.DateTimeUtil;
import se.magnulund.dev.movementlog.utils.NotificationSender;

public class TripRecognitionIntentService extends IntentService {

    private static final String TAG = "TripRecognitionIntentService";
    private static final String TESTING = "testing";
    private Boolean testing;

    private static final int POSSIBLE_CAR_TRIP_CONFIDENCE_THRESHOLD = 40;
    private static final int POSSIBLE_BIKE_TRIP_CONFIDENCE_THRESHOLD = 40;

    long timestamp;

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

        testing = intent.getBooleanExtra(TESTING, false);

        if (ActivityRecognitionResult.hasResult(intent)) {
            /*
            Bundle extras = intent.getExtras();
            if (extras != null) {

                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    Log.e(TAG, key);
                }
            }
            */
            processActivityRecognitionResult(intent);

        } else {

            if(testing){Log.d(TAG, "Unhandled intent");}

        }
    }

    // store data, analyze for trip start/end, initiate location requests and change update frequencies
    // TODO change update frequencies
    private void processActivityRecognitionResult(Intent intent) {

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        timestamp = result.getTime();

        ArrayList<RawData> rawDataArrayList = storeAndReturnResultData(result.getProbableActivities());

        RawData topActivity = rawDataArrayList.get(0);

        assert topActivity != null;

        Trip trip = TripLogContract.getLatestUnfinishedTrip(getContentResolver());

        boolean ongoingTrip = (trip != null);

        if (ongoingTrip && !trip.isStartConfirmed() && timestamp - trip.getStartTime() > 5 * DateTimeUtil.MILLIS_PER_MINUTE) {
            confirmTripAsIncorrect(trip);
            ongoingTrip = false;
        }

        switch (topActivity.getType()) {

            case DetectedActivity.IN_VEHICLE:
            case DetectedActivity.ON_BICYCLE:

                if (ongoingTrip) {

                    checkForTripConfirmation(topActivity, trip);

                } else {

                    startTrip(topActivity, Trip.CONFIRMED);

                }

                break;

            case DetectedActivity.ON_FOOT:

                if (ongoingTrip) {

                    confirmTripEnd(topActivity, trip);

                }

                break;

            case DetectedActivity.STILL:
            case DetectedActivity.TILTING:
            case DetectedActivity.UNKNOWN:

                if (!ongoingTrip) {

                    checkForPossibleTripStart(rawDataArrayList);

                }

                break;

            default:
        }
    }

    private ArrayList<RawData> storeAndReturnResultData(List<DetectedActivity> detectedActivities) {

        if(testing){ Log.e(TAG, "DA_COUNT: "+detectedActivities.size()); }

        ArrayList<RawData> data = new ArrayList<>();

        int i = 0;

        for (DetectedActivity detectedActivity : detectedActivities) {

            RawData rawData = new RawData(detectedActivity);

            rawData.setTimestamp(timestamp);

            rawData.setRank(i);

            if(testing){ Log.e(TAG, "RD: "+rawData.getActivityName()+" rnk: "+rawData.getRank()+" conf: "+rawData.getConfidence()); }

            Uri dataEntry = RawDataContract.addEntry(this, rawData);

            assert dataEntry != null;

            assert dataEntry.getPathSegments() != null;

            rawData.setId(Integer.valueOf(dataEntry.getPathSegments().get(1)));

            data.add(rawData);

            i++;
        }

        return data;
    }

    private void confirmTripAsIncorrect(Trip trip) {

        trip.setConfirmedAs(Trip.TRIP_CONFIRMED_AS_INCORRECT);

        TripLogContract.updateTrip(getContentResolver(), trip);

    }

    private void confirmTripEnd(RawData data, Trip trip) {
        if (trip.isStartConfirmed()) {

            // end confirmed trip
            endTrip(trip, data, Trip.CONFIRMED);

        } else {

            // mark trip as a incorrectly detected (trip ended before it was confirmed)
            confirmTripAsIncorrect(trip);

        }
    }

    private void checkForTripConfirmation(RawData data, Trip trip) {

        if (!trip.isStartConfirmed()) {

            if (data.getType() == trip.getType()) {

                trip.setStartConfirmedByID(data.getId());

                TripLogContract.updateTrip(getContentResolver(), trip);

                if (trip.hasStartCoords()) {

                    NotificationSender.sendTripStateNotification(this, LocationRequestService.COMMAND_STORE_START_LOCATION, trip);

                } else {
                    sendLocationRequest(LocationRequestService.COMMAND_STORE_START_LOCATION, trip);
                }

            } else {
                // mark trip as a incorrectly detected ( due to e.g. biking detected during a supposed car trip)
                confirmTripAsIncorrect(trip);

            }
        }
    }



    private void startTrip(RawData startedBy, boolean startConfirmed) {

        Trip trip = new Trip(startedBy.getTimestamp(), startedBy.getType());

        trip.setStartedByID(startedBy.getId());

        trip.setStartedByType(Constants.RESULT_TYPE_ACTIVITY_RECOGNITION);

        if (startConfirmed) {

            trip.setStartConfirmedByID(startedBy.getId());

        }

        Uri uri = TripLogContract.addTrip(getContentResolver(), trip);

        int id = Integer.valueOf(uri.getLastPathSegment());

        trip.setID(id);

        if (!trip.hasStartCoords()) {

            sendLocationRequest(LocationRequestService.COMMAND_STORE_START_LOCATION, trip);

        }
    }

    private void endTrip(Trip trip, RawData endedBy, boolean endConfirmed) {

        trip.setEndTime(timestamp);

        if (endConfirmed) {

            trip.setEndConfirmedByID(endedBy.getId());

        }

        if (trip.getEndedByID() < 0) {

            trip.setEndedByID(endedBy.getId());

            trip.setEndedByType(Constants.RESULT_TYPE_ACTIVITY_RECOGNITION);
        }

        TripLogContract.updateTrip(getContentResolver(), trip);

        if (!trip.hasEndCoords()) {

            sendLocationRequest(LocationRequestService.COMMAND_STORE_END_LOCATION, trip);

        }
    }

    private void checkForPossibleTripStart(ArrayList<RawData> rawDataArrayList) {

        for (RawData data : rawDataArrayList) {

            if (isPossibleTripStart(data)) {

                startTrip(data, Trip.UNCONFIRMED);

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

    private void sendLocationRequest(int requestType, Trip trip) {

        Intent intent = new Intent(this, LocationRequestService.class);

        intent.putExtra(LocationRequestService.COMMAND, requestType);

        intent.putExtra(LocationRequestService.EXTRA_TRIP_ID, trip.getID());

        intent.putExtra(TESTING, testing);

        startService(intent);

    }
}
