package se.magnulund.dev.movementlog.location;// Created by Gustav on 27/01/2014.

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationCallback; // Changed from LocationListener
import com.google.android.gms.location.LocationResult;   // Added for onLocationResult

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Removed DateTimeUtil import as it's not used in this file.
// import se.magnulund.dev.movementlog.utils.DateTimeUtil;

public class LocationFuture extends LocationCallback implements Future<Location> { // Changed to extend LocationCallback

    private static final String TAG ="LocationFuture";

    public static LocationFuture newInstance() {
        LocationFuture lf = new LocationFuture();
        lf.locationReceived = false;
        Log.d(TAG, "Location future created");
        return lf;
    }

    public void initLocation(Location location) {
        this.location = location;
        locationReceived = location != null;
    }

    private Location location;
    private boolean locationReceived;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Location get() throws InterruptedException, ExecutionException {
        try {
            return getLocation(null);
        } catch (TimeoutException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Location get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        return getLocation(TimeUnit.MILLISECONDS.convert(timeout, unit));
    }

    private synchronized Location getLocation(Long timeoutMs) throws InterruptedException, TimeoutException, ExecutionException {

        Log.d(TAG, "Trying to get location");

        if (locationReceived) {
            return location;
        }

        if (timeoutMs == null) {
            ((Object) this).wait(0);
        } else if (timeoutMs > 0) {
            ((Object) this).wait(timeoutMs);
        }

        if (!locationReceived) {
            Log.d(TAG, "Locationrequest timeout; throwing...");
            throw new TimeoutException();
        }

        return location;
    }

    public void clearResult() {
        location = null;
        locationReceived = false;
    }

    @Override
    public synchronized void onLocationResult(LocationResult locationResult) {
        if (locationResult == null || locationResult.getLastLocation() == null) {
            return;
        }
        Location lastLocation = locationResult.getLastLocation();
        Log.e(TAG, "Location received! " + lastLocation.getProvider() + " @ " + lastLocation.getTime());
        locationReceived = true;
        location = lastLocation;
        ((Object) this).notifyAll();
    }

    // onLocationAvailability can be overridden if needed, but not strictly necessary for this refactor.
}