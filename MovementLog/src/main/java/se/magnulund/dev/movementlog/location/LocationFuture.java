package se.magnulund.dev.movementlog.location;// Created by Gustav on 27/01/2014.

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.RemoteException;

import java.security.ProviderException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LocationFuture implements Future, LocationListener {

    public static LocationFuture newInstance(String fromProvider) {
        LocationFuture l = new LocationFuture();
        l.providerName = fromProvider;
        return l;
    }

    private LocationFuture() {
    }

    private Location location;
    private boolean locationReceived = false;
    private String providerName;
    private Exception mException;

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

        if (locationReceived) {

            return location;

        }

        if (timeoutMs == null) {

            ((Object) this).wait(0);

        } else if (timeoutMs > 0) {

            ((Object) this).wait(timeoutMs);

        }

        if (mException != null) {
            throw new ExecutionException(mException);
        }

        if (!locationReceived) {

            throw new TimeoutException();

        }

        return location;
    }

    @Override
    public synchronized void onLocationChanged(android.location.Location locationResult) {
        if (locationResult != null) {
            locationReceived = true;
            location = locationResult;
            ((Object) this).notifyAll();
        }
    }

    @Override
    public synchronized void onStatusChanged(String provider, int status, Bundle extras) {
        if (provider.equals(providerName) && status == LocationProvider.OUT_OF_SERVICE){
            locationReceived = false;
            ((Object) this).notifyAll();
        }

        mException = new RemoteException("Provider is out of service");
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public synchronized void onProviderDisabled(String provider) {
        if (provider.equals(providerName)){
            locationReceived = false;
            mException = new ProviderException("Provider was disabled");
            ((Object) this).notifyAll();
        }
    }
}