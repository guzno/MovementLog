package se.magnulund.dev.movementlog.test; // Created by Gustav on 14/01/2014.

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry; // AndroidX Test
import androidx.test.ext.junit.runners.AndroidJUnit4; // AndroidX Test

import com.google.android.gms.location.DetectedActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test; // JUnit 4
import org.junit.runner.RunWith; // JUnit 4

import static org.junit.Assert.*; // JUnit 4 Asserts

import se.magnulund.dev.movementlog.contracts.RawDataContract;
import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.rawdata.RawData;
// import se.magnulund.dev.movementlog.providers.TripLogProvider; // Provider itself not directly instantiated

@RunWith(AndroidJUnit4.class) // Use AndroidJUnit4 runner
public class MovementLogProviderTestCase { // No longer extends ProviderTestCase2

    private static final String TAG = "MovementLogProviderTestCase";

    private ContentResolver resolver;
    private Context context;

    @Before
    public void setUp() throws Exception {
        // super.setUp(); // No longer needed
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        resolver = context.getContentResolver(); // Use target context's resolver
        // Clean up before each test to ensure isolation
        RawDataContract.deleteAllEntries(context);
        TripLogContract.deleteAllTrips(context);
    }

    @After
    public void tearDown() throws Exception {
        // Clean up after each test
        RawDataContract.deleteAllEntries(context);
        TripLogContract.deleteAllTrips(context);
        // super.tearDown(); // No longer needed
    }


    /**
     * Raw data provider tests (RawDataContract)
     */

    @Test
    public void testRawDataInsertRowAndVerify() {

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.IN_VEHICLE, 100);
        RawData rawData = new RawData(detectedActivity);
        rawData.setTimestamp(System.currentTimeMillis());
        rawData.setRank(0);

        Uri result = RawDataContract.addEntry(context, rawData); // Use context
        assertNotNull(result);
        assertNotNull(result.getPathSegments());

        assertEquals(RawDataContract.URI_PART_ALL_CONTENT, result.getPathSegments().get(0));

        int resultID = Integer.valueOf(result.getPathSegments().get(1));

        // check if getEntryByID works
        Cursor cursor = RawDataContract.getEntryByID(context, resultID);
        assertNotNull("RawDataContract.getEntryByID() returned a null Cursor", cursor);
        if (cursor == null) return; // Avoid NPE if assert fails without throwing

        assertTrue(RawDataContract.isValidCursor(cursor));
        assertTrue("Cursor was empty", cursor.moveToFirst());

        RawData storedMovement = RawData.fromCursor(cursor);
        cursor.close();

        assertEquals(rawData.getType(), storedMovement.getType());
        assertEquals(rawData.getConfidence(), storedMovement.getConfidence());
        assertEquals(rawData.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(rawData.getRank(), storedMovement.getRank());

        // check if getEntry works
        cursor = RawDataContract.getEntry(context, result);
        assertNotNull("RawDataContract.getEntry() returned a null Cursor", cursor);
        if (cursor == null) return;

        assertTrue(RawDataContract.isValidCursor(cursor));
        assertTrue("Cursor was empty", cursor.moveToFirst());
        storedMovement = RawData.fromCursor(cursor);
        cursor.close();

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
        if (cursor == null) return;

        assertTrue(RawDataContract.isValidCursor(cursor));
        assertTrue("Cursor was empty",cursor.moveToFirst());
        storedMovement = RawData.fromCursor(cursor);
        cursor.close();

        assertEquals(rawData.getType(), storedMovement.getType());
        assertEquals(rawData.getConfidence(), storedMovement.getConfidence());
        assertEquals(rawData.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(rawData.getRank(), storedMovement.getRank());
    }

    @Test
    public void testRawDataUpdateEntryAndVerify() {
        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.IN_VEHICLE, 100);
        RawData rawData = new RawData(detectedActivity);
        rawData.setTimestamp(System.currentTimeMillis());
        rawData.setRank(0);

        Uri result = RawDataContract.addEntry(context, rawData);
        assertNotNull(result);
        assertNotNull(result.getPathSegments());

        long dataID = Long.valueOf(result.getPathSegments().get(1));

        RawData updatedMovement = new RawData(detectedActivity);
        long newTimestamp = System.currentTimeMillis(); // Ensure it's different
        updatedMovement.setTimestamp(newTimestamp);
        updatedMovement.setRank(1);

        boolean updateResult = RawDataContract.updateEntry(context, dataID, updatedMovement);
        assertTrue(updateResult);

        Cursor cursor = RawDataContract.getEntryByID(context, dataID);
        assertNotNull("RawDataContract.getEntryByID() returned a null Cursor", cursor);
        if (cursor == null) return;

        assertTrue(RawDataContract.isValidCursor(cursor));
        assertTrue(cursor.moveToFirst());

        RawData storedMovement = RawData.fromCursor(cursor);
        cursor.close();

        assertEquals(updatedMovement.getTimestamp(), storedMovement.getTimestamp());
        assertEquals(updatedMovement.getRank(), storedMovement.getRank());
    }

    @Test
    public void testRawDataDeleteEntriesAndVerify() {
        Cursor cursor = RawDataContract.getCursor(context);
        assertNotNull(cursor);
        int initialEntries = cursor.getCount();
        cursor.close();

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.IN_VEHICLE, 100);
        RawData rawData = new RawData(detectedActivity);
        long oldTimestamp = System.currentTimeMillis() - 100000;
        rawData.setTimestamp(oldTimestamp);
        rawData.setRank(0);

        Uri result1 = RawDataContract.addEntry(context, rawData);
        assertNotNull(result1);

        long currentTimestamp = System.currentTimeMillis();
        rawData.setTimestamp(currentTimestamp);
        Uri result2 = RawDataContract.addEntry(context, rawData);
        assertNotNull(result2);
        assertNotNull(result2.getPathSegments());
        long result2ID = Long.valueOf(result2.getPathSegments().get(1));

        Uri result3 = RawDataContract.addEntry(context, rawData);
        assertNotNull(result3);

        cursor = RawDataContract.getCursor(context);
        assertNotNull(cursor);
        int updatedEntries = cursor.getCount();
        cursor.close();
        assertEquals(updatedEntries, initialEntries + 3);

        cursor = RawDataContract.getEntry(context, result1);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() > 0);
        cursor.close();

        int entriesDeleted = RawDataContract.deleteOldEntries(context, System.currentTimeMillis() - 50000);
        assertEquals(1, entriesDeleted); // Only one entry should be old enough

        cursor = RawDataContract.getEntry(context, result1);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount()); // Should be deleted
        cursor.close();

        cursor = RawDataContract.getCursor(context);
        assertNotNull(cursor);
        assertEquals(updatedEntries - entriesDeleted, cursor.getCount());
        cursor.close();

        cursor = RawDataContract.getEntryByID(context, result2ID);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() > 0);
        cursor.close();

        entriesDeleted = RawDataContract.deleteEntryByID(context, result2ID);
        assertEquals(1, entriesDeleted);

        cursor = RawDataContract.getEntryByID(context, result2ID);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();

        entriesDeleted = RawDataContract.deleteAllEntries(context);
        assertEquals(initialEntries + 1, entriesDeleted); // result3 is left

        cursor = RawDataContract.getCursor(context);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    /**
     *  Trip data provider tests (TripLogContract)
     */
    // TODO: Add tests for TripLogContract similar to RawDataContract tests
}
