package se.magnulund.dev.movementlog.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
// Remove GooglePlayServicesClient imports if no longer directly used after refactor
// import com.google.android.gms.common.GooglePlayServicesClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.location.LocationFuture;
import se.magnulund.dev.movementlog.location.LocationUtils;
import se.magnulund.dev.movementlog.trips.Trip;
import se.magnulund.dev.movementlog.location.TripCoords;
import se.magnulund.dev.movementlog.utils.DateTimeUtil;
import se.magnulund.dev.movementlog.utils.NotificationSender;

// Removed GooglePlayServicesClient interfaces
public class LocationRequestService extends Service {
    public static final String COMMAND = "location_request_command";
    public static final int COMMAND_START_UPDATES = 1;
    public static final int COMMAND_STOP_UPDATES = 2;
    public static final int COMMAND_STORE_START_LOCATION = 3;
    public static final int COMMAND_STORE_END_LOCATION = 4;
    public static final String EXTRA_TRIP_ID = "location_request_extra_trip_id";
    private static final String TAG = "LocationRequestService";
    private static final String TESTING = "testing";
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000; // Keep for error dialog
    public boolean requestHandled;
    public LocationFuture locationFuture; // This will need to adapt or be replaced
    // LocationClient locationClient; // Replaced by FusedLocationProviderClient
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private boolean updatesRequested;
    private int requestType;
    private int tripId;
    private boolean testing;
    private LocationRequest backgroundLocationRequest;
    private LocationRequest updatedLocationRequest;

    private ExecutorService mThreadPool;
    private Handler looperHandler;
    private final IBinder mBinder = new LocalBinder(); // Binder for ServiceTestRule

    // Binder class
    public class LocalBinder extends Binder {
        public LocationRequestService getService() {
            return LocationRequestService.this;
        }
    }

    public static Intent getStartUpdatesIntent(Context context) {
        Intent intent = new Intent(context, LocationRequestService.class);
        intent.putExtra(COMMAND, COMMAND_START_UPDATES);
        return intent;
    }

    public static Intent getStopUpdatesIntent(Context context) {
        Intent intent = new Intent(context, LocationRequestService.class);
        intent.putExtra(COMMAND, COMMAND_STOP_UPDATES);
        return intent;
    }

    public static Intent getStoreStartLocationIntent(Context context, int tripId) {
        Intent intent = new Intent(context, LocationRequestService.class);
        intent.putExtra(COMMAND, COMMAND_STORE_START_LOCATION);
        intent.putExtra(EXTRA_TRIP_ID, tripId);
        return intent;
    }

    public static Intent getStoreEndLocationIntent(Context context, int tripId) {
        Intent intent = new Intent(context, LocationRequestService.class);
        intent.putExtra(COMMAND, COMMAND_STORE_END_LOCATION);
        intent.putExtra(EXTRA_TRIP_ID, tripId);
        return intent;
    }

    @Override
    public void onCreate() {

        backgroundLocationRequest = new LocationRequest();
        backgroundLocationRequest.setFastestInterval(5 * DateTimeUtil.MILLIS_PER_SECOND);
        backgroundLocationRequest.setInterval(30 * DateTimeUtil.MILLIS_PER_MINUTE);
        backgroundLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        updatedLocationRequest = new LocationRequest();
        updatedLocationRequest.setFastestInterval(DateTimeUtil.MILLIS_PER_SECOND);
        updatedLocationRequest.setInterval(DateTimeUtil.MILLIS_PER_SECOND);
        // updatedLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // Use new constants
        updatedLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Example, adjust as needed

        // locationClient = new LocationClient(this, this, this); // Old client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LooperThread looperThread = new LooperThread();
        looperThread.setPriority(Thread.MAX_PRIORITY);

        mThreadPool = Executors.newSingleThreadExecutor();
        mThreadPool.execute(looperThread);

        locationFuture = LocationFuture.newInstance();

        updatesRequested = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder; // Return the binder
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        requestHandled = false;
        testing = intent.getBooleanExtra(TESTING, false);
        requestType = intent.getIntExtra(COMMAND, -1);
        tripId = intent.getIntExtra(EXTRA_TRIP_ID, -1);

        if (testing) { Log.d(TAG, "type" + requestType); Log.d(TAG, "id:" + tripId); }

        if (servicesConnected()) { // servicesConnected now just checks availability
            handleRequest(); // Actual connection/request logic is now within handleRequest
        } else {
            // Error already sent by servicesConnected if needed
            stopSelf(); // Stop service if Play Services not available
        }

        return START_STICKY;
    }

    private void handleRequest() {

        switch (requestType) {
            case COMMAND_START_UPDATES:
                initLocationUpdates();
                break;
            case COMMAND_STORE_START_LOCATION:
            case COMMAND_STORE_END_LOCATION:
                if (!updatesRequested) {
                    initLocationUpdates();
                }
                storeLocation();
                break;
            case COMMAND_STOP_UPDATES:
                if (mFusedLocationClient != null && mLocationCallback != null) {
                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                }
                updatesRequested = false;
                break;
            default:
                Log.d(TAG, "Unhandled intent");
        }

        requestHandled = true;
    }

