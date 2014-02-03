package se.magnulund.dev.movementlog.databases;

        import android.content.Context;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;

        import se.magnulund.dev.movementlog.databases.tables.RawDataTable;
        import se.magnulund.dev.movementlog.databases.tables.TripsTable;

/**
 * Created by erikeelde on 21/1/2014.
 */
public class TripLogDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "movement_data.db";
    private static final int DATABASE_VERSION = 1;

    public TripLogDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        RawDataTable.onCreate(db);
        TripsTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        RawDataTable.onUpgrade(db, oldVersion, newVersion);
        TripsTable.onUpgrade(db, oldVersion, newVersion);
    }
}
