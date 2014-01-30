package se.magnulund.dev.movementlog.test;// Created by Gustav on 28/01/2014.

import se.magnulund.dev.movementlog.services.LocationRequestService;

public class LocationRequestServiceWrapper extends LocationRequestService {

    private static final String TAG = "LocationRequestServiceWrapper";

    /*    private CountDownLatch latch;

        @Override
        protected void onHandleIntent(Intent intent) {
            super.onHandleIntent(intent);
            latch.countDown();
        }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }
        */
}
