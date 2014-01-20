package se.magnulund.dev.movementlog;

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

import se.magnulund.dev.movementlog.provider.MovementDataContract;

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

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle arguments = null; // new Bundle();
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
                uri = MovementDataContract.Trips.CONTENT_URI;

                projection = MovementDataContract.Trips.DEFAULT_PROJECTION;

                selection = null;

                selectionArgs = null;

                sortOrder = MovementDataContract.Trips.DEFAULT_SORT_ORDER;
                break;
            case TYPE_RAWDATA:
                uri = MovementDataContract.RawData.CONTENT_URI;

                projection = MovementDataContract.RawData.DEFAULT_PROJECTION;

                selection = MovementDataContract.RawData.CONFIDENCE_RANK + " = ?";

                selectionArgs = new String[]{"0"};

                sortOrder = MovementDataContract.RawData.DEFAULT_SORT_ORDER;
                break;
            default:
                return null;
        }

        return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface MainFragmentListener {
        void stopButtonClicked();

        void startButtonClicked();
    }

}
