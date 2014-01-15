package se.magnulund.dev.movementlog.test;// Created by Gustav on 14/01/2014.

import android.content.ContentResolver;
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

        Cursor cursorByID = MovementDataContract.RawData.getEntryByID(getMockContext(), resultID);

        Log.e(TAG, "cols: "+cursorByID.getColumnCount());

        int i = cursorByID.getColumnIndex(MovementDataContract.RawData.ACTIVITY_TYPE);

        assertTrue(i > -1);

        cursorByID.moveToFirst();

        int activity_type = cursorByID.getInt(i);

        i = cursorByID.getColumnIndex(MovementDataContract.RawData.CONFIDENCE);

        assertTrue(i > -1);

        int confidence = cursorByID.getInt(i);

        i = cursorByID.getColumnIndex(MovementDataContract.RawData.TIMESTAMP);

        assertTrue(i > -1);

        int timestamp = cursorByID.getInt(i);

        i = cursorByID.getColumnIndex(MovementDataContract.RawData.CONFIDENCE_RANK);

        assertTrue(i > -1);

        int confidence_rank = cursorByID.getInt(i);

        assertEquals(detectedMovement.getType(), activity_type);
        assertEquals(detectedMovement.getConfidence(), confidence);
        assertEquals(detectedMovement.getTimestamp(), timestamp);
        assertEquals(detectedMovement.getRank(), confidence_rank);

        // check if getEntry works

        Cursor cursorByUri = MovementDataContract.RawData.getEntry(getMockContext(), result);

        cursorByUri.moveToFirst();

        i = cursorByUri.getColumnIndex(MovementDataContract.RawData.ACTIVITY_TYPE);

        assertTrue(i > -1);

        activity_type = cursorByUri.getInt(i);

        i = cursorByUri.getColumnIndex(MovementDataContract.RawData.CONFIDENCE);

        assertTrue(i > -1);

        confidence = cursorByUri.getInt(i);

        i = cursorByUri.getColumnIndex(MovementDataContract.RawData.TIMESTAMP);

        assertTrue(i > -1);

        timestamp = cursorByUri.getInt(i);

        i = cursorByUri.getColumnIndex(MovementDataContract.RawData.CONFIDENCE_RANK);

        assertTrue(i > -1);

        confidence_rank = cursorByUri.getInt(i);

        assertEquals(detectedMovement.getType(), activity_type);
        assertEquals(detectedMovement.getConfidence(), confidence);
        assertEquals(detectedMovement.getTimestamp(), timestamp);
        assertEquals(detectedMovement.getRank(), confidence_rank);

        //check ContentResolver

        Cursor cursor = resolver.query(
                result,
                MovementDataContract.RawData.DEFAULT_PROJECTION,
                null,
                null,
                MovementDataContract.RawData.DEFAULT_SORT_ORDER
        );

        assertNotNull(cursor);

        cursor.moveToFirst();

        i = cursor.getColumnIndex(MovementDataContract.RawData.ACTIVITY_TYPE);

        assertTrue(i > -1);

        activity_type = cursor.getInt(i);

        i = cursor.getColumnIndex(MovementDataContract.RawData.CONFIDENCE);

        assertTrue(i > -1);

        confidence = cursor.getInt(i);

        i = cursor.getColumnIndex(MovementDataContract.RawData.TIMESTAMP);

        assertTrue(i > -1);

        timestamp = cursor.getInt(i);

        i = cursor.getColumnIndex(MovementDataContract.RawData.CONFIDENCE_RANK);

        assertTrue(i > -1);

        confidence_rank = cursor.getInt(i);

        assertEquals(detectedMovement.getType(), activity_type);
        assertEquals(detectedMovement.getConfidence(), confidence);
        assertEquals(detectedMovement.getTimestamp(), timestamp);
        assertEquals(detectedMovement.getRank(), confidence_rank);

    }

    /**
     *  Trip data provider tests
     *
     */

}
