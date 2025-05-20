package se.magnulund.dev.movementlog.utils; // Created by Gustav on 26/01/2014.

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.common.base.Joiner;

import se.magnulund.dev.movementlog.MainActivity;
import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.services.LocationRequestService;
import se.magnulund.dev.movementlog.trips.Trip;

public class NotificationSender {
    private static final String TAG = "NotificationSender";
    public static final String DEFAULT_CHANNEL_ID = "movement_log_channel";

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name); // Create this string resource
            String description = context.getString(R.string.channel_description); // Create this string resource
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void sendNotification(Context context, String title, String text) {
        createNotificationChannel(context); // Ensure channel exists
        PendingIntent pendingIntent = getPendingIntent(context, MainActivity.class, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build()); // Use a consistent notification ID or generate one
    }

    private static PendingIntent getPendingIntent(Context context, Class<?> activityClass, int requestCode) {
        Intent intent = new Intent(context, activityClass);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }

    // Overload for default request code if not specified
    private static PendingIntent getPendingIntent(Context context) {
        return getPendingIntent(context, MainActivity.class, 0);
    }


    // This method can be kept if custom PendingIntents are constructed elsewhere and passed in
    public static void notificationWithCustomIntent(Context context, String title, String text, PendingIntent pendingIntent) {
        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build()); // Use a different ID or manage IDs carefully
    }

    public static void sendCustomNotification(Context context, Notification notification) {
        // This method is tricky as 'notification' is already built.
        // For channel support, it's better to build here or ensure the passed notification is channel-aware.
        // For now, just send it, but it might not work correctly on O+ if channel isn't set by caller.
        createNotificationChannel(context); // Ensure channel exists, though builder needs it
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // ID 0 might conflict with the other sendNotification. Use unique IDs.
        notificationManager.notify(java.util.UUID.randomUUID().hashCode(), notification);
    }

    public static void sendTripStateNotification(Context context, int tripState, Trip trip) {
        createNotificationChannel(context);
        PendingIntent mainIntent = getPendingIntent(context, MainActivity.class, 2); // Unique request code
        PendingIntent mapsIntent;

        String title;
        String text;

        switch (tripState) {
            case LocationRequestService.COMMAND_STORE_START_LOCATION:
                title = "Trip started!";
                text = "@ " + DateTimeUtil.getDateTimeString(trip.getStartTime(), DateTimeUtil.TIME_HOUR_MINUTE);
                mapsIntent = PendingIntent.getActivity(context, 3, mIntentBuilder.getMapsIntent("Trip start", trip.getStartCoords()), getMutabilityFlags(PendingIntent.FLAG_UPDATE_CURRENT));
                break;
            case LocationRequestService.COMMAND_STORE_END_LOCATION:
                title = "Trip ended!";
                text = "@ " + DateTimeUtil.getDateTimeString(trip.getEndTime(), DateTimeUtil.TIME_HOUR_MINUTE);
                mapsIntent = PendingIntent.getActivity(context, 4, mIntentBuilder.getMapsIntent("Trip end", trip.getEndCoords()), getMutabilityFlags(PendingIntent.FLAG_UPDATE_CURRENT));
                break;
            default:
                title = "Trip Update"; // More generic title
                text = "Trip status updated."; // More generic text
                mapsIntent = null;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(mainIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_dialog_info, "Trip info", mainIntent);

        if (mapsIntent != null) {
            builder.addAction(android.R.drawable.ic_dialog_map, "Show on map", mapsIntent);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(trip.getId(), builder.build()); // Use trip ID for unique notification
    }

    private static int getMutabilityFlags(int baseFlags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return baseFlags | PendingIntent.FLAG_IMMUTABLE;
        }
        return baseFlags;
    }


    private static NotificationCompat.Builder getBigTextNotificationBuilder(Context context, String title, String text, String extraContent) {
        createNotificationChannel(context);
        return new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(extraContent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    public static void sendBigTextNotification(Context context, String title, String text, String extraContent) {
        NotificationCompat.Builder builder = getBigTextNotificationBuilder(context, title, text, extraContent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(java.util.UUID.randomUUID().hashCode(), builder.build()); // Unique ID
    }

    public static void sendBigTextNotification(Context context, String title, String text, String[] extraContent) {
        Joiner joiner = Joiner.on("\n").skipNulls();
        String bigText = joiner.join(extraContent);
        sendBigTextNotification(context, title, text, bigText);
    }
}
