package se.magnulund.dev.movementlog.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import se.magnulund.dev.movementlog.databases.tables.BTConnectionEventsTable;
import se.magnulund.dev.movementlog.databases.tables.BTDevicesTable;

/**
 * Created by erikeelde on 21/1/2014.
 */
public class BTDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BT_data.db";
    private static final int DATABASE_VERSION = 1;

    public BTDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        BTDevicesTable.onCreate(db);
        BTConnectionEventsTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        BTDevicesTable.onUpgrade(db, oldVersion, newVersion);
        BTConnectionEventsTable.onUpgrade(db, oldVersion, newVersion);
    }
}
