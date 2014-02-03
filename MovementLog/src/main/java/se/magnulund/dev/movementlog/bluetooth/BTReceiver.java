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
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        //int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);

        if (bluetoothDevice != null) {

            BTDevice device;

            ContentResolver resolver = mContext.getContentResolver();

            device = BTContract.Devices.getDevice(resolver, bluetoothDevice.getAddress());

            if (device == null) {

                device = new BTDevice(bluetoothDevice);

                Uri uri = BTContract.Devices.addDevice(resolver, device);

                if (uri != null && uri.getPathSegments() != null) {
                    device.setId(Integer.valueOf(uri.getPathSegments().get(1)));
                    NotificationSender.sendBigTextNotification(
                        mContext,
                        "BT device detected",
                        BTUtils.getDeviceMajorClassName(device.getMajorClass()),
                        BTUtils.getDeviceClassName(device.getSubClass())
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
