package se.magnulund.dev.movementlog.utils;// Created by Gustav on 26/01/2014.

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.common.base.Joiner;

import se.magnulund.dev.movementlog.MainActivity;
import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.services.LocationRequestService;
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
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
            case LocationRequestService.COMMAND_STORE_START_LOCATION:
                title = "Trip started!";
                text = "@ "+DateTimeUtil.getDateTimeString(trip.getStartTime(), DateTimeUtil.TIME_HOUR_MINUTE);
                mapsIntent = PendingIntent.getActivity(context, 0, mIntentBuilder.getMapsIntent("Trip start", trip.getStartCoords()), PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case LocationRequestService.COMMAND_STORE_END_LOCATION:
                title = "Trip ended!";
                text = "@ "+DateTimeUtil.getDateTimeString(trip.getEndTime(), DateTimeUtil.TIME_HOUR_MINUTE);
                mapsIntent = PendingIntent.getActivity(context, 0, mIntentBuilder.getMapsIntent("Trip end", trip.getEndCoords()), PendingIntent.FLAG_UPDATE_CURRENT);
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

    private static Notification getBigTextNotification(Context context, String title, String text, String extraContent){
        return new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setStyle(new Notification.BigTextStyle().bigText(extraContent))
                .build();
    }

    public static void sendBigTextNotification(Context context, String title, String text, String extraContent){
        sendNotification(context, getBigTextNotification(context, title, text, extraContent));
    }

    public static void sendBigTextNotification(Context context, String title, String text, String[] extraContent){

        Joiner joiner = Joiner.on("\n").skipNulls();

        String bigText = joiner.join(extraContent);

        sendNotification(context, getBigTextNotification(context, title, text, bigText));
    }
}
