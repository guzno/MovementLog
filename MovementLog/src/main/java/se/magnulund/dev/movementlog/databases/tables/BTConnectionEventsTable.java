package se.magnulund.dev.movementlog.databases.tables;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

import se.magnulund.dev.movementlog.contracts.BTContract;
import se.magnulund.dev.movementlog.contracts.RawDataContract;

public class BTConnectionEventsTable {
    public static final String TABLE = "bt_connection_events";
    public static HashMap<String, String> projectionMap;

    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(BTContract.ConnectionEvents.Columns._ID, BTContract.ConnectionEvents.Columns._ID);
        projectionMap.put(BTContract.ConnectionEvents.Columns.TIMESTAMP, BTContract.ConnectionEvents.Columns.TIMESTAMP);
        projectionMap.put(BTContract.ConnectionEvents.Columns.DEVICE_ID, BTContract.ConnectionEvents.Columns.DEVICE_ID);
        projectionMap.put(BTContract.ConnectionEvents.Columns.CONNECTION_STATE, BTContract.ConnectionEvents.Columns.CONNECTION_STATE);
    }

    private static final String TABLE_CREATE =
            "create table " + TABLE + "("
                    + BTContract.ConnectionEvents.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + BTContract.ConnectionEvents.Columns.TIMESTAMP + " INTEGER, "
                    + BTContract.ConnectionEvents.Columns.DEVICE_ID + " INTEGER, "
                    + BTContract.ConnectionEvents.Columns.CONNECTION_STATE + " INTEGER"
                    + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("Content provider database", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}
