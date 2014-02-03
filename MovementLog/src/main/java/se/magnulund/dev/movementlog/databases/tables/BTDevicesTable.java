package se.magnulund.dev.movementlog.databases.tables;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

import se.magnulund.dev.movementlog.contracts.BTContract;

public class BTDevicesTable {
    public static final String TABLE = "bt_devices";
    public static HashMap<String, String> projectionMap;

    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(BTContract.Devices.Columns._ID, BTContract.Devices.Columns._ID);
        projectionMap.put(BTContract.Devices.Columns.ADDRESS, BTContract.Devices.Columns.ADDRESS);
        projectionMap.put(BTContract.Devices.Columns.NAME, BTContract.Devices.Columns.NAME);
        projectionMap.put(BTContract.Devices.Columns.MAJOR_CLASS, BTContract.Devices.Columns.MAJOR_CLASS);
        projectionMap.put(BTContract.Devices.Columns.SUBCLASS, BTContract.Devices.Columns.SUBCLASS);
    }

    private static final String TABLE_CREATE =
            "create table " + TABLE + "("
                    + BTContract.Devices.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + BTContract.Devices.Columns.ADDRESS + " TEXT, "
                    + BTContract.Devices.Columns.NAME + " TEXT, "
                    + BTContract.Devices.Columns.MAJOR_CLASS + " INTEGER, "
                    + BTContract.Devices.Columns.SUBCLASS + " INTEGER"
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
