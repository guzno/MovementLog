package se.magnulund.dev.movementlog.fragments;

import android.content.Context; // Changed from android.app.Activity
import androidx.fragment.app.Fragment; // Changed from android.app.Fragment
import androidx.loader.app.LoaderManager; // Changed from android.app.LoaderManager
import androidx.loader.content.CursorLoader; // Changed from android.content.CursorLoader
import androidx.loader.content.Loader; // Changed from android.content.Loader
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;

import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.contracts.RawDataContract;
import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.rawdata.RawDataAdapter;
import se.magnulund.dev.movementlog.trips.Trip;
import se.magnulund.dev.movementlog.trips.TripsAdapter;

// Removed static imports for R.string as they might not be necessary or could be simplified
// import static se.magnulund.dev.movementlog.R.string.rawdata_title;
// import static se.magnulund.dev.movementlog.R.string.trips_title;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MainFragmentListener mListener;

    public static final String FRAGMENT_TYPE = "fragment_type";

    public static final int TYPE_RAWDATA = 0;
    public static final int TYPE_TRIPS = 1;

    CursorAdapter mAdapter;

    public static MainFragment newInstance(Bundle arguments) {
        MainFragment fragment = new MainFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        setHasOptionsMenu(true);

        // LoaderManager loaderManager = getLoaderManager(); // Old way
        if (arguments != null) { // arguments needed for initLoader
             LoaderManager.getInstance(this).initLoader(R.id.main_fragment_loader, arguments, this);
        }


        int titleID;

        if (arguments != null) {
            switch (arguments.getInt(FRAGMENT_TYPE)) {
                case TYPE_TRIPS:
                    titleID = R.string.trips_title; // Use R.string directly
                    mAdapter = new TripsAdapter(requireContext(), null, 0); // Use requireContext()
                    break;
                case TYPE_RAWDATA:
                default:
                    titleID = R.string.rawdata_title; // Use R.string directly
                    mAdapter = new RawDataAdapter(requireContext(), null, 0); // Use requireContext()
            }
        } else {
            mAdapter = new RawDataAdapter(requireContext(), null, 0); // Use requireContext()
            titleID = -1; // Or some default title
        }

        if (getActivity() != null && titleID != -1) { // Check titleID for valid resource
            getActivity().setTitle(titleID);
        }
    }

    @Override
    public void onAttach(Context context) { // Changed from Activity to Context
        super.onAttach(context);
        if (context instanceof MainFragmentListener) {
            mListener = (MainFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MainFragmentListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_button:
                mListener.startButtonClicked();
                break;
            case R.id.stop_button:
                mListener.stopButtonClicked();
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (rootView != null) {
            ListView listView = (ListView) rootView.findViewById(R.id.listview);
            listView.setAdapter(mAdapter);
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uri;

        String[] projection;

        String selection;

        String[] selectionArgs;

        String sortOrder;

        if (args == null) { // Guard against null args
            return null;
        }

        switch (args.getInt(FRAGMENT_TYPE)) {
            case TYPE_TRIPS:
                uri = TripLogContract.CONTENT_URI;

                projection = TripLogContract.DEFAULT_PROJECTION;

                selection = TripLogContract.Columns.CONFIRMED_AS + " > ?";

                selectionArgs = new String[]{Integer.toString(Trip.TRIP_CONFIRMED_AS_INCORRECT)};

                sortOrder = TripLogContract.DEFAULT_SORT_ORDER;
                break;
            case TYPE_RAWDATA:
                uri = RawDataContract.CONTENT_URI;

                projection = RawDataContract.DEFAULT_PROJECTION;

                selection = RawDataContract.Columns.CONFIDENCE_RANK + " = ?";

                selectionArgs = new String[]{"0"};

                sortOrder = RawDataContract.DEFAULT_SORT_ORDER;
                break;
            default:
                return null;
        }

        return new CursorLoader(requireContext(), uri, projection, selection, selectionArgs, sortOrder); // Use requireContext()
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor oldCursor = mAdapter.swapCursor(data);
        mAdapter.notifyDataSetChanged();
        if (oldCursor != null){
            oldCursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Cursor oldCursor = mAdapter.swapCursor(null);

        if (oldCursor != null){
            oldCursor.close();
        }
    }

    public interface MainFragmentListener {
        void stopButtonClicked();

        void startButtonClicked();
    }

}
