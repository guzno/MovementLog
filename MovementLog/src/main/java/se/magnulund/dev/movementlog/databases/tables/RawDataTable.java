package se.magnulund.dev.movementlog.databases.tables;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

import se.magnulund.dev.movementlog.contracts.RawDataContract;

public class RawDataTable {
    public static final String TABLE = "rawdata";
    public static HashMap<String, String> projectionMap;

    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(RawDataContract.Columns._ID, RawDataContract.Columns._ID);
        projectionMap.put(RawDataContract.Columns.TIMESTAMP, RawDataContract.Columns.TIMESTAMP);
        projectionMap.put(RawDataContract.Columns.ACTIVITY_TYPE, RawDataContract.Columns.ACTIVITY_TYPE);
        projectionMap.put(RawDataContract.Columns.CONFIDENCE, RawDataContract.Columns.CONFIDENCE);
        projectionMap.put(RawDataContract.Columns.CONFIDENCE_RANK, RawDataContract.Columns.CONFIDENCE_RANK);
    }

    private static final String TABLE_CREATE =
            "create table " + TABLE + "("
                    + RawDataContract.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + RawDataContract.Columns.TIMESTAMP + " INTEGER, "
                    + RawDataContract.Columns.ACTIVITY_TYPE + " INTEGER, "
                    + RawDataContract.Columns.CONFIDENCE + " INTEGER, "
                    + RawDataContract.Columns.CONFIDENCE_RANK + " INTEGER"
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
