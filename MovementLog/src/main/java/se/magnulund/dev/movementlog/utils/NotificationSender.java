package se.magnulund.dev.movementlog.utils;// Created by Gustav on 26/01/2014.

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import se.magnulund.dev.movementlog.MainActivity;
import se.magnulund.dev.movementlog.R;

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
}
