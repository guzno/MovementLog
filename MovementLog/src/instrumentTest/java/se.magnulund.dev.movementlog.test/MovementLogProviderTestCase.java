package se.magnulund.dev.movementlog.test;// Created by Gustav on 14/01/2014.

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.util.Calendar;

import se.magnulund.dev.movementlog.DetectedMovement;
import se.magnulund.dev.movementlog.provider.MovementDataContract;
import se.magnulund.dev.movementlog.provider.MovementDataProvider;

public class MovementLogProviderTestCase extends ProviderTestCase2<MovementDataProvider> {

    private static final String TAG = "MovementLogProviderTestCase";

    private ContentResolver resolver;

    /**
     * Constructor.
     */
    public MovementLogProviderTestCase() {
        super(MovementDataProvider.class, MovementDataContract.AUTHORITY);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
    }


    /**
     * Raw data provider tests (MovementDataContract.RawData)
     */


    /**
     * Tests insert of single item and checks the inserted data against the original data using 3
     * separate query methods.
     * Methods:
     *      MovementDataContract.RawData.addEntry()
     *      MovementDataContract.RawData.getEntryByID()
     *      MovementDataContract.RawData.getEntry()
     *      resolver.query() on single item URI
     */
    public void test_mdcRawData_insertRowAndCheckResult() {

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.IN_VEHICLE, 100);

        DetectedMovement detectedMovement = new DetectedMovement(detectedActivity);

        detectedMovement.setTimestamp((int) Calendar.getInstance().getTimeInMillis());
        detectedMovement.setRank(0);

        Uri result = MovementDataContract.RawData.addEntry(getMockContext(), detectedMovement);
        assertNotNull(result);
        assertNotNull(result.getPathSegments());

        assertEquals(MovementDataContract.RawData.URI_PART_ALL_CONTENT, result.getPathSegments().get(0));

        int resultID = Integer.valueOf(result.getPathSegments().get(1));

        // check if getEntryByID works

        Cursor cursor = MovementDataContract.RawData.getEntryByID(getMockContext(), resultID);

        assertNotNull("MovementDataContract.RawData.getEntryByID() returned a null Cursor", cursor);

        cursor.moveToFirst();

        DetectedMovement storedMovement = DetectedMovement.fromCursor(cursor);

        assertEquals(detectedMovement.getType(), storedMovement.getType());
        assertEquals(detectedMovement.getConfidence(), storedMovement.getConfidence());
        assertEquals(detectedMovement.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(detectedMovement.getRank(), storedMovement.getRank());

        // check if getEntry works

        cursor = MovementDataContract.RawData.getEntry(getMockContext(), result);

        assertNotNull("MovementDataContract.RawData.getEntry() returned a null Cursor", cursor);

        cursor.moveToFirst();

        storedMovement = DetectedMovement.fromCursor(cursor);

        assertEquals(detectedMovement.getType(), storedMovement.getType());
        assertEquals(detectedMovement.getConfidence(), storedMovement.getConfidence());
        assertEquals(detectedMovement.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(detectedMovement.getRank(), storedMovement.getRank());

        //check ContentResolver

        cursor = resolver.query(
                result,
                MovementDataContract.RawData.DEFAULT_PROJECTION,
                null,
                null,
                MovementDataContract.RawData.DEFAULT_SORT_ORDER
        );

        assertNotNull("Contentresolver returned a null Cursor", cursor);

        cursor.moveToFirst();

        storedMovement = DetectedMovement.fromCursor(cursor);

        assertEquals(detectedMovement.getType(), storedMovement.getType());
        assertEquals(detectedMovement.getConfidence(), storedMovement.getConfidence());
        assertEquals(detectedMovement.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(detectedMovement.getRank(), storedMovement.getRank());

    }

    /**
     *  Trip data provider tests
     *
     */

}
