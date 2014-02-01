package se.magnulund.dev.movementlog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import se.magnulund.dev.movementlog.fragments.MainFragment;
import se.magnulund.dev.movementlog.services.ActivityRecognitionService;
import se.magnulund.dev.movementlog.services.LocationRequestService;

public class MainActivity extends Activity implements MainFragment.MainFragmentListener {

    private static final String TAG = "MainActivity";

    private int currentFragment;

    private MainFragment rawDataFragment;
    private MainFragment tripsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();

            arguments.putInt(MainFragment.FRAGMENT_TYPE, MainFragment.TYPE_TRIPS);

            tripsFragment = MainFragment.newInstance(arguments);

            getFragmentManager().beginTransaction().add(R.id.container, tripsFragment).commit();

            currentFragment = MainFragment.TYPE_TRIPS;
        }

    }

    private void switchFragment() {
        int newFragmentType = -1;
        MainFragment newFragment;

        switch (currentFragment) { // check which fragment is currently shown
            case MainFragment.TYPE_RAWDATA:
                newFragmentType = MainFragment.TYPE_TRIPS;
                newFragment = tripsFragment;
                break;
            case MainFragment.TYPE_TRIPS:
                newFragmentType = MainFragment.TYPE_RAWDATA;
                newFragment = rawDataFragment;
                break;
            default:
                newFragment = null;
        }
        if (newFragmentType >= 0) {
            if (newFragment == null) { // create fragment if we don't have one of the correct type exists,

                Bundle arguments = new Bundle();

                arguments.putInt(MainFragment.FRAGMENT_TYPE, newFragmentType);

                newFragment = MainFragment.newInstance(arguments);

                switch (newFragmentType) {
                    case MainFragment.TYPE_RAWDATA:
                        rawDataFragment = newFragment;
                        break;
                    case MainFragment.TYPE_TRIPS:
                        tripsFragment = newFragment;
                        break;
                }
            }

            // replace fragment
            getFragmentManager().beginTransaction().replace(R.id.container, newFragment).commit();

            currentFragment = newFragmentType;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class), null);
                break;
            case R.id.switch_fragment:
                switchFragment();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void stopButtonClicked() {
        Intent startRecognitionIntent = ActivityRecognitionService.getStopIntent(this);
        startService(startRecognitionIntent);
        Intent startLocationIntent = LocationRequestService.getStartUpdatesIntent(this);
        startService(startLocationIntent);
    }

    @Override
    public void startButtonClicked() {
        Intent startIntent = ActivityRecognitionService.getStartIntent(this);
        startService(startIntent);
        Intent stopLocationIntent = LocationRequestService.getStopUpdatesIntent(this);
        startService(stopLocationIntent);
    }

}
