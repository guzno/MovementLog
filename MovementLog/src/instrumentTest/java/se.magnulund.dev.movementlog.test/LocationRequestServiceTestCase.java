package se.magnulund.dev.movementlog.test;// Created by Gustav on 28/01/2014.

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.test.ServiceTestCase;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.services.LocationRequestService;
import se.magnulund.dev.movementlog.trips.Trip;

public class LocationRequestServiceTestCase extends ServiceTestCase<LocationRequestService> {
    private static final String TAG = "TripRecognitionIntentServiceTestCase";
    private static final String TESTING = "testing";
    private Context context;

    /**
     * Constructor
     */
    public LocationRequestServiceTestCase() {
        super(LocationRequestService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getContext();
    }

    @Override
    protected void setupService() {
        super.setupService();

    }

    public synchronized void testLocationRequest(){

        Trip trip = new Trip(System.currentTimeMillis(), DetectedActivity.IN_VEHICLE);

        Uri uri = TripLogContract.addTrip(context.getContentResolver(), trip);

        trip = TripLogContract.getTrip(context.getContentResolver(), uri);

        Intent intent = LocationRequestService.getStoreStartLocationIntent(context,trip.getID());

        intent.putExtra(TESTING, true);

        startService(intent);

        while(!getService().requestHandled) {

        }

        trip = TripLogContract.getTrip(context.getContentResolver(), uri);

        Log.e(TAG, "Trip info: "+trip.getStartCoords().getProvider()+" @ "+trip.getStartCoords().getTimestamp());

        try {
            wait(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startService(intent);

        Log.e(TAG, "startService done!");

        try {
            wait(21000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startService(intent);

        Log.e(TAG, "startService done!");

        try {
            wait(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startService(intent);

        Log.e(TAG, "startService done!");

    }
}
