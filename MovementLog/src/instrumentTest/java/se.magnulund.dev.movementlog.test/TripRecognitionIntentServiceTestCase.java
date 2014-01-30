package se.magnulund.dev.movementlog.test;// Created by Gustav on 28/01/2014.

import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import se.magnulund.dev.movementlog.contracts.RawDataContract;
import se.magnulund.dev.movementlog.services.TripRecognitionIntentService;

public class TripRecognitionIntentServiceTestCase extends ServiceTestCase<TripReccognitionIntentServiceWrapper> {
    private static final String TAG = "TripRecognitionIntentServiceTestCase";
    private static final String TESTING = "testing";
    private Context context;
    private CountDownLatch latch;

    /**
     * Constructor
     */
    public TripRecognitionIntentServiceTestCase() {
        super(TripReccognitionIntentServiceWrapper.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getContext();
    }

    @Override
    protected void setupService() {
        super.setupService();

        latch = new CountDownLatch(1);
        getService().setLatch(latch);
    }

    public void test_tris_HandleUndefinedIntent() {


        Intent intent = new Intent(context, TripRecognitionIntentService.class);
        intent.putExtra("WRONG_EXTRA", 1);

        startService(intent);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void test_tris_HandleResultAndStore() {

        Intent intent = new Intent(context, TripRecognitionIntentService.class);

        ActivityRecognitionResult r = getActivityRecognitionResult(RESULT_STILL);

        intent.putExtra(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT, r);
        intent.putExtra(TESTING, true);

        startService(intent);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final int RESULT_IN_VEHICLE = 0;
    private static final int RESULT_BIKING = 1;
    private static final int RESULT_STILL = 2;
    private static final int RESULT_UNKNOWN = 3;
    private static final int RESULT_TILTING = 4;
    private static final int RESULT_ONFOOT = 5;
    private static final int RESULT_POSSIBLE_IN_VEHICLE = 6;
    private static final int RESULT_POSSIBLE_BIKING = 7;

    private ActivityRecognitionResult getActivityRecognitionResult(int resultType) {

        ArrayList<DetectedActivity> arrayList = new ArrayList<>();

        DetectedActivity d;

        switch (resultType) {
            case RESULT_IN_VEHICLE:
                d = new DetectedActivity(DetectedActivity.IN_VEHICLE, 90);
                arrayList.add(d);
                break;
            case RESULT_BIKING:
                d = new DetectedActivity(DetectedActivity.ON_BICYCLE, 90);
                arrayList.add(d);
                break;
            case RESULT_STILL:
                d = new DetectedActivity(DetectedActivity.STILL, 90);
                arrayList.add(d);
                break;
            case RESULT_UNKNOWN:
                d = new DetectedActivity(DetectedActivity.UNKNOWN, 90);
                arrayList.add(d);
                break;
            case RESULT_TILTING:
                d = new DetectedActivity(DetectedActivity.TILTING, 90);
                arrayList.add(d);
                break;
            case RESULT_ONFOOT:
                d = new DetectedActivity(DetectedActivity.ON_FOOT, 90);
                arrayList.add(d);
                break;
            case RESULT_POSSIBLE_IN_VEHICLE:
                d = new DetectedActivity(DetectedActivity.UNKNOWN, 48);
                arrayList.add(d);
                d = new DetectedActivity(DetectedActivity.IN_VEHICLE, 42);
                arrayList.add(d);
                break;
            case RESULT_POSSIBLE_BIKING:
                d = new DetectedActivity(DetectedActivity.UNKNOWN, 48);
                arrayList.add(d);
                d = new DetectedActivity(DetectedActivity.ON_BICYCLE, 42);
                arrayList.add(d);
                break;
            default:
                assertTrue("Invalid result type in test...", false);
        }

        if (resultType == RESULT_STILL) {
            d = new DetectedActivity(DetectedActivity.UNKNOWN, 6);
            arrayList.add(d);
            d = new DetectedActivity(DetectedActivity.TILTING, 4);
            arrayList.add(d);
        } else if (resultType == RESULT_TILTING) {
            d = new DetectedActivity(DetectedActivity.UNKNOWN, 6);
            arrayList.add(d);
            d = new DetectedActivity(DetectedActivity.STILL, 4);
            arrayList.add(d);
        } else {
            d = new DetectedActivity(DetectedActivity.TILTING, 6);
            arrayList.add(d);
            d = new DetectedActivity(DetectedActivity.STILL, 4);
            arrayList.add(d);
        }

        return new ActivityRecognitionResult(arrayList, System.currentTimeMillis(), System.nanoTime());
    }
}
