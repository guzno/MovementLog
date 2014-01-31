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
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

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
import se.magnulund.dev.movementlog.trips.TripCoords;
import se.magnulund.dev.movementlog.utils.DateTimeUtil;
import se.magnulund.dev.movementlog.utils.NotificationSender;

public class LocationRequestService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    public static final String COMMAND = "location_request_command";
    public static final int COMMAND_START_UPDATES = 1;
    public static final int COMMAND_STOP_UPDATES = 2;
    public static final int COMMAND_STORE_START_LOCATION = 3;
    public static final int COMMAND_STORE_END_LOCATION = 4;
    public static final String EXTRA_TRIP_ID = "location_request_extra_trip_id";
    private static final String TAG = "LocationRequestService";
    private static final String TESTING = "testing";
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public boolean requestHandled;
    public LocationFuture locationFuture;
    LocationClient locationClient;
    private boolean updatesRequested;
    private int requestType;
    private int tripId;
    private boolean testing;
    private LocationRequest backgroundLocationRequest;
    private LocationRequest updatedLocationRequest;

    private ExecutorService mThreadPool;
    private Handler looperHandler;

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
        updatedLocationRequest.setInterval(5 * DateTimeUtil.MILLIS_PER_SECOND);
        updatedLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationClient = new LocationClient(this, this, this);

        LooperThread looperThread = new LooperThread();
        looperThread.setPriority(Thread.MAX_PRIORITY);

        mThreadPool = Executors.newSingleThreadExecutor();
        mThreadPool.execute(looperThread);

        locationFuture = LocationFuture.newInstance();

        updatesRequested = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        requestHandled = false;
        testing = intent.getBooleanExtra(TESTING, false);
        requestType = intent.getIntExtra(COMMAND, -1);
        tripId = intent.getIntExtra(EXTRA_TRIP_ID, -1);

        if (testing) { Log.d(TAG, "type" + requestType); Log.d(TAG, "id:" + tripId); }

        if (servicesConnected() && locationClient.isConnected()) {
            handleRequest();
        } else {
            if(testing){Log.d(TAG, "connecting locationClient");}
            locationClient.connect();
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
                locationClient.removeLocationUpdates(locationFuture);
                updatesRequested = false;
                break;
            default:
                Log.d(TAG, "Unhandled intent");
        }

        requestHandled = true;
    }

    private void initLocationUpdates() {

        locationFuture.clearResult();
        locationFuture.initLocation(locationClient.getLastLocation());

        sendLocationRequest(backgroundLocationRequest);

        updatesRequested = true;
    }

    private void storeLocation() {

        Location location;

        try {
            location = getCurrentLocation();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            location = null;
            e.printStackTrace();
        }

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
                        Log.d(TAG, "Unknown location request type");
                }

                TripLogContract.updateTrip(resolver, trip);

            } else {
                Log.d(TAG, "Trip with id: " + tripId + " was null...");
            }


        } else {

            Log.e(TAG, "Could not retrieve location!");

        }

    }

    private Location getCurrentLocation() throws InterruptedException, ExecutionException, TimeoutException {

        Location location = locationFuture.get(2, TimeUnit.SECONDS);

        if (testing && location != null) {
            Log.d(TAG, "stored Location from: " + location.getProvider() + " @ " + location.getTime());
            Log.d(TAG, "location is this old: " + Long.toString(LocationUtils.getLocationAgeInSeconds(location)) + " s");
        }

        if (location == null || LocationUtils.getLocationAgeInSeconds(location) > 20) {
            locationFuture.clearResult();
            sendLocationRequest(updatedLocationRequest);
            Location newLocation = locationFuture.get(30, TimeUnit.SECONDS);
            sendLocationRequest(backgroundLocationRequest);

            if (testing && newLocation != null) {
                Log.d(TAG, "new Location from: " + newLocation.getProvider() + " @ " + newLocation.getTime());
            }

            if (newLocation != null) {
                location = newLocation;
            }
        }

        return location;
    }


    @Override
    public void onDestroy() {

        locationClient.removeLocationUpdates(locationFuture);

        looperHandler.getLooper().quit();

        locationClient.disconnect();

        super.onDestroy();

    }

    private void sendLocationRequest(LocationRequest request) {

        locationClient.requestLocationUpdates(request, locationFuture, looperHandler.getLooper());

    }

    private boolean servicesConnected() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {

            return true;

        } else {

            sendErrorNotification(resultCode);

            return false;

        }
    }

    private void sendErrorNotification(int resultCode) {

        PendingIntent pendingIntent = GooglePlayServicesUtil.getErrorPendingIntent(resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Oh noes!")
                .setContentText("Google play services is unhappy (errocode:" + resultCode + ")")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_lock_lock, "And more", pendingIntent).build();

        NotificationSender.sendCustomNotification(this, notification);

    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.d(TAG, "Connected!");

        handleRequest();

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        sendErrorNotification(connectionResult.getErrorCode());

        requestHandled = true;

    }

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
