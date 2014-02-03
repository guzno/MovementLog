package se.magnulund.dev.movementlog.bluetooth;// Created by Gustav on 02/02/2014.

import android.bluetooth.BluetoothClass;

public class BTUtils {
    private static final String TAG = "BTUtils";


    public static String getDeviceMajorClassName(int deviceMajorClass) {
        String className;
        switch (deviceMajorClass) {
            case BluetoothClass.Device.Major.MISC:
                className = "MISC";
                break;
            case BluetoothClass.Device.Major.COMPUTER:
                className = "COMPUTER";
                break;
            case BluetoothClass.Device.Major.PHONE:
                className = "PHONE";
                break;
            case BluetoothClass.Device.Major.NETWORKING:
                className = "NETWORKING";
                break;
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                className = "AUDIO_VIDEO";
                break;
            case BluetoothClass.Device.Major.PERIPHERAL:
                className = "PERIPHERAL";
                break;
            case BluetoothClass.Device.Major.IMAGING:
                className = "IMAGING";
                break;
            case BluetoothClass.Device.Major.WEARABLE:
                className = "WEARABLE";
                break;
            case BluetoothClass.Device.Major.TOY:
                className = "TOY";
                break;
            case BluetoothClass.Device.Major.HEALTH:
                className = "HEALTH";
                break;
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                className = "UNCATEGORIZED";
                break;
            default:
                className = "UNKNOWN CLASS";
        }
        return className;
    }

    public static String getDeviceClassName(int deviceClass) {
        String className = "";
        switch (deviceClass) {
            // Devices in the COMPUTER major class
            case BluetoothClass.Device.COMPUTER_UNCATEGORIZED:
                className ="COMPUTER_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.COMPUTER_DESKTOP:
                className ="COMPUTER_DESKTOP";
                break;
            case BluetoothClass.Device.COMPUTER_SERVER:
                className ="COMPUTER_SERVER";
                break;
            case BluetoothClass.Device.COMPUTER_LAPTOP:
                className ="COMPUTER_LAPTOP";
                break;
            case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA:
                className ="COMPUTER_HANDHELD_PC_PDA";
                break;
            case BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA:
                className ="COMPUTER_PALM_SIZE_PC_PDA";
                break;
            case BluetoothClass.Device.COMPUTER_WEARABLE:
                className ="COMPUTER_WEARABLE";
                break;

            // Devices in the PHONE major class
            case BluetoothClass.Device.PHONE_UNCATEGORIZED:
                className ="PHONE_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.PHONE_CELLULAR:
                className ="PHONE_CELLULAR";
                break;
            case BluetoothClass.Device.PHONE_CORDLESS:
                className ="PHONE_CORDLESS";
                break;
            case BluetoothClass.Device.PHONE_SMART:
                className ="PHONE_SMART";
                break;
            case BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY:
                className ="PHONE_MODEM_OR_GATEWAY";
                break;
            case BluetoothClass.Device.PHONE_ISDN:
                className ="PHONE_ISDN";
                break;

            // Minor classes for the AUDIO_VIDEO major class
            case BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED:
                className ="AUDIO_VIDEO_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                className ="AUDIO_VIDEO_WEARABLE_HEADSET";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                className ="AUDIO_VIDEO_HANDSFREE";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE:
                className ="AUDIO_VIDEO_MICROPHONE";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                className ="AUDIO_VIDEO_LOUDSPEAKER";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
                className ="AUDIO_VIDEO_HEADPHONES";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO:
                className ="AUDIO_VIDEO_PORTABLE_AUDIO";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                className ="AUDIO_VIDEO_CAR_AUDIO";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX:
                className ="AUDIO_VIDEO_SET_TOP_BOX";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                className ="AUDIO_VIDEO_HIFI_AUDIO";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VCR:
                className ="AUDIO_VIDEO_VCR";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA:
                className ="AUDIO_VIDEO_VIDEO_CAMERA";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER:
                className ="AUDIO_VIDEO_CAMCORDER";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR:
                className ="AUDIO_VIDEO_VIDEO_MONITOR";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER:
                className ="AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING:
                className ="AUDIO_VIDEO_VIDEO_CONFERENCING";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_GAMING_TOY:
                className ="AUDIO_VIDEO_VIDEO_GAMING_TOY";
                break;
            case BluetoothClass.Device.WEARABLE_UNCATEGORIZED:
                className ="WEARABLE_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.WEARABLE_WRIST_WATCH:
                className ="WEARABLE_WRIST_WATCH";
                break;
            case BluetoothClass.Device.WEARABLE_PAGER:
                className ="WEARABLE_PAGER";
                break;
            case BluetoothClass.Device.WEARABLE_JACKET:
                className ="WEARABLE_JACKET";
                break;
            case BluetoothClass.Device.WEARABLE_HELMET:
                className ="WEARABLE_HELMET";
                break;
            case BluetoothClass.Device.WEARABLE_GLASSES:
                className ="WEARABLE_GLASSES";
                break;
            case BluetoothClass.Device.TOY_UNCATEGORIZED:
                className ="TOY_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.TOY_ROBOT:
                className ="TOY_ROBOT";
                break;
            case BluetoothClass.Device.TOY_VEHICLE:
                className ="TOY_VEHICLE";
                break;
            case BluetoothClass.Device.TOY_DOLL_ACTION_FIGURE:
                className ="TOY_DOLL_ACTION_FIGURE";
                break;
            case BluetoothClass.Device.TOY_CONTROLLER:
                className ="TOY_CONTROLLER";
                break;
            case BluetoothClass.Device.TOY_GAME:
                className ="TOY_GAME";
                break;
            case BluetoothClass.Device.HEALTH_UNCATEGORIZED:
                className ="HEALTH_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.HEALTH_BLOOD_PRESSURE:
                className ="HEALTH_BLOOD_PRESSURE";
                break;
            case BluetoothClass.Device.HEALTH_THERMOMETER:
                className ="HEALTH_THERMOMETER";
                break;
            case BluetoothClass.Device.HEALTH_WEIGHING:
                className ="HEALTH_WEIGHING";
                break;
            case BluetoothClass.Device.HEALTH_GLUCOSE:
                className ="HEALTH_GLUCOSE";
                break;
            case BluetoothClass.Device.HEALTH_PULSE_OXIMETER:
                className ="HEALTH_PULSE_OXIMETER";
                break;
            case BluetoothClass.Device.HEALTH_PULSE_RATE:
                className ="HEALTH_PULSE_RATE";
                break;
            case BluetoothClass.Device.HEALTH_DATA_DISPLAY:
                className ="HEALTH_DATA_DISPLAY";
                break;
            default:
                className = "UNKNOWN SUBCLASS";
        }
        return className;
    }
}
