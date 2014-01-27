package se.magnulund.dev.movementlog.services;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.trips.Trip;
import se.magnulund.dev.movementlog.trips.TripCoords;
import se.magnulund.dev.movementlog.utils.Constants;
import se.magnulund.dev.movementlog.utils.NotificationSender;

public class LocationLogIntentService extends IntentService {
    private static final String TAG = "LocationLogIntentService";

    public static final String LOCATION_REQUEST_TYPE = "TripTracker_location_request_type";
    public static final int START_LOCATION = 0;
    public static final int END_LOCATION = 1;

    public static final String LOCATION_REQUEST_TRIP_ID = "TripTracker_location_request_trip_id";

    public LocationLogIntentService() {
        super(TAG);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)){

            storeLocationRequestResult(intent);

        } else {

            Log.d(TAG, "Unhandled intent");

        }
    }

    private void storeLocationRequestResult(Intent intent) {

        if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {

            Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);

            int tripID = intent.getIntExtra(LOCATION_REQUEST_TRIP_ID, -1);

            ContentResolver resolver = getContentResolver();

            Trip trip = TripLogContract.getTrip(resolver, ContentUris.withAppendedId(TripLogContract.CONTENT_URI, tripID));

            switch (intent.getIntExtra(LOCATION_REQUEST_TYPE, -1)) {

                case START_LOCATION:

                    trip.setStartCoords(TripCoords.fromLocation(location));

                    assert trip.hasStartCoords();

                    NotificationSender.sendTripStateNotification(this, START_LOCATION, trip);

                    break;

                case END_LOCATION:

                    trip.setEndCoords(TripCoords.fromLocation(location));

                    assert trip.hasEndCoords();

                    NotificationSender.sendTripStateNotification(this, END_LOCATION, trip);

                    break;

                default:

                    Log.d(TAG, "Unknown location request type");

            }

            TripLogContract.updateTrip(resolver, trip);

        } else {

            Log.d(TAG, "Location update without location received!");

        }
    }
}
