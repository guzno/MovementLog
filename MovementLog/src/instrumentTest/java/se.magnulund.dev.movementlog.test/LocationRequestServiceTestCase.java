package se.magnulund.dev.movementlog.test;// Created by Gustav on 28/01/2014.

import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;

import se.magnulund.dev.movementlog.services.LocationRequestService;

public class LocationRequestServiceTestCase extends ServiceTestCase<LocationRequestService> {
    private static final String TAG = "TripRecognitionIntentServiceTestCase";
    private static final String TESTING = "testing";
    private Context context;
    //private CountDownLatch latch;

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

        //latch = new CountDownLatch(1);
        //getService().setLatch(latch);
    }

    public synchronized void testLocationRequest(){
        Intent intent = new Intent(context, LocationRequestService.class);

        intent.putExtra(TESTING, true);
        intent.putExtra(LocationRequestService.COMMAND, LocationRequestService.COMMAND_STORE_START_LOCATION);
        intent.putExtra(LocationRequestService.EXTRA_TRIP_ID, 0);

        startService(intent);

        while(getService().requestHandled == false) {

        }

        try {
            wait(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startService(intent);

        while(getService().requestHandled == false) {

        }

        try {
            wait(21000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startService(intent);

        while(getService().requestHandled == false) {

        }

        try {
            wait(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startService(intent);

        while(getService().requestHandled == false) {

        }

/*

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }
}
