package se.magnulund.dev.movementlog.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat; // For NotificationCompat.Builder

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
// import com.google.android.gms.common.GooglePlayServicesClient; // Not needed
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

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

    // private boolean mInProgress; // No longer needed with Task API
    // private REQUEST_TYPE mRequestType; // No longer needed

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

    // public enum REQUEST_TYPE {START, STOP} // No longer needed

    public ActivityRecognitionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // mInProgress = false; // Not needed

        // mActivityRecognitionClient = new ActivityRecognitionClient(this, googlePlayServicesClientConnectionCallbacks, googlePlayFailer); // Old constructor
        mActivityRecognitionClient = new ActivityRecognitionClient(this);

        Intent intent = new Intent(this, TripRecognitionIntentService.class);

        // Ensure FLAG_IMMUTABLE or FLAG_MUTABLE is set for PendingIntent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        mActivityRecognitionPendingIntent = PendingIntent.getService(this, 0, intent, flags);
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
        if (!servicesConnected()) {
            stopSelf(); // Stop service if Play Services not available
            return;
        }

        // TODO: Add runtime permission check for ACTIVITY_RECOGNITION (API 29+)
        // For now, assuming permission is granted for compilation.
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        //     ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
        //     Log.e(TAG, "ACTIVITY_RECOGNITION permission not granted.");
        //     sendErrorNotificationMessage("Permission missing for Activity Recognition.");
        //     return;
        // }


        mActivityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, mActivityRecognitionPendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendNotification("ActivityRecognitionService", "Registered for activity updates");
                        Log.i(TAG, "Successfully requested activity updates.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        sendNotification("ActivityRecognitionService", "Failed to register for activity updates.");
                        Log.e(TAG, "Failed to request activity updates.", e);
                    }
                });
    }

    /**
     * Turn off activity recognition updates
     */
    public void stopUpdates() {
        if (!servicesConnected()) {
            stopSelf(); // Stop service if Play Services not available
            return;
        }

        mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendNotification("ActivityRecognitionService", "Unregistered for activity updates");
                        Log.i(TAG, "Successfully removed activity updates.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        sendNotification("ActivityRecognitionService", "Failed to unregister for activity updates.");
                        Log.e(TAG, "Failed to remove activity updates.", e);
                    }
                });
    }

    private boolean servicesConnected() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            sendErrorNotification(resultCode); // This method now uses GoogleApiAvailability
            return false;
        }
    }

    // GooglePlayServicesClientConnectionCallbacks and GooglePlayFailer are no longer needed.

    private void sendNotification(String title, String text) {
        // TODO: Ensure NotificationSender uses Notification Channels for Android O+
        NotificationSender.sendNotification(this, title, text);
    }

    private void sendErrorNotificationMessage(String message) {
        // TODO: Ensure NotificationSender uses Notification Channels for Android O+
        NotificationSender.sendNotification(this, "Activity Recognition Error", message);
    }

    private void sendErrorNotification(int resultCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        PendingIntent pendingIntent = apiAvailability.getErrorResolutionPendingIntent(this, resultCode, CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // Use NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "error_channel_activity") // Replace with actual channel ID
                .setContentTitle("Activity Recognition Error")
                .setContentText("Google Play services issue (errorcode:" + resultCode + ")")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent); // Set content intent only if resolution is possible
            builder.addAction(android.R.drawable.ic_menu_manage, "Fix Now", pendingIntent);
        }
        
        // TODO: Create Notification Channel for Android O+
        // NotificationSender.sendCustomNotification(this, builder.build());
        // Assuming NotificationSender handles channels or direct send for now:
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("error_channel_activity", "Error Channel Activity", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(2, builder.build()); // Use a unique ID for this notification
    }
}