    private void initLocationUpdates() {
        // Replace direct use of locationFuture.initLocation with FusedLocationProviderClient
        try {
            // TODO: Add permission check here for ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION
            // This is a placeholder for the actual permission check logic which is more involved.
            // For now, assuming permissions are granted for compilation.
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                locationFuture.initLocation(location); // locationFuture might still be useful for internal logic
                            }
                        }
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted", e);
            // Handle permission not granted - perhaps stop the service or notify user
            return;
        }

        sendLocationRequest(backgroundLocationRequest);
        updatesRequested = true;
    }

    private void storeLocation() {
        // This method now relies on locationFuture being updated by the LocationCallback
        // or getLastLocation. The logic for fetching current location needs to be adapted.
        // For simplicity, let's assume locationFuture.get() can still be used if populated by callback.

        // TODO: This needs careful review. getCurrentLocation's logic of switching
        // request priorities and timeouts is complex and might not map directly.
        // The LocationFuture will be updated by the mLocationCallback.
        // We might need a different strategy to "force" an update if the last known location is old.

        // Simplified for now: try to get a location from the future.
        // A more robust solution would involve requesting a single fresh location update if needed.
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    if (LocationUtils.getLocationAgeInSeconds(location) < 60) { // If location is recent enough
                        processLocationForStorage(location);
                    } else {
                        // Location is too old, request a new one
                        requestSingleUpdateAndStore();
                    }
                } else {
                    requestSingleUpdateAndStore();
                }
            }
        });
    }

    private void requestSingleUpdateAndStore() {
        try {
            final LocationRequest singleUpdateRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(1);

            LocationCallback singleUpdateCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null && locationResult.getLastLocation() != null) {
                        processLocationForStorage(locationResult.getLastLocation());
                    } else {
                        Log.e(TAG, "Failed to get single update location for storage.");
                    }
                    // Re-register for background updates
                    if (updatesRequested) {
                        sendLocationRequest(backgroundLocationRequest);
                    }
                }
            };
            // Ensure looper is available for the callback
            mFusedLocationClient.requestLocationUpdates(singleUpdateRequest, singleUpdateCallback, looperHandler.getLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted for single update", e);
        }
    }


    private void processLocationForStorage(Location location) {
        if (location != null) {
            ContentResolver resolver = getContentResolver();
            Trip trip = TripLogContract.getTrip(resolver, ContentUris.withAppendedId(TripLogContract.CONTENT_URI, tripId));

            if (trip != null) {
                switch (requestType) {
                    case COMMAND_STORE_START_LOCATION:
                        trip.setStartCoords(TripCoords.fromLocation(location));
                        assert trip.hasStartCoords();
                        if (trip.isStartConfirmed()) {
                            NotificationSender.sendTripStateNotification(this, COMMAND_STORE_START_LOCATION, trip);
                        }
                        break;
                    case COMMAND_STORE_END_LOCATION:
                        trip.setEndCoords(TripCoords.fromLocation(location));
                        assert trip.hasEndCoords();
                        if (trip.isEndConfirmed()) {
                            NotificationSender.sendTripStateNotification(this, COMMAND_STORE_END_LOCATION, trip);
                        }
                        break;
                    default:
                        Log.d(TAG, "Unknown location request type for storage");
                }
                TripLogContract.updateTrip(resolver, trip);
            } else {
                Log.d(TAG, "Trip with id: " + tripId + " was null for storage.");
            }
        } else {
            Log.e(TAG, "Could not retrieve location for storage!");
        }
    }

    // getCurrentLocation() is largely replaced by the callback mechanism or specific requests.
    // The old logic of switching priorities within getCurrentLocation needs to be rethought.
    // For now, I'll keep it commented out and rely on getLastLocation + single updates.
    /*
    private Location getCurrentLocation() throws InterruptedException, ExecutionException, TimeoutException {
        // ... old logic ...
    }
    */

    @Override
    public void onDestroy() {
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        if (looperHandler != null && looperHandler.getLooper() != null) {
            looperHandler.getLooper().quit();
        }
        // locationClient.disconnect(); // No longer needed
        super.onDestroy();
    }

    private void sendLocationRequest(LocationRequest request) {
        // Ensure callback is initialized
        if (mLocationCallback == null) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        for (Location location : locationResult.getLocations()) {
                            // Update LocationFuture or handle location directly
                            locationFuture.setResult(location); // Careful with LocationFuture's design
                            if (testing) {
                                Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                            }
                        }
                    }
                }
            };
        }
        try {
            // TODO: Add permission check here
            mFusedLocationClient.requestLocationUpdates(request, mLocationCallback, looperHandler.getLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted for request", e);
            // Handle permission not granted
        }
    }

    private boolean servicesConnected() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            sendErrorNotification(resultCode);
            return false;
        }
    }

    private void sendErrorNotification(int resultCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        PendingIntent pendingIntent = apiAvailability.getErrorResolutionPendingIntent(this, resultCode, CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // Use NotificationCompat.Builder for broader compatibility
        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, "error_channel") // Replace "error_channel" with a real channel ID
                .setContentTitle("Oh noes!")
                .setContentText("Google Play services is unhappy (errorcode:" + resultCode + ")")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (pendingIntent != null) { // getErrorResolutionPendingIntent can return null
             builder.addAction(android.R.drawable.ic_menu_manage, "Fix Now", pendingIntent); // Example action
        }
        
        // TODO: Create Notification Channel for Android O+
        // NotificationSender.sendCustomNotification(this, builder.build()); // Assuming NotificationSender is updated or handles channels

        // Simplified direct send for now, assuming NotificationSender handles channels
        NotificationSender.sendCustomNotification(this, builder.build());
    }

    // onConnected, onDisconnected, onConnectionFailed are no longer needed with FusedLocationProviderClient direct usage.
    // The FusedLocationProviderClient uses a Task-based API (addOnSuccessListener, etc.)

    private class LooperThread extends Thread {

        @Override
        public void run() {
            Log.d(TAG, "looperthread " + this.getId() + " running");
            Looper.prepare();
            looperHandler = new Handler();
            Log.d(TAG, "looperthread " + this.getId() + " in loop");
            Looper.loop();
            Log.d(TAG, "looperthread " + this.getId() + " stopping");
        }
    }

}
