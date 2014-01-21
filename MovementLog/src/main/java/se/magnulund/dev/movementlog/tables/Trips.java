package se.magnulund.dev.movementlog.tables;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

import se.magnulund.dev.movementlog.provider.MovementDataContract;

public class Trips {

    public static final String TABLE = "trips";
    private static final String TABLE_CREATE =
            "create table " + TABLE + "("
                    + MovementDataContract.TripLog._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MovementDataContract.TripLog.START_TIME + " INTEGER, "
                    + MovementDataContract.TripLog.END_TIME + " INTEGER, "
                    + MovementDataContract.TripLog.TRIP_TYPE + " INTEGER,"
                    + MovementDataContract.TripLog.START_LATITUDE + " TEXT, "
                    + MovementDataContract.TripLog.START_LONGITUDE + " TEXT, "
                    + MovementDataContract.TripLog.END_LATITUDE + " TEXT, "
                    + MovementDataContract.TripLog.END_LONGITUDE + " TEXT,"
                    + MovementDataContract.TripLog.CONFIRMED + " INTEGER"
                    + ");";


    public static final HashMap<String, String> projectionMap;

    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(MovementDataContract.TripLog._ID, MovementDataContract.TripLog._ID);
        projectionMap.put(MovementDataContract.TripLog.START_TIME, MovementDataContract.TripLog.START_TIME);
        projectionMap.put(MovementDataContract.TripLog.END_TIME, MovementDataContract.TripLog.END_TIME);
        projectionMap.put(MovementDataContract.TripLog.TRIP_TYPE, MovementDataContract.TripLog.TRIP_TYPE);
        projectionMap.put(MovementDataContract.TripLog.START_LATITUDE, MovementDataContract.TripLog.START_LATITUDE);
        projectionMap.put(MovementDataContract.TripLog.START_LONGITUDE, MovementDataContract.TripLog.START_LONGITUDE);
        projectionMap.put(MovementDataContract.TripLog.END_LATITUDE, MovementDataContract.TripLog.END_LATITUDE);
        projectionMap.put(MovementDataContract.TripLog.END_LONGITUDE, MovementDataContract.TripLog.END_LONGITUDE);
        projectionMap.put(MovementDataContract.TripLog.CONFIRMED, MovementDataContract.TripLog.CONFIRMED);
    }

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("Content provider database", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}
