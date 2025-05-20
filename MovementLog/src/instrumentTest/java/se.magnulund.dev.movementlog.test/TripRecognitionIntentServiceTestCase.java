package se.magnulund.dev.movementlog.test; // Created by Gustav on 28/01/2014.

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ServiceTestRule;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

// import se.magnulund.dev.movementlog.contracts.RawDataContract; // Not used directly
import se.magnulund.dev.movementlog.services.TripRecognitionIntentService; // Used for intent action

@RunWith(AndroidJUnit4.class)
public class TripRecognitionIntentServiceTestCase { // No longer extends ServiceTestCase
    private static final String TAG = "TripRecognitionTest"; // Changed TAG
    private static final String TESTING = "testing";
    private Context context;
    private CountDownLatch latch;
    private TripReccognitionIntentServiceWrapper mService;

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private void setupServiceAndLatch(Intent intent) throws TimeoutException {
        latch = new CountDownLatch(1);
        // Bind to the service to get an instance of it and set the latch
        // TripReccognitionIntentServiceWrapper.LocalBinder binder = (TripReccognitionIntentServiceWrapper.LocalBinder) serviceRule.bindService(intent);
        // mService = binder.getService();
        // mService.setLatch(latch);
        // Or, if the wrapper is simple and only needs to call super.onHandleIntent and latch.countDown(),
        // we might not need a binder if the ServiceTestCase allowed setting it up before start.
        // With ServiceTestRule, we start the service, then would need another way to pass the latch if not binding.
        // For IntentService, testing the onHandleIntent logic often means verifying side effects (e.g. DB changes, broadcasts)
        // rather than direct interaction after start, unless it's a JobIntentService or a bound service.

        // Since TripReccognitionIntentServiceWrapper is the class under test for ServiceTestRule:
        // We need to ensure the wrapper instance used by the rule gets the latch.
        // This is tricky with ServiceTestRule for IntentServices if you need to modify the instance pre-start.
        // A common pattern is to use a binder, or to test via side-effects.

        // For this specific case, the wrapper is simple. We'll start it and trust the wrapper's onHandleIntent.
        // The serviceRule will create an instance of TripReccognitionIntentServiceWrapper.
        // To set the latch on *that* instance, we'd ideally bind.
        // However, IntentServices are tricky to bind to once started with startService for their main work.
        // Let's assume for now the wrapper needs to be designed to fetch the latch itself,
        // or we test effects. The original test directly called startService on the wrapper.

        // A simplified approach for this refactor, maintaining existing latch logic as much as possible:
        // 1. Start the service (which is the wrapper).
        // 2. The wrapper's onHandleIntent will be called. If it's designed to pick up a static latch, that might work.
        // Or, we use a binder to set the latch on the specific instance.
        // Let's add a binder to TripReccognitionIntentServiceWrapper.
        IBinder binder = serviceRule.bindService(new Intent(context, TripReccognitionIntentServiceWrapper.class));
        mService = ((TripReccognitionIntentServiceWrapper.LocalBinder) binder).getService();
        mService.setLatch(latch);
    }


    @Test
    public void testHandleUndefinedIntent() throws TimeoutException, InterruptedException {
        Intent intent = new Intent(context, TripReccognitionIntentServiceWrapper.class); // Target the wrapper
        intent.putExtra("WRONG_EXTRA", 1);
        // setupServiceAndLatch must be called before startService if we need to set latch via binder.
        // However, startService is what triggers onHandleIntent for an IntentService.
        // This highlights the difficulty of using ServiceTestRule for an IntentService that needs pre-start instance setup.

        // Let's try a pattern where the wrapper can retrieve the latch statically, or we accept testing side effects.
        // For now, to closely match original, we set latch then start. This might require wrapper to be a bound service.
        // The original ServiceTestCase likely allowed more direct instance manipulation before onHandleIntent.

        // Re-thinking: The wrapper IS the service. The rule starts THE wrapper.
        // So, we bind to it first to set the latch.
        setupServiceAndLatch(intent); // This binds and sets the latch

        serviceRule.startService(intent); // This now calls onStartCommand -> onHandleIntent on the wrapper instance

        assertTrue("Latch did not count down", latch.await(10, TimeUnit.SECONDS)); // Wait for onHandleIntent to complete
    }

