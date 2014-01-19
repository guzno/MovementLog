package se.magnulund.dev.movementlog.test;// Created by Gustav on 14/01/2014.

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
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
    public void test_mdcRawData_insertRowAndVerify() {

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

        assertTrue(MovementDataContract.RawData.isValidCursor(cursor));

        cursor.moveToFirst();

        DetectedMovement storedMovement = DetectedMovement.fromCursor(cursor);

        assertEquals(detectedMovement.getType(), storedMovement.getType());
        assertEquals(detectedMovement.getConfidence(), storedMovement.getConfidence());
        assertEquals(detectedMovement.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(detectedMovement.getRank(), storedMovement.getRank());

        // check if getEntry works

        cursor = MovementDataContract.RawData.getEntry(getMockContext(), result);

        assertNotNull("MovementDataContract.RawData.getEntry() returned a null Cursor", cursor);

        assertTrue(MovementDataContract.RawData.isValidCursor(cursor));

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

        assertTrue(MovementDataContract.RawData.isValidCursor(cursor));

        cursor.moveToFirst();

        storedMovement = DetectedMovement.fromCursor(cursor);

        assertEquals(detectedMovement.getType(), storedMovement.getType());
        assertEquals(detectedMovement.getConfidence(), storedMovement.getConfidence());
        assertEquals(detectedMovement.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(detectedMovement.getRank(), storedMovement.getRank());

    }

    /**
     * Tests update of single item and checks the updated data.
     * Methods:
     *      MovementDataContract.RawData.addEntry()
     *      MovementDataContract.RawData.getEntryByID()
     *      MovementDataContract.RawData.updateEntry()
     */
    public void test_mdcRawData_updateEntryAndVerify() {

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.IN_VEHICLE, 100);

        DetectedMovement detectedMovement = new DetectedMovement(detectedActivity);

        detectedMovement.setTimestamp((int) Calendar.getInstance().getTimeInMillis());
        detectedMovement.setRank(0);

        Uri result = MovementDataContract.RawData.addEntry(getMockContext(), detectedMovement);

        assertNotNull(result);

        assertNotNull(result.getPathSegments());

        long dataID = Long.valueOf(result.getPathSegments().get(1));

        DetectedMovement updatedMovement = new DetectedMovement(detectedActivity);

        updatedMovement.setTimestamp((int) Calendar.getInstance().getTimeInMillis());
        updatedMovement.setRank(1);

        boolean updateResult = MovementDataContract.RawData.updateEntry(getMockContext(), dataID, updatedMovement);

        assertTrue(updateResult);

        Cursor cursor = MovementDataContract.RawData.getEntryByID(getMockContext(), dataID);

        assertNotNull("MovementDataContract.RawData.getEntryByID() returned a null Cursor", cursor);

        assertTrue(MovementDataContract.RawData.isValidCursor(cursor));

        cursor.moveToFirst();

        DetectedMovement storedMovement = DetectedMovement.fromCursor(cursor);

        assertEquals(updatedMovement.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(updatedMovement.getRank(), storedMovement.getRank());
    }

    public void test_mdcRawData_deleteEntriesAndVerify() {

        Cursor cursor = MovementDataContract.RawData.getCursor(getMockContext());

        assertNotNull(cursor);

        int initialEntries = cursor.getCount();

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.IN_VEHICLE, 100);

        DetectedMovement detectedMovement = new DetectedMovement(detectedActivity);

        detectedMovement.setTimestamp((int) Calendar.getInstance().getTimeInMillis() - 100000);
        detectedMovement.setRank(0);

        Uri result1 = MovementDataContract.RawData.addEntry(getMockContext(), detectedMovement);

        assertNotNull(result1);

        detectedMovement.setTimestamp((int) Calendar.getInstance().getTimeInMillis());

        Uri result2 = MovementDataContract.RawData.addEntry(getMockContext(), detectedMovement);

        assertNotNull(result2);

        assertNotNull(result2.getPathSegments());

        long result2ID = Long.valueOf(result2.getPathSegments().get(1));

        Uri result3 = MovementDataContract.RawData.addEntry(getMockContext(), detectedMovement);

        assertNotNull(result3);

        cursor = MovementDataContract.RawData.getCursor(getMockContext());

        assertNotNull(cursor);

        int updatedEntries = cursor.getCount();

        assertEquals(updatedEntries, initialEntries + 3);

        cursor = MovementDataContract.RawData.getEntry(getMockContext(), result1);

        assertNotNull(cursor);

        assertTrue(cursor.getCount() > 0);

        int entriesDeleted = MovementDataContract.RawData.deleteOldEntries(getMockContext(), (int) Calendar.getInstance().getTimeInMillis() - 50000);

        assertTrue(entriesDeleted > 0);

        cursor = MovementDataContract.RawData.getEntry(getMockContext(), result1);

        assertNotNull(cursor);

        assertTrue(cursor.getCount() == 0);

        cursor = MovementDataContract.RawData.getCursor(getMockContext());

        assertNotNull(cursor);

        assertEquals(cursor.getCount(), updatedEntries - entriesDeleted);

        cursor = MovementDataContract.RawData.getEntryByID(getMockContext(), result2ID);

        assertNotNull(cursor);

        assertTrue(cursor.getCount() > 0);

        entriesDeleted = MovementDataContract.RawData.deleteEntryByID(getMockContext(), result2ID);

        assertEquals(entriesDeleted, 1);

        cursor = MovementDataContract.RawData.getEntryByID(getMockContext(), result2ID);

        assertNotNull(cursor);

        assertTrue(cursor.getCount() == 0);

        entriesDeleted = MovementDataContract.RawData.deleteAllEntries(getMockContext());

        assertTrue( entriesDeleted > 0 );

        cursor = MovementDataContract.RawData.getCursor(getMockContext());

        assertNotNull(cursor);

        assertEquals(cursor.getCount(), 0);
    }

    /**
     *  Trip data provider tests
     *
     */



}
