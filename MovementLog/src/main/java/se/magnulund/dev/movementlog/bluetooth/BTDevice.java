package se.magnulund.dev.movementlog.bluetooth;// Created by Gustav on 02/02/2014.

import android.bluetooth.BluetoothClass;
import android.database.Cursor;

import se.magnulund.dev.movementlog.contracts.BTContract;

public class BTDevice {
    private static final String TAG = "BTDevice";

    String address;
    String name;
    int majorClass;
    int subClass;
    int _id;

    public BTDevice(String address) {
        this.address = address;
    }

    public BTDevice(android.bluetooth.BluetoothDevice device) {
        this.address = device.getAddress();
        this.name = device.getName();
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass != null) {
            this.majorClass = bluetoothClass.getMajorDeviceClass();
            this.subClass = bluetoothClass.getDeviceClass();
        } else {
            this.majorClass = -1;
            this.subClass = -1;
        }
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMajorClass() {
        return majorClass;
    }

    public void setMajorClass(int majorClass) {
        this.majorClass = majorClass;
    }

    public int getSubClass() {
        return subClass;
    }

    public void setSubClass(int subClass) {
        this.subClass = subClass;
    }

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public static BTDevice fromCursor(Cursor c) {

        String address = c.getString(c.getColumnIndexOrThrow(BTContract.Devices.Columns.ADDRESS));

        BTDevice bluetoothDevice = new BTDevice(address);

        bluetoothDevice.setId(c.getInt(c.getColumnIndexOrThrow(BTContract.Devices.Columns._ID)));

        bluetoothDevice.setName(c.getString(c.getColumnIndexOrThrow(BTContract.Devices.Columns.NAME)));

        bluetoothDevice.setMajorClass(c.getInt(c.getColumnIndexOrThrow(BTContract.Devices.Columns.MAJOR_CLASS)));

        bluetoothDevice.setSubClass(c.getInt(c.getColumnIndexOrThrow(BTContract.Devices.Columns.SUBCLASS)));

        return bluetoothDevice;
    }
}
