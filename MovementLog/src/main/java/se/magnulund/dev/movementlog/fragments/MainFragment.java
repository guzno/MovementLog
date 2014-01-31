package se.magnulund.dev.movementlog.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
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

import static se.magnulund.dev.movementlog.R.string.rawdata_title;
import static se.magnulund.dev.movementlog.R.string.trips_title;

/**
 * Created by erikeelde on 17/12/2013.
 */
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

        LoaderManager loaderManager = getLoaderManager();
        if (null != loaderManager) {
            loaderManager.initLoader(R.id.main_fragment_loader, arguments, this);
        }

        int titleID;

        if (arguments != null) {
            switch (arguments.getInt(FRAGMENT_TYPE)) {
                case TYPE_TRIPS:
                    titleID = trips_title;
                    mAdapter = new TripsAdapter(getActivity(), null, 0);
                    break;
                case TYPE_RAWDATA:
                default:
                    titleID = rawdata_title;
                    mAdapter = new RawDataAdapter(getActivity(), null, 0);
            }
        } else {
            mAdapter = new RawDataAdapter(getActivity(), null, 0);
            titleID = -1;
        }

        if (getActivity() != null && titleID >= 0) {
            getActivity().setTitle(titleID);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        assert activity instanceof MainFragmentListener;
        mListener = (MainFragmentListener) activity;
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

        return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
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
