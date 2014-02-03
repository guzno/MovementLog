package se.magnulund.dev.movementlog.contracts;// Created by Gustav on 22/01/2014.

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import se.magnulund.dev.movementlog.bluetooth.BTConnectionEvent;
import se.magnulund.dev.movementlog.bluetooth.BTDevice;
import se.magnulund.dev.movementlog.providers.BTDataProvider;

public class BTContract {
    private static final String TAG = "BTContract";

    /**
     * Authority string for this provider.
     */
    public static final String AUTHORITY = BTDataProvider.AUTHORITY;

    /**
     * Bluetooth devices
     */

    public static class Devices {

        public static final String URI_PART_ALL_CONTENT = "bt_devices";
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART_ALL_CONTENT);

        public static final String URI_PART_SINGLE_ITEM = URI_PART_ALL_CONTENT + "/#";
        /**
         * The MIME type of {@link #CONTENT_URI} providing the for all bluetooth devices.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.magnulund.bt_devices";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single bluetooth device.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.magnulund.bt_devices";
        public static final String[] DEFAULT_PROJECTION = {
                Columns._ID,
                Columns.ADDRESS,
                Columns.NAME,
                Columns.MAJOR_CLASS,
                Columns.SUBCLASS
        };
        public static final String DEFAULT_SORT_ORDER = Columns._ID + " DESC";

        public static BTDevice getDevice(ContentResolver resolver, String deviceAddress){

            String selection = Columns.ADDRESS +" = ?";

            String[] selectionArgs = {deviceAddress};

            Cursor c = resolver.query(CONTENT_URI, DEFAULT_PROJECTION, selection, selectionArgs, DEFAULT_SORT_ORDER);

            if (c != null && c.getCount() > 0) {

                c.moveToFirst();

                try {
                    BTDevice device = BTDevice.fromCursor(c);
                    c.close();
                    return device;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                if (c!=null) {c.close();}
                return null;
            }
        }

        public static BTDevice getDevice(ContentResolver resolver, Uri uri){
            Cursor c;
            if (uri != null) {
                c = resolver.query(uri, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
            } else {
                return null;
            }

            if (c != null && c.getCount() > 0) {

                c.moveToFirst();

                try {
                    BTDevice device = BTDevice.fromCursor(c);
                    c.close();
                    return device;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                if (c!=null) {c.close();}
                return null;
            }
        }

        public static Uri addDevice(ContentResolver resolver, BTDevice device) {
            ContentValues values = getDeviceContentValues(device);

            return resolver.insert(CONTENT_URI, values);
        }

        private static ContentValues getDeviceContentValues(BTDevice device) {

            ContentValues values = new ContentValues(Columns.CONTENT_VALUE_COLUMN_COUNT);

            values.put(Columns.ADDRESS, device.getAddress());
            values.put(Columns.NAME, device.getName());
            values.put(Columns.MAJOR_CLASS, device.getMajorClass());
            values.put(Columns.SUBCLASS, device.getSubClass());

            return values;
        }

        public static class Columns implements BaseColumns {

            public static final String _ID = BaseColumns._ID;

            public static final int CONTENT_VALUE_COLUMN_COUNT = 5;

            /**
             * The ADDRESS column. The address of the device as a MAC-address type string (e.g. 00:11:22:AA:BB:CC).
             * <p>TYPE: STRING</p>
             */
            public static final String ADDRESS = "address";

            /**
             * The NAME column. The name of this device.
             * <p>TYPE: STRING</p>
             */
            public static final String NAME = "name";

            /**
             * The MAJOR_CLASS column. The major class of this device.
             * <p>TYPE: INTEGER</p>
             */
            public static final String MAJOR_CLASS = "major_class";

            /**
             * The SUBCLASS column. The class of this device.
             * <p>TYPE: INTEGER</p>
             */
            public static final String SUBCLASS = "subclass";
        }
    }

    /**
     * Bluetooth device connection state change events
     */

    public static class ConnectionEvents {

        public static final String URI_PART_ALL_CONTENT = "bt_connection_events";
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART_ALL_CONTENT);

        public static final String URI_PART_SINGLE_ITEM = URI_PART_ALL_CONTENT + "/#";
        /**
         * The MIME type of {@link #CONTENT_URI} providing the for all bluetooth connection events.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.magnulund.bt_connection_events";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single bluetooth connection event.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.magnulund.bt_connection_events";
        public static final String[] DEFAULT_PROJECTION = {
                Columns._ID,
                Columns.TIMESTAMP,
                Columns.DEVICE_ID,
                Columns.CONNECTION_STATE
        };
        public static final String DEFAULT_SORT_ORDER = Columns.TIMESTAMP + " DESC";

        public static BTConnectionEvent getEvent(ContentResolver resolver, Uri uri){
            Cursor c;
            if (uri != null) {
                c = resolver.query(uri, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
            } else {
                return null;
            }

            if (c != null && c.getCount() > 0) {

                c.moveToFirst();

                try {
                    BTConnectionEvent event = BTConnectionEvent.fromCursor(c);
                    c.close();
                    return event;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                if (c!=null) {c.close();}
                return null;
            }
        }

        public static Cursor getConnectionEventsForDevice(ContentResolver resolver, BTDevice device, Integer connectionType){

            String selection = Columns.DEVICE_ID + "= ?";

            String[] selectionArgs;

            if (connectionType != null) {
                selection += " AND " + Columns.CONNECTION_STATE + " = ?";
                selectionArgs = new String[]{Integer.toString(device.getId()), Integer.toString(connectionType)};
            } else {
                selectionArgs = new String[]{Integer.toString(device.getId())};
            }

            return resolver.query(CONTENT_URI, DEFAULT_PROJECTION, selection, selectionArgs, DEFAULT_SORT_ORDER);
        }

        public static Uri addEvent(ContentResolver resolver, BTConnectionEvent event) {

            ContentValues values = getEventContentValues(event);

            return resolver.insert(CONTENT_URI, values);
        }

        private static ContentValues getEventContentValues(BTConnectionEvent event) {

            ContentValues values = new ContentValues(Columns.CONTENT_VALUE_COLUMN_COUNT);

            values.put(Columns.TIMESTAMP, event.getTimestamp());
            values.put(Columns.DEVICE_ID, event.getDeviceId());
            values.put(Columns.CONNECTION_STATE, event.getConnectionState());

            return values;
        }

        public static class Columns implements BaseColumns {

            public static final String _ID = BaseColumns._ID;

            public static final int CONTENT_VALUE_COLUMN_COUNT = 4;

            /**
             * The TIMESTAMP column. The timestamp of this connection event.
             * <p>TYPE: INTEGER</p>
             */
            public static final String TIMESTAMP = "timestamp";

            /**
             * The DEVICE_ID column. The id of the device for which the connection state changed.
             * <p>TYPE: INTEGER</p>
             */
            public static final String DEVICE_ID = "device_id";

            /**
             * The CONNECTION_STATE column. The state the connection changed to.
             * <p>TYPE: INTEGER</p>
             */
            public static final String CONNECTION_STATE = "connection_state";
        }
    }
}
