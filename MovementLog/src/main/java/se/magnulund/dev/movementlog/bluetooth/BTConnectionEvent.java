package se.magnulund.dev.movementlog.bluetooth;// Created by Gustav on 03/02/2014.

import android.database.Cursor;

import se.magnulund.dev.movementlog.contracts.BTContract;

public class BTConnectionEvent {
    private static final String TAG = "BTConnectionEvent";

    public static final int STATE_CONNECTED = 1;
    public static final int STATE_DISCONNECTED = 2;

    private long timestamp;
    private int deviceId;
    private int connectionState;

    public BTConnectionEvent(long timestamp, int deviceId, int connectionState) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.connectionState = connectionState;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getConnectionState() {
        return connectionState;
    }

    public static BTConnectionEvent fromCursor(Cursor c) {
        final long timestamp = c.getLong(c.getColumnIndexOrThrow(BTContract.ConnectionEvents.Columns.TIMESTAMP));
        final int deviceId = c.getInt(c.getColumnIndexOrThrow(BTContract.ConnectionEvents.Columns.DEVICE_ID));
        final int connectionState = c.getInt(c.getColumnIndexOrThrow(BTContract.ConnectionEvents.Columns.CONNECTION_STATE));

        return new BTConnectionEvent(timestamp, deviceId, connectionState);
    }
}
