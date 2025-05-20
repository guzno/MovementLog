package se.magnulund.dev.movementlog.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import se.magnulund.dev.movementlog.contracts.BTContract;
import se.magnulund.dev.movementlog.utils.NotificationSender;

public class BTReceiver extends BroadcastReceiver {

    private static final String TAG = "BTReceiver";

    public BTReceiver() {
    }

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        // TODO: For Android 12 (API 31+) and targetSdkVersion 34, BLUETOOTH_CONNECT permission
        // is required in AndroidManifest.xml to receive BluetoothDevice from this intent.
        // Also, runtime permission request for BLUETOOTH_CONNECT might be needed if the app
        // performs Bluetooth operations like fetching device name/address directly,
        // though this receiver primarily reacts to system broadcasts.

        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                Log.d(TAG, "BT DEVICE CONNECTION CHANGE");
                handleConnectionChange(intent);
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                Log.d(TAG, "BT STATE CHANGE");
                handleStateChange(intent);
                break;
            default:
                Log.d(TAG, "unknown intent");
        }
    }

    private void handleConnectionChange(Intent intent) {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
        switch (state) {
            case BluetoothAdapter.STATE_CONNECTING:
                Log.d(TAG, "STATE_CONNECTING");
                break;
            case BluetoothAdapter.STATE_CONNECTED:
                logDeviceConnectionStateChange(intent, state);
                break;
            case BluetoothAdapter.STATE_DISCONNECTING:
                Log.d(TAG, "STATE_DISCONNECTING");
                break;
            case BluetoothAdapter.STATE_DISCONNECTED:
                logDeviceConnectionStateChange(intent, state);
                break;
            default:
                Log.d(TAG, "UNKNOWN STATE");
        }
    }

    private void handleStateChange(Intent intent) {

        int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);

        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
            case BluetoothAdapter.STATE_TURNING_ON:
                //Log.d(TAG, "STATE_TURNING_ON");
                break;
            case BluetoothAdapter.STATE_ON:
                //Log.d(TAG, "STATE_ON");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                //Log.d(TAG, "STATE_TURNING_OFF");
                break;
            case BluetoothAdapter.STATE_OFF:
                //Log.d(TAG, "STATE_OFF");
                break;
            default:
                //Log.d(TAG, "UNKNOWN STATE");
        }
    }

    private void logDeviceConnectionStateChange(Intent intent, int state) {
        // TODO: For Android 12 (API 31+), if BLUETOOTH_CONNECT permission is not granted,
        // getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) may return null or a device
        // with limited information (e.g., no name/address).
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        //int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);

        if (bluetoothDevice != null) {

            BTDevice device;

            ContentResolver resolver = mContext.getContentResolver();

            device = BTContract.Devices.getDevice(resolver, bluetoothDevice.getAddress());

            if (device == null) {
                // Ensure bluetoothDevice is not null and has an address before proceeding
                if (bluetoothDevice == null || bluetoothDevice.getAddress() == null) {
                    Log.w(TAG, "BluetoothDevice is null or has no address, cannot process connection change.");
                    return;
                }

                device = new BTDevice(bluetoothDevice);

                Uri uri = BTContract.Devices.addDevice(resolver, device);

                if (uri != null && uri.getPathSegments() != null) {
                    device.setId(Integer.valueOf(uri.getPathSegments().get(1)));
                    // Potentially check for BLUETOOTH_CONNECT before accessing device name/class for notification
                    String deviceName = "";
                    String deviceClassInfo = "";
                    try {
                         // These might require BLUETOOTH_CONNECT if the device wasn't discovered by this app.
                         // However, since it's from a system broadcast, it might be okay. Test thoroughly.
                        deviceName = device.getName() != null ? device.getName() : "Unknown Device";
                        deviceClassInfo = BTUtils.getDeviceMajorClassName(device.getMajorClass()) + " - " + BTUtils.getDeviceClassName(device.getSubClass());
                    } catch (SecurityException e) {
                        Log.e(TAG, "Bluetooth permission issue when getting device details for notification: " + e.getMessage());
                        deviceName = "Device"; // Fallback
                        deviceClassInfo = "Details unavailable (permission)";
                    }
                    NotificationSender.sendBigTextNotification(
                        mContext,
                        "BT device detected",
                        deviceName, // Use potentially permission-restricted name
                        deviceClassInfo
                    );
                }
            }

            long timestamp = System.currentTimeMillis();

            BTConnectionEvent event;
            switch (state) {
                case BluetoothAdapter.STATE_CONNECTED:
                    event = new BTConnectionEvent(timestamp, device.getId(), BTConnectionEvent.STATE_CONNECTED);
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    event = new BTConnectionEvent(timestamp, device.getId(), BTConnectionEvent.STATE_DISCONNECTED);
                    break;
                default:
                    event = null;
            }

            BTContract.ConnectionEvents.addEvent(resolver, event);
        }
    }
}
