package se.magnulund.dev.movementlog.triprecognition;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.magnulund.dev.movementlog.MainActivity;
import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.contracts.RawDataContract;
import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.rawdata.RawData;
import se.magnulund.dev.movementlog.trips.Trip;
import se.magnulund.dev.movementlog.utils.NotificationSender;

public class TripRecognitionIntentService extends IntentService {

    private static final String TAG = "TripRecognitionIntentService";

    public static final String RESULT_TYPE = "TripRecognitionIntentService_result_type";

    public static final int RESULT_TYPE_ACTIVITY_RECOGNITION = 0;
    public static final int RESULT_TYPE_BLUETOOTH_CONNECTION_CHANGE = 1;
    public static final int RESULT_TYPE_LOCATION_RESULT = 2;
    public static final int RESULT_TYPE_CHARGING_STATE_CHANGED = 3;

    public static final String LOCATION_REQUEST_TYPE = "TripRecognitionIntentService_location_request_type";
    public static final int START_LOCATION = 0;
    public static final int END_LOCATION = 1;
    public static final String LOCATION_REQUEST_TRIP_ID = "TripRecognitionIntentService_location_request_trip_id";

    private static final int POSSIBLE_CAR_TRIP_CONFIDENCE_THRESHOLD = 40;
    private static final int POSSIBLE_BIKE_TRIP_CONFIDENCE_THRESHOLD = 40;

    public static final int NANOS_PER_MILLI = 1000;
    public static final int NANOS_PER_SECOND = 1000 * NANOS_PER_MILLI;
    public static final int NANOS_PER_MINUTE = 60 * NANOS_PER_SECOND;

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

        } else if (intent.hasExtra(RESULT_TYPE)) {
            switch (intent.getIntExtra(RESULT_TYPE, -1)) {
                case RESULT_TYPE_LOCATION_RESULT:
                    if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
                        Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);

                        int tripID = intent.getIntExtra(LOCATION_REQUEST_TRIP_ID, -1);

                        ContentResolver resolver = getContentResolver();

                        Trip trip = TripLogContract.getTrip(resolver, ContentUris.withAppendedId(TripLogContract.CONTENT_URI, tripID));

                        switch (intent.getIntExtra(LOCATION_REQUEST_TYPE, -1)){
                            case START_LOCATION:
                                trip.setStartLocation(location);
                                assert location != null;
                                NotificationSender.sendNotification(this,
                                        "Trip started at",
                                        "lat: "+Location.convert(location.getLatitude(), Location.FORMAT_DEGREES)+"\r\n"+
                                                "long: "+Location.convert(location.getLongitude(), Location.FORMAT_DEGREES)
                                );
                                break;
                            case END_LOCATION:
                                trip.setEndLocation(location);
                                assert location != null;
                                NotificationSender.sendNotification(this,
                                        "Trip ended at",
                                        "lat: "+Location.convert(location.getLatitude(), Location.FORMAT_DEGREES)+"\r\n"+
                                                "long: "+Location.convert(location.getLongitude(), Location.FORMAT_DEGREES)
                                );
                                break;
                            default:
                                Log.d(TAG, "Unknown location request type");
                        }

                        TripLogContract.updateTrip(resolver, trip);

                    } else {
                        Log.d(TAG, "Location update without location received!");
                    }

                    break;
                default:
            }

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

            switch (activityType) {

                case DetectedActivity.IN_VEHICLE:
                case DetectedActivity.ON_BICYCLE:

                    if (onGoingTrip.getStartConfirmedByID() < 0) {
                        if (activityType == onGoingTrip.getType()) {

                            onGoingTrip.setStartConfirmedByID(mostProbableActivity.getId());
                            TripLogContract.updateTrip(getContentResolver(), onGoingTrip);
                        } else {
                            TripLogContract.deleteTrip(getContentResolver(), onGoingTrip);
                        }
                    }

                case DetectedActivity.ON_FOOT:
                    if (onGoingTrip.getStartConfirmedByID() > 0) {
                        // end confirmed trip
                        endTrip(onGoingTrip, mostProbableActivity, true);
                    } else {
                        // delete onconfirmed trip
                        TripLogContract.deleteTrip(getContentResolver(), onGoingTrip);
                    }
                    break;

                case DetectedActivity.STILL:
                case DetectedActivity.TILTING:
                case DetectedActivity.UNKNOWN:
                default:
            }

            //check if walking was detected and end the trip

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

        sendLocationRequest(START_LOCATION, trip);
    }

    private void endTrip(Trip trip, RawData endedBy, boolean endConfirmed) {

        trip.setEndTime(timestamp);

        if (endConfirmed) {
            trip.setEndConfirmedByID(endedBy.getId());
        }

        if (trip.getEndedByID() < 0) {

            trip.setEndedByID(endedBy.getId());

            trip.setEndedByType(currentResultType);
        }

        TripLogContract.updateTrip(getContentResolver(), trip);

        sendLocationRequest(END_LOCATION, trip);
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

    private void sendLocationRequest(int requestType, Trip trip){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String providerName = locationManager.getBestProvider(criteria, true);

        Location lastLocation = locationManager.getLastKnownLocation(providerName);

        long comparableTime = System.nanoTime();
        long locationTimestamp = lastLocation.getElapsedRealtimeNanos();

        Intent intent = new Intent(this, TripRecognitionIntentService.class);

        Bundle bundle = new Bundle();

        bundle.putInt(LOCATION_REQUEST_TYPE, requestType);
        bundle.putInt(LOCATION_REQUEST_TRIP_ID, trip.getID());

        if (comparableTime - locationTimestamp < NANOS_PER_MINUTE) {

            bundle.putParcelable(LocationManager.KEY_LOCATION_CHANGED, lastLocation);

            startService(intent);

        } else {
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            locationManager.requestSingleUpdate(providerName, pendingIntent);
        }

    }
}
