package se.magnulund.dev.movementlog.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
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

        timestamp = System.currentTimeMillis();

        if (ActivityRecognitionResult.hasResult(intent)) {

            processActivityRecognitionResult(intent);

        } else {

            Log.d(TAG, "Unhandled intent");

        }
    }

    // store data, analyze for trip start/end, initiate location requests and change update frequencies
    // TODO change update frequencies
    private void processActivityRecognitionResult(Intent intent) {

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

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

    private void confirmTripEnd(RawData data, Trip trip) {
        if (trip.isStartConfirmed()) {

            // end confirmed trip
            endTrip(trip, data, Trip.CONFIRMED);

        } else {

            // mark trip as a incorrectly detected (trip ended before it was confirmed)
            confirmTripAsIncorrect(trip);

        }
    }

    private void confirmTripAsIncorrect(Trip trip) {

        trip.setConfirmedAs(Trip.TRIP_CONFIRMED_AS_INCORRECT);

        TripLogContract.updateTrip(getContentResolver(), trip);

    }

    private void checkForTripConfirmation(RawData data, Trip trip) {

        if (!trip.isStartConfirmed()) {

            if (data.getType() == trip.getType()) {

                trip.setStartConfirmedByID(data.getId());

                TripLogContract.updateTrip(getContentResolver(), trip);

                if (trip.hasStartCoords()) {

                    NotificationSender.sendTripStateNotification(this, LocationLogIntentService.START_LOCATION, trip);

                }

            } else {
                // mark trip as a incorrectly detected ( due to e.g. biking detected during a supposed car trip)
                confirmTripAsIncorrect(trip);

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


    private void startTrip(RawData startedBy, boolean startConfirmed) {

        Trip trip = new Trip(startedBy.getTimestamp(), startedBy.getType());

        trip.setStartedByID(startedBy.getId());

        trip.setStartedByType(Constants.RESULT_TYPE_ACTIVITY_RECOGNITION);

        if (startConfirmed) {

            trip.setStartConfirmedByID(startedBy.getId());

        }

        TripLogContract.addTrip(getContentResolver(), trip);

        if (trip.hasStartCoords()) {

            sendLocationRequest(LocationLogIntentService.START_LOCATION, trip);

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

        if (trip.hasEndCoords()) {

            sendLocationRequest(LocationLogIntentService.END_LOCATION, trip);

        }
    }

    private void checkForPossibleTripStart(ArrayList<RawData> rawDataArrayList) {

        for (int i = 1; i < rawDataArrayList.size(); i++) {

            RawData data = rawDataArrayList.get(i);

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

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.e(TAG, "Sending location request!");

        Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String providerName = locationManager.getBestProvider(criteria, true);

        Location lastLocation = locationManager.getLastKnownLocation(providerName);

        Intent intent = new Intent(this, LocationLogIntentService.class);

        Bundle bundle = new Bundle();

        bundle.putInt(LocationLogIntentService.LOCATION_REQUEST_TYPE, requestType);

        bundle.putInt(LocationLogIntentService.LOCATION_REQUEST_TRIP_ID, trip.getID());

        if (lastLocation.getElapsedRealtimeNanos() - System.nanoTime() < DateTimeUtil.NANOS_PER_MINUTE) {

            Log.e(TAG, "Using lastknownlocation");

            bundle.putParcelable(LocationManager.KEY_LOCATION_CHANGED, lastLocation);

            intent.putExtras(bundle);

            startService(intent);

        } else {

            Log.e(TAG, "Requesting location fix");

            intent.putExtras(bundle);

            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            locationManager.requestSingleUpdate(providerName, pendingIntent);
        }

    }
}
