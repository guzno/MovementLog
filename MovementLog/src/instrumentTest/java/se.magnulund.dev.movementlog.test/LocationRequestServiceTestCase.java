package se.magnulund.dev.movementlog.test; // Created by Gustav on 28/01/2014.

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ServiceTestRule;

import com.google.android.gms.location.DetectedActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;


import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.services.LocationRequestService;
import se.magnulund.dev.movementlog.trips.Trip;

@RunWith(AndroidJUnit4.class)
public class LocationRequestServiceTestCase { // No longer extends ServiceTestCase
    private static final String TAG = "LocationRequestServiceTest"; // Changed TAG for clarity
    private static final String TESTING = "testing";
    private Context context;

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    private LocationRequestService mService;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Clean up before each test
        TripLogContract.deleteAllTrips(context.getContentResolver());
    }

    @After
    public void tearDown() throws Exception {
        // Clean up after each test
        TripLogContract.deleteAllTrips(context.getContentResolver());
    }

    private void startServiceAndWait(Intent intent, int timeoutMillis) throws InterruptedException, TimeoutException {
        serviceRule.startService(intent);
        mService = ((LocationRequestService.LocalBinder) serviceRule.bindService(intent)).getService(); // Assuming LocalBinder

        long startTime = System.currentTimeMillis();
        while(!mService.requestHandled) {
            Thread.sleep(100); // Poll
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                throw new TimeoutException("Service request not handled in time");
            }
        }
        mService.requestHandled = false; // Reset for next call
    }


    @Test
    public void testLocationRequest() throws TimeoutException, InterruptedException {

        Trip trip = new Trip(System.currentTimeMillis(), DetectedActivity.IN_VEHICLE);
        Uri uri = TripLogContract.addTrip(context.getContentResolver(), trip);
        assertNotNull(uri);
        trip = TripLogContract.getTrip(context.getContentResolver(), uri);
        assertNotNull(trip);

        Intent intent = LocationRequestService.getStoreStartLocationIntent(context, trip.getID());
        intent.putExtra(TESTING, true);

        // First call
        Log.d(TAG, "Starting service for first location request...");
        startServiceAndWait(intent, 5000); // Wait up to 5 seconds for the service to handle it
        Log.d(TAG, "Service handled first request.");

        Trip updatedTrip = TripLogContract.getTrip(context.getContentResolver(), uri);
        assertNotNull(updatedTrip);
        if (updatedTrip.hasStartCoords()) { // Location might not be available immediately
            Log.i(TAG, "Trip info after 1st call: " + updatedTrip.getStartCoords().getProvider() + " @ " + updatedTrip.getStartCoords().getTimestamp());
        } else {
            Log.w(TAG, "No start coordinates after 1st call.");
        }

        // Simulate delay for next operation
        Thread.sleep(2000); // Short delay before next "event"

        // Second call (re-using intent for simplicity, in real scenario might be different action)
        Log.d(TAG, "Starting service for second location attempt...");
        // If the service is still running, bindService might be faster or startService might be a no-op if not stopped.
        // For this test structure, we assume startService will trigger onStartCommand again.
        startServiceAndWait(intent, 10000); // Longer timeout if location acquisition is expected
        Log.d(TAG, "Service handled second request.");
        updatedTrip = TripLogContract.getTrip(context.getContentResolver(), uri);
        assertNotNull(updatedTrip);
         if (updatedTrip.hasStartCoords()) {
            Log.i(TAG, "Trip info after 2nd call: " + updatedTrip.getStartCoords().getProvider() + " @ " + updatedTrip.getStartCoords().getTimestamp());
        } else {
            Log.w(TAG, "No start coordinates after 2nd call.");
        }

        // Third call
        Thread.sleep(2000);
        Log.d(TAG, "Starting service for third location attempt...");
        startServiceAndWait(intent, 5000);
        Log.d(TAG, "Service handled third request.");
         updatedTrip = TripLogContract.getTrip(context.getContentResolver(), uri);
        assertNotNull(updatedTrip);
         if (updatedTrip.hasStartCoords()) {
            Log.i(TAG, "Trip info after 3rd call: " + updatedTrip.getStartCoords().getProvider() + " @ " + updatedTrip.getStartCoords().getTimestamp());
        } else {
            Log.w(TAG, "No start coordinates after 3rd call.");
        }
        Log.i(TAG, "testLocationRequest completed.");
    }
}
