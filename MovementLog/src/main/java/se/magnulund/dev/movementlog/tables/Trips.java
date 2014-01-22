package se.magnulund.dev.movementlog.tables;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

import se.magnulund.dev.movementlog.contracts.TripLogContract;

public class Trips {

    public static final String TABLE = "trips";
    private static final String TABLE_CREATE =
            "create table " + TABLE + "("
                    + TripLogContract.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TripLogContract.Columns.START_TIME + " INTEGER, "
                    + TripLogContract.Columns.END_TIME + " INTEGER, "
                    + TripLogContract.Columns.TRIP_TYPE + " INTEGER,"
                    + TripLogContract.Columns.START_LATITUDE + " TEXT, "
                    + TripLogContract.Columns.START_LONGITUDE + " TEXT, "
                    + TripLogContract.Columns.END_LATITUDE + " TEXT, "
                    + TripLogContract.Columns.END_LONGITUDE + " TEXT,"
                    + TripLogContract.Columns.CONFIRMED + " INTEGER"
                    + ");";


    public static final HashMap<String, String> projectionMap;

    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(TripLogContract.Columns._ID, TripLogContract.Columns._ID);
        projectionMap.put(TripLogContract.Columns.START_TIME, TripLogContract.Columns.START_TIME);
        projectionMap.put(TripLogContract.Columns.END_TIME, TripLogContract.Columns.END_TIME);
        projectionMap.put(TripLogContract.Columns.TRIP_TYPE, TripLogContract.Columns.TRIP_TYPE);
        projectionMap.put(TripLogContract.Columns.START_LATITUDE, TripLogContract.Columns.START_LATITUDE);
        projectionMap.put(TripLogContract.Columns.START_LONGITUDE, TripLogContract.Columns.START_LONGITUDE);
        projectionMap.put(TripLogContract.Columns.END_LATITUDE, TripLogContract.Columns.END_LATITUDE);
        projectionMap.put(TripLogContract.Columns.END_LONGITUDE, TripLogContract.Columns.END_LONGITUDE);
        projectionMap.put(TripLogContract.Columns.CONFIRMED, TripLogContract.Columns.CONFIRMED);
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
