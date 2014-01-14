package se.magnulund.dev.movementlog;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by erikeelde on 17/12/2013.
 */
public class MainFragment extends Fragment {

    private MainFragmentListener mListener;

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
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        assert activity instanceof MainFragmentListener;
        mListener = (MainFragmentListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        rootView.findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.startButtonClicked();
            }
        });

        rootView.findViewById(R.id.stop_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.stopButtonClicked();
            }
        });

        return rootView;
    }

    public interface MainFragmentListener {
        void stopButtonClicked();

        void startButtonClicked();
    }

}
