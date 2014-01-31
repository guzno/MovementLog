package se.magnulund.dev.movementlog.location;// Created by Gustav on 27/01/2014.

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import se.magnulund.dev.movementlog.utils.DateTimeUtil;

public class LocationFuture implements Future<Location>, LocationListener {

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
    public synchronized void onLocationChanged(Location locationResult) {
        Log.e(TAG, "Location received! "+locationResult.getProvider()+" @ "+ locationResult.getTime());
        if (locationResult != null) {
            locationReceived = true;
            location = locationResult;
            ((Object) this).notifyAll();
        }
    }
}