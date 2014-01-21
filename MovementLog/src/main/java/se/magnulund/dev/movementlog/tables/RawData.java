package se.magnulund.dev.movementlog.tables;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

import se.magnulund.dev.movementlog.provider.MovementDataContract;

public class RawData {
    public static final String TABLE = "rawdata";
    public static HashMap<String, String> projectionMap;

    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(MovementDataContract.RawDataLog._ID, MovementDataContract.RawDataLog._ID);
        projectionMap.put(MovementDataContract.RawDataLog.TIMESTAMP, MovementDataContract.RawDataLog.TIMESTAMP);
        projectionMap.put(MovementDataContract.RawDataLog.ACTIVITY_TYPE, MovementDataContract.RawDataLog.ACTIVITY_TYPE);
        projectionMap.put(MovementDataContract.RawDataLog.CONFIDENCE, MovementDataContract.RawDataLog.CONFIDENCE);
        projectionMap.put(MovementDataContract.RawDataLog.CONFIDENCE_RANK, MovementDataContract.RawDataLog.CONFIDENCE_RANK);
    }

    private static final String TABLE_CREATE =
            "create table " + TABLE + "("
                    + MovementDataContract.RawDataLog._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MovementDataContract.RawDataLog.TIMESTAMP + " INTEGER, "
                    + MovementDataContract.RawDataLog.ACTIVITY_TYPE + " INTEGER, "
                    + MovementDataContract.RawDataLog.CONFIDENCE + " INTEGER, "
                    + MovementDataContract.RawDataLog.CONFIDENCE_RANK + " INTEGER"
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
