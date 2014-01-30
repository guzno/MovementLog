package se.magnulund.dev.movementlog.test;// Created by Gustav on 14/01/2014.

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.google.android.gms.location.DetectedActivity;

import se.magnulund.dev.movementlog.contracts.RawDataContract;
import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.rawdata.RawData;
import se.magnulund.dev.movementlog.providers.MovementDataProvider;

public class MovementLogProviderTestCase extends ProviderTestCase2<MovementDataProvider> {

    private static final String TAG = "MovementLogProviderTestCase";

    private ContentResolver resolver;
    private Context context;

    /**
     * Constructor.
     */
    public MovementLogProviderTestCase() {
        super(MovementDataProvider.class, TripLogContract.AUTHORITY);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
        context = getMockContext();
    }


    /**
     * Raw data provider tests (TripLogContract.Columns)
     */


    /**
     * Tests insert of single item and checks the inserted data against the original data using 3
     * separate query methods.
     * Methods:
     *      TripLogContract.Columns.addEntry()
     *      TripLogContract.Columns.getEntryByID()
     *      TripLogContract.Columns.getEntry()
     *      resolver.query() on single item URI
     */
    public void test_mdcRawData_insertRowAndVerify() {

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.IN_VEHICLE, 100);

        RawData rawData = new RawData(detectedActivity);

        rawData.setTimestamp(System.currentTimeMillis());
        rawData.setRank(0);

        Uri result = RawDataContract.addEntry(getMockContext(), rawData);
        assertNotNull(result);
        assertNotNull(result.getPathSegments());

        assertEquals(RawDataContract.URI_PART_ALL_CONTENT, result.getPathSegments().get(0));

        int resultID = Integer.valueOf(result.getPathSegments().get(1));

        // check if getEntryByID works

        Cursor cursor = RawDataContract.getEntryByID(context, resultID);

        assertNotNull("TripLogContract.Columns.getEntryByID() returned a null Cursor", cursor);

        assertTrue(RawDataContract.isValidCursor(cursor));

        cursor.moveToFirst();

        RawData storedMovement = RawData.fromCursor(cursor);

        assertEquals(rawData.getType(), storedMovement.getType());
        assertEquals(rawData.getConfidence(), storedMovement.getConfidence());
        assertEquals(rawData.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(rawData.getRank(), storedMovement.getRank());

        // check if getEntry works

        cursor = RawDataContract.getEntry(getMockContext(), result);

        assertNotNull("TripLogContract.Columns.getEntry() returned a null Cursor", cursor);

        assertTrue(RawDataContract.isValidCursor(cursor));

        cursor.moveToFirst();

        storedMovement = RawData.fromCursor(cursor);

        assertEquals(rawData.getType(), storedMovement.getType());
        assertEquals(rawData.getConfidence(), storedMovement.getConfidence());
        assertEquals(rawData.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(rawData.getRank(), storedMovement.getRank());

        //check ContentResolver

        cursor = resolver.query(
                result,
                RawDataContract.DEFAULT_PROJECTION,
                null,
                null,
                RawDataContract.DEFAULT_SORT_ORDER
        );

        assertNotNull("Contentresolver returned a null Cursor", cursor);

        assertTrue(RawDataContract.isValidCursor(cursor));

        cursor.moveToFirst();

        storedMovement = RawData.fromCursor(cursor);

        assertEquals(rawData.getType(), storedMovement.getType());
        assertEquals(rawData.getConfidence(), storedMovement.getConfidence());
        assertEquals(rawData.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(rawData.getRank(), storedMovement.getRank());

    }

    /**
     * Tests update of single item and checks the updated data.
     * Methods:
     *      TripLogContract.Columns.addEntry()
     *      TripLogContract.Columns.getEntryByID()
     *      TripLogContract.Columns.updateEntry()
     */
    public void test_mdcRawData_updateEntryAndVerify() {

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.IN_VEHICLE, 100);

        RawData rawData = new RawData(detectedActivity);

        rawData.setTimestamp(System.currentTimeMillis());
        rawData.setRank(0);

        Uri result = RawDataContract.addEntry(getMockContext(), rawData);

        assertNotNull(result);

        assertNotNull(result.getPathSegments());

        long dataID = Long.valueOf(result.getPathSegments().get(1));

        RawData updatedMovement = new RawData(detectedActivity);

        updatedMovement.setTimestamp(System.currentTimeMillis());
        updatedMovement.setRank(1);

        boolean updateResult = RawDataContract.updateEntry(getMockContext(), dataID, updatedMovement);

        assertTrue(updateResult);

        Cursor cursor = RawDataContract.getEntryByID(getMockContext(), dataID);

        assertNotNull("TripLogContract.Columns.getEntryByID() returned a null Cursor", cursor);

        assertTrue(RawDataContract.isValidCursor(cursor));

        cursor.moveToFirst();

        RawData storedMovement = RawData.fromCursor(cursor);

        assertEquals(updatedMovement.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(updatedMovement.getRank(), storedMovement.getRank());
    }

    public void test_mdcRawData_deleteEntriesAndVerify() {

        Cursor cursor = RawDataContract.getCursor(getMockContext());

        assertNotNull(cursor);

        int initialEntries = cursor.getCount();

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.IN_VEHICLE, 100);

        RawData rawData = new RawData(detectedActivity);

        rawData.setTimestamp(System.currentTimeMillis() - 100000);
        rawData.setRank(0);

        Uri result1 = RawDataContract.addEntry(getMockContext(), rawData);

        assertNotNull(result1);

        rawData.setTimestamp(System.currentTimeMillis());

        Uri result2 = RawDataContract.addEntry(getMockContext(), rawData);

        assertNotNull(result2);

        assertNotNull(result2.getPathSegments());

        long result2ID = Long.valueOf(result2.getPathSegments().get(1));

        Uri result3 = RawDataContract.addEntry(getMockContext(), rawData);

        assertNotNull(result3);

        cursor = RawDataContract.getCursor(getMockContext());

        assertNotNull(cursor);

        int updatedEntries = cursor.getCount();

        assertEquals(updatedEntries, initialEntries + 3);

        cursor = RawDataContract.getEntry(getMockContext(), result1);

        assertNotNull(cursor);

        assertTrue(cursor.getCount() > 0);

        int entriesDeleted = RawDataContract.deleteOldEntries(getMockContext(), System.currentTimeMillis() - 50000);

        assertTrue(entriesDeleted > 0);

        cursor = RawDataContract.getEntry(getMockContext(), result1);

        assertNotNull(cursor);

        assertTrue(cursor.getCount() == 0);

        cursor = RawDataContract.getCursor(getMockContext());

        assertNotNull(cursor);

        assertEquals(cursor.getCount(), updatedEntries - entriesDeleted);

        cursor = RawDataContract.getEntryByID(getMockContext(), result2ID);

        assertNotNull(cursor);

        assertTrue(cursor.getCount() > 0);

        entriesDeleted = RawDataContract.deleteEntryByID(getMockContext(), result2ID);

        assertEquals(entriesDeleted, 1);

        cursor = RawDataContract.getEntryByID(getMockContext(), result2ID);

        assertNotNull(cursor);

        assertTrue(cursor.getCount() == 0);

        entriesDeleted = RawDataContract.deleteAllEntries(getMockContext());

        assertTrue( entriesDeleted > 0 );

        cursor = RawDataContract.getCursor(getMockContext());

        assertNotNull(cursor);

        assertEquals(cursor.getCount(), 0);
    }

    /**
     *  Trip data provider tests
     *
     */



}