    @Test
    public void testHandleResultAndStore() throws TimeoutException, InterruptedException {
        Intent intent = new Intent(context, TripReccognitionIntentServiceWrapper.class); // Target the wrapper
        ActivityRecognitionResult r = getActivityRecognitionResult(RESULT_STILL);
        intent.putExtra(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT, r);
        intent.putExtra(TESTING, true);

        setupServiceAndLatch(intent); // Bind and set latch

        serviceRule.startService(intent); // Execute onHandleIntent

        assertTrue("Latch did not count down", latch.await(10, TimeUnit.SECONDS));
        // Further assertions would typically check database state or broadcasts here
    }

    // --- Test data setup copied from original ---
    private static final int RESULT_IN_VEHICLE = 0;
    private static final int RESULT_BIKING = 1;
    private static final int RESULT_STILL = 2;
    private static final int RESULT_UNKNOWN = 3;
    private static final int RESULT_TILTING = 4;
    private static final int RESULT_ONFOOT = 5;
    private static final int RESULT_POSSIBLE_IN_VEHICLE = 6;
    private static final int RESULT_POSSIBLE_BIKING = 7;

    private ActivityRecognitionResult getActivityRecognitionResult(int resultType) {

        ArrayList<DetectedActivity> arrayList = new ArrayList<>();

        DetectedActivity d;

        switch (resultType) {
            case RESULT_IN_VEHICLE:
                d = new DetectedActivity(DetectedActivity.IN_VEHICLE, 90);
                arrayList.add(d);
                break;
            case RESULT_BIKING:
                d = new DetectedActivity(DetectedActivity.ON_BICYCLE, 90);
                arrayList.add(d);
                break;
            case RESULT_STILL:
                d = new DetectedActivity(DetectedActivity.STILL, 90);
                arrayList.add(d);
                break;
            case RESULT_UNKNOWN:
                d = new DetectedActivity(DetectedActivity.UNKNOWN, 90);
                arrayList.add(d);
                break;
            case RESULT_TILTING:
                d = new DetectedActivity(DetectedActivity.TILTING, 90);
                arrayList.add(d);
                break;
            case RESULT_ONFOOT:
                d = new DetectedActivity(DetectedActivity.ON_FOOT, 90);
                arrayList.add(d);
                break;
            case RESULT_POSSIBLE_IN_VEHICLE:
                d = new DetectedActivity(DetectedActivity.UNKNOWN, 48);
                arrayList.add(d);
                d = new DetectedActivity(DetectedActivity.IN_VEHICLE, 42);
                arrayList.add(d);
                break;
            case RESULT_POSSIBLE_BIKING:
                d = new DetectedActivity(DetectedActivity.UNKNOWN, 48);
                arrayList.add(d);
                d = new DetectedActivity(DetectedActivity.ON_BICYCLE, 42);
                arrayList.add(d);
                break;
            default:
                assertTrue("Invalid result type in test...", false);
        }

        if (resultType == RESULT_STILL) {
            d = new DetectedActivity(DetectedActivity.UNKNOWN, 6);
            arrayList.add(d);
            d = new DetectedActivity(DetectedActivity.TILTING, 4);
            arrayList.add(d);
        } else if (resultType == RESULT_TILTING) {
            d = new DetectedActivity(DetectedActivity.UNKNOWN, 6);
            arrayList.add(d);
            d = new DetectedActivity(DetectedActivity.STILL, 4);
            arrayList.add(d);
        } else {
            d = new DetectedActivity(DetectedActivity.TILTING, 6);
            arrayList.add(d);
            d = new DetectedActivity(DetectedActivity.STILL, 4);
            arrayList.add(d);
        }

        return new ActivityRecognitionResult(arrayList, System.currentTimeMillis(), System.nanoTime());
    }
}
