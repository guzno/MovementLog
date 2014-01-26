package se.magnulund.dev.movementlog.utils;// Created by Gustav on 26/01/2014.

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import se.magnulund.dev.movementlog.MainActivity;
import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.triprecognition.TripRecognitionIntentService;
import se.magnulund.dev.movementlog.trips.Trip;

public class NotificationSender {
    private static final String TAG = "NotificationSender";

    public static void sendNotification(Context context, String title, String text) {
        PendingIntent pendingIntent = getPendingIntent(context);

        Notification n = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true).build();

        sendNotification(context, n);
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private static void sendNotification(Context context, Notification notification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    public static void notificationWithCustomIntent(Context context, String title, String text, PendingIntent pendingIntent) {

        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true).build();

        sendNotification(context, notification);
    }

    public static void sendCustomNotification(Context context, Notification notification) {
        sendNotification(context, notification);
    }

    public static void sendTripStateNotification(Context context, int tripState, Trip trip){
        PendingIntent mainIntent = getPendingIntent(context);
        PendingIntent mapsIntent;

        String title;
        String text;

        switch (tripState) {
            case TripRecognitionIntentService.START_LOCATION:
                title = "Trip started!";
                text = "@ "+DateTimeUtil.getDateTimeString(trip.getStartTime(), DateTimeUtil.TIME_HOUR_MINUTE);
                mapsIntent = PendingIntent.getActivity(context, 0, mIntentBuilder.getMapsIntent("Trip start", trip.getStartLocation()), 0);
                break;
            case TripRecognitionIntentService.END_LOCATION:
                title = "Trip ended!";
                text = "@ "+DateTimeUtil.getDateTimeString(trip.getEndTime(), DateTimeUtil.TIME_HOUR_MINUTE);
                mapsIntent = PendingIntent.getActivity(context, 0, mIntentBuilder.getMapsIntent("Trip start", trip.getEndLocation()), 0);
                break;
            default:
                title = "not good trip!";
                text = ":(";
                mapsIntent = null;
        }


        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(mainIntent)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_dialog_info, "Trip info", mainIntent)
                .addAction(android.R.drawable.ic_dialog_map, "Show on map", mapsIntent).build();

        sendNotification(context, notification);
    }
}
