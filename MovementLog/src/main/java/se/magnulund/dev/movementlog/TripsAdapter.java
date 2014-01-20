package se.magnulund.dev.movementlog;// Created by Gustav on 20/01/2014.

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripsAdapter extends CursorAdapter {
    private static final String TAG = "TripsAdapter";

    public TripsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private class ViewHolder {
        TextView startTimeTextView;
        TextView endTimeTextView;
        TextView typeTextView;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.trip_list_item, parent, false);

        if (view != null) {

            ViewHolder holder = new ViewHolder();

            holder.startTimeTextView = (TextView) view.findViewById(R.id.trip_starttime);
            holder.typeTextView = (TextView) view.findViewById(R.id.trip_type);
            holder.endTimeTextView = (TextView) view.findViewById(R.id.trip_endtime);

            view.setTag(holder);
        }

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        Trip trip;
        try {
            trip = Trip.fromCursor(cursor);
            holder.typeTextView.setText(trip.getTripTypeName());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm H:mm:ss", Locale.getDefault());

            Date startTime = new Date(trip.getStartTime());

            holder.startTimeTextView.setText(dateFormat.format(startTime));

            Date endTime = new Date(trip.getEndTime());

            holder.endTimeTextView.setText(dateFormat.format(endTime));

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
