package se.magnulund.dev.movementlog.test;// Created by Gustav on 28/01/2014.

import android.content.Intent;

import android.content.Intent;
import android.os.Binder; // Import Binder
import android.os.IBinder; // Import IBinder

import java.util.concurrent.CountDownLatch;

import se.magnulund.dev.movementlog.services.TripRecognitionIntentService;

public class TripReccognitionIntentServiceWrapper extends TripRecognitionIntentService {
    private static final String TAG = "TripReccognitionIntentServiceWrapper";

    private CountDownLatch latch;

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        latch.countDown();
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }
}
