package se.magnulund.dev.movementlog.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = "BluetoothReceiver";

    public BluetoothReceiver() {
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

        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);

        if (bluetoothDevice != null) {

            Log.d(TAG, "N: "+bluetoothDevice.getName()+" @ "+bluetoothDevice.getAddress());

            switch (intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)) {
                case BluetoothAdapter.STATE_CONNECTING:
                    Log.d(TAG, "STATE_CONNECTING");
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    Log.d(TAG, "STATE_CONNECTED");
                    break;
                case BluetoothAdapter.STATE_DISCONNECTING:
                    Log.d(TAG, "STATE_DISCONNECTING");
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    Log.d(TAG, "STATE_DISCONNECTED");
                    break;
                default:
                    Log.d(TAG, "UNKNOWN STATE");
            }
        }
    }

    private void handleStateChange(Intent intent) {

        int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);

        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.d(TAG, "STATE_TURNING_ON");
                break;
            case BluetoothAdapter.STATE_ON:
                Log.d(TAG, "STATE_ON");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.d(TAG, "STATE_TURNING_OFF");
                break;
            case BluetoothAdapter.STATE_OFF:
                Log.d(TAG, "STATE_OFF");
                break;
            default:
                Log.d(TAG, "UNKNOWN STATE");
        }
    }
}
