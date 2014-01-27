package se.magnulund.dev.movementlog.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.utils.DateTimeUtil;
import se.magnulund.dev.movementlog.utils.NotificationSender;

public class ActivityRecognitionService extends Service {

    // Constants that define the activity detection interval
    public static final int DETECTION_INTERVAL_SECONDS = 10;
    public static final int DETECTION_INTERVAL_MILLISECONDS = DateTimeUtil.MILLIS_PER_SECOND * DETECTION_INTERVAL_SECONDS;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final String COMMAND = "COMMAND";
    private static final int COMMAND_START = 1;
    private static final int COMMAND_STOP = 2;
    private static final String TAG = ActivityRecognitionService.class.getSimpleName();

    /*
     * Store the PendingIntent used to send activity recognition events back to the app
     */
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;

    private boolean mInProgress;
    private REQUEST_TYPE mRequestType;

    public static Intent getStartIntent(Context context) {
        Intent startIntent = new Intent(context, ActivityRecognitionService.class);
        startIntent.putExtra(COMMAND, COMMAND_START);
        return startIntent;
    }

    public static Intent getStopIntent(Context context) {
        Intent stopIntent = new Intent(context, ActivityRecognitionService.class);
        stopIntent.putExtra(COMMAND, COMMAND_STOP);
        return stopIntent;
    }

    public enum REQUEST_TYPE {START, STOP}

    public ActivityRecognitionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInProgress = false;

        mActivityRecognitionClient = new ActivityRecognitionClient(this, googlePlayServicesClientConnectionCallbacks, googlePlayFailer);

        Intent intent = new Intent(this, TripRecognitionIntentService.class);
/*
        Bundle bundle = new Bundle();

        bundle.putInt(TripRecognitionIntentService.RESULT_TYPE, TripRecognitionIntentService.RESULT_TYPE_ACTIVITY_RECOGNITION);

        intent.putExtras(bundle);
*/
        mActivityRecognitionPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(COMMAND, 0);
        switch (command) {
            case COMMAND_START:
                Log.e(TAG, "I can start for u *<|:)");
                startUpdates();
                break;
            case COMMAND_STOP:
                Log.e(TAG, "I can stop for u *<|:(");
                stopUpdates();
                break;
            default:
                Log.e(TAG, "I don't know this command. :(");
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void startUpdates() {
        // Set the request type to START
        mRequestType = REQUEST_TYPE.START;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request type
         * can be restarted.
         */
        if (!servicesConnected()) {
            return;
        }

        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            //
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
            mActivityRecognitionClient.disconnect();
            mInProgress = false;
            startUpdates();
        }
    }

    /**
     * Turn off activity recognition updates
     */
    public void stopUpdates() {
        // Set the request type to STOP
        mRequestType = REQUEST_TYPE.STOP;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected()) {
            return;
        }

        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            //
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
            mActivityRecognitionClient.disconnect();
            mInProgress = false;
            stopUpdates();
        }
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

    private GooglePlayServicesClientConnectionCallbacks googlePlayServicesClientConnectionCallbacks = new GooglePlayServicesClientConnectionCallbacks();

    private class GooglePlayServicesClientConnectionCallbacks implements GooglePlayServicesClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle bundle) {
            switch (mRequestType) {
                case START:
                /*
                 * Request activity recognition updates using the
                 * preset detection interval and PendingIntent.
                 * This call is synchronous.
                 */
                    mActivityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, mActivityRecognitionPendingIntent);
                    sendNotification("ActivityRecognitionService", "Registered for activity updates");
                    break;

                case STOP:
                    mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
                    sendNotification("ActivityRecognitionService", "Unregistered for activity updates");
                    break;

                /*
                 * An enum was added to the definition of REQUEST_TYPE,
                 * but it doesn't match a known case. Throw an exception.
                 */
                default:
                    throw new RuntimeException("Unknown request type in onConnected().");
            }
            mInProgress = false;
            mActivityRecognitionClient.disconnect();

        }

        @Override
        public void onDisconnected() {


        }
    }

    private GooglePlayFailer googlePlayFailer = new GooglePlayFailer();

    private class GooglePlayFailer implements GooglePlayServicesClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            mInProgress = false;

            sendErrorNotification(connectionResult.getErrorCode());
        }
    }

    private void sendNotification(String title, String text) {
        NotificationSender.sendNotification(this, title, text);
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

}
