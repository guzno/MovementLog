package se.magnulund.dev.movementlog.databases;

        import android.content.Context;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;

        import se.magnulund.dev.movementlog.databases.tables.RawData;
        import se.magnulund.dev.movementlog.databases.tables.Trips;

/**
 * Created by erikeelde on 21/1/2014.
 */
public class MovementsDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "movement_data.db";
    private static final int DATABASE_VERSION = 1;

    public MovementsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        RawData.onCreate(db);
        Trips.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        RawData.onUpgrade(db, oldVersion, newVersion);
        Trips.onUpgrade(db, oldVersion, newVersion);
    }
}
