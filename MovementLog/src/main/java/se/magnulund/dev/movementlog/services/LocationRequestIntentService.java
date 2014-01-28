package se.magnulund.dev.movementlog.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.location.LocationFuture;
import se.magnulund.dev.movementlog.trips.Trip;
import se.magnulund.dev.movementlog.trips.TripCoords;
import se.magnulund.dev.movementlog.utils.DateTimeUtil;
import se.magnulund.dev.movementlog.utils.NotificationSender;

public class LocationRequestIntentService extends IntentService {
    private static final String TAG = "LocationRequestIntentService";

    public static final String LOCATION_REQUEST_TYPE = "TripTracker_location_request_type";
    public static final int START_LOCATION = 0;
    public static final int END_LOCATION = 1;

    public static final String LOCATION_REQUEST_TRIP_ID = "TripTracker_location_request_trip_id";

    private int requestType;
    private int tripId;

    public LocationRequestIntentService() {
        super(TAG);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        requestType = intent.getIntExtra(LOCATION_REQUEST_TYPE, -1);

        tripId = intent.getIntExtra(LOCATION_REQUEST_TRIP_ID, -1);

        if (requestType > -1 && tripId > -1) {

            Location location = requestLocation();

            if (location != null) {
                storeLocation(location);
            } else {
                Log.d(TAG, "Location request failed!");
            }

        } else {

            Log.d(TAG, "Unhandled intent");

        }
    }

    private Location requestLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String providerName = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(providerName);

        if (location == null || System.nanoTime() - location.getElapsedRealtimeNanos() > DateTimeUtil.NANOS_PER_MINUTE) {

            LocationFuture locationFuture = LocationFuture.newInstance(providerName);

            locationManager.requestSingleUpdate(providerName, locationFuture, null);

            Location newLocation = null;

            try {
                newLocation = locationFuture.get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }

            if (null != newLocation) {
                location = newLocation;
            }
        }

        return location;
    }

    private void storeLocation(Location location) {

        ContentResolver resolver = getContentResolver();

        Trip trip = TripLogContract.getTrip(resolver, ContentUris.withAppendedId(TripLogContract.CONTENT_URI, tripId));

        switch (requestType) {

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
    }
}
