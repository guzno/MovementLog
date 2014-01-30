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
import android.os.SystemClock;
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
import se.magnulund.dev.movementlog.trips.Trip;
import se.magnulund.dev.movementlog.trips.TripCoords;
import se.magnulund.dev.movementlog.utils.DateTimeUtil;
import se.magnulund.dev.movementlog.utils.NotificationSender;

public class LocationRequestService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String TAG = "LocationRequestService";

    private static final String TESTING = "testing";

    public static final String COMMAND = "location_request_command";
    public static final int COMMAND_START_UPDATES = 1;
    public static final int COMMAND_STOP_UPDATES = 2;
    public static final int COMMAND_STORE_START_LOCATION = 3;
    public static final int COMMAND_STORE_END_LOCATION = 4;

    public static final String EXTRA_TRIP_ID = "location_request_extra_trip_id";

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private boolean updatesRequested;

    private int requestType;
    private int tripId;
    private boolean testing;

    public boolean requestHandled;

    LocationClient locationClient;

    private LocationRequest backgroundLocationRequest;

    private LocationRequest updatedLocationRequest;

    public LocationFuture locationFuture;

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

        updatedLocationRequest.setSmallestDisplacement(0);

        locationFuture = LocationFuture.newInstance();

        locationClient = new LocationClient(this, this, this);

        updatesRequested = false;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
    public int onStartCommand(Intent intent, int flags, int startId) {

        testing = intent.getBooleanExtra(TESTING, false);

        requestType = intent.getIntExtra(COMMAND, -1);

        tripId = intent.getIntExtra(EXTRA_TRIP_ID, -1);

        if (testing) {
            Log.e(TAG, "type" + requestType);
            Log.e(TAG, "id:" + tripId);
        }

        requestHandled = false;

        if (servicesConnected() && locationClient.isConnected()) {
            if (testing) {
                Log.e(TAG, "handling request");
            }

            handleRequest();

        } else {
            Log.e(TAG, "connecting locationClient");
            locationClient.connect();

        }

        return START_STICKY;

    }

    private void handleRequest() {

        switch (requestType) {

            case COMMAND_START_UPDATES:

                sendLocationRequest(backgroundLocationRequest);

                updatesRequested = true;

                break;

            case COMMAND_STORE_START_LOCATION:
            case COMMAND_STORE_END_LOCATION:

                if (!updatesRequested) {

                    sendLocationRequest(backgroundLocationRequest);

                    updatesRequested = true;

                }

                try {

                    storeLocation();

                } catch (ExecutionException | InterruptedException | TimeoutException e) {

                    e.printStackTrace();

                }

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

    private void storeLocation() throws ExecutionException, InterruptedException, TimeoutException {

        Location location;

        location = locationFuture.get(2 * DateTimeUtil.MILLIS_PER_SECOND, TimeUnit.MILLISECONDS);

        //location = locationClient.getLastLocation();

        if (testing && location != null) {
            Log.e(TAG, "stored Location from: " + location.getProvider() + " @ " + location.getTime());
            Log.e(TAG, "location is this old: "+Long.toString(Math.abs(SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos())/DateTimeUtil.NANOS_PER_SECOND)+" s");
        }

        if (location == null || Math.abs(SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) > 20 * DateTimeUtil.NANOS_PER_SECOND) {

            locationFuture.clearResult();

            sendLocationRequest(updatedLocationRequest);

            location = locationFuture.get(20 * DateTimeUtil.MILLIS_PER_SECOND, TimeUnit.MILLISECONDS);

            sendLocationRequest(backgroundLocationRequest);

            if (testing && location != null) {
                Log.e(TAG, "new Location from: " + location.getProvider() + " @ " + location.getTime());
            }

        }

        if (location != null) {

            ContentResolver resolver = getContentResolver();

            Trip trip = TripLogContract.getTrip(resolver, ContentUris.withAppendedId(TripLogContract.CONTENT_URI, tripId));

            if (trip != null) {

                switch (requestType) {
                    case COMMAND_STORE_START_LOCATION:
                        trip.setStartCoords(TripCoords.fromLocation(location));
                        assert trip.hasStartCoords();
                        NotificationSender.sendTripStateNotification(this, COMMAND_STORE_START_LOCATION, trip);
                        break;
                    case COMMAND_STORE_END_LOCATION:
                        trip.setEndCoords(TripCoords.fromLocation(location));
                        assert trip.hasEndCoords();
                        NotificationSender.sendTripStateNotification(this, COMMAND_STORE_END_LOCATION, trip);
                        break;
                    default:
                        Log.d(TAG, "Unknown location request type");
                }

                TripLogContract.updateTrip(resolver, trip);

            } else {
                Log.e(TAG, "Trip with id: "+tripId+" was null...");
            }


        } else {

            Log.e(TAG, "Could not retrieve location!");

        }

    }

    private class LocationThread extends Thread {

        public LocationRequest mRequest;

        public void setRequest(LocationRequest mRequest) {
            this.mRequest = mRequest;
        }

        @Override
        public void run() {
            Log.e(TAG, "I be runnin' !!!!");
            Looper.prepare();
            Handler handler = new Handler();
            locationClient.requestLocationUpdates(mRequest, locationFuture, handler.getLooper());
            Looper.loop();
        }

    }

    private void sendLocationRequest(LocationRequest request) {
        ExecutorService mThreadPool = Executors.newSingleThreadExecutor();

        LocationThread lt = new LocationThread();
        lt.setRequest(request);

        mThreadPool.execute(lt);
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

        Log.e(TAG, "Connected!");

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
}
