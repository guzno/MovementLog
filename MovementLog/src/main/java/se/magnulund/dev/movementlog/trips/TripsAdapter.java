package se.magnulund.dev.movementlog.trips;// Created by Gustav on 20/01/2014.

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import se.magnulund.dev.movementlog.R;

public class TripsAdapter extends CursorAdapter {
    private static final String TAG = "TripsAdapter";

    public TripsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private class ViewHolder {
        TextView typeTextView;
        TextView startTimeTextView;
        TextView endTimeTextView;
        TextView durationTextView;
        TextView dateTextView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.trip_list_item, parent, false);

        if (view != null) {

            ViewHolder holder = new ViewHolder();

            holder.startTimeTextView = (TextView) view.findViewById(R.id.trip_start_time);
            holder.typeTextView = (TextView) view.findViewById(R.id.trip_type);
            holder.endTimeTextView = (TextView) view.findViewById(R.id.trip_end_time);
            holder.dateTextView = (TextView) view.findViewById(R.id.trip_date);
            holder.durationTextView = (TextView) view.findViewById(R.id.trip_duration);

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
            holder.typeTextView.setText(trip.getTripTypeNameID());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
            SimpleDateFormat timeFormat = new SimpleDateFormat("kk:mm");

            Date startTime = new Date(trip.getStartTime());

            holder.dateTextView.setText(dateFormat.format(startTime));

            holder.startTimeTextView.setText(timeFormat.format(startTime));

            // -- TODO calculate duration
            holder.durationTextView.setText("XX min");

            Date endTime = new Date(trip.getEndTime());

            holder.endTimeTextView.setText(timeFormat.format(endTime));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
