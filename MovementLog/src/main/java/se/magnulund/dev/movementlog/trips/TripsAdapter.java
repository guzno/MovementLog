package se.magnulund.dev.movementlog.trips;// Created by Gustav on 20/01/2014.

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.utils.DateTimeUtil;

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
            holder.typeTextView.setText(trip.getTripTypeNameResourceID());

            holder.dateTextView.setText(DateTimeUtil.getDateTimeString(trip.getStartTime(), DateTimeUtil.DATE_MONTHNAME_DAY));

            if (trip.getStartConfirmedByID() >= 0) {

                holder.startTimeTextView.setText(DateTimeUtil.getDateTimeString(trip.getStartTime(), DateTimeUtil.TIME_HOUR_MINUTE));

                // -- TODO calculate duration
                holder.durationTextView.setText(DateTimeUtil.getDurationString(trip.getStartTime(), trip.getEndTime(), false));

                if (trip.getEndConfirmedByID() >= 0) {

                    holder.endTimeTextView.setText(DateTimeUtil.getDateTimeString(trip.getEndTime(), DateTimeUtil.TIME_HOUR_MINUTE));

                } else {

                    holder.endTimeTextView.setText(context.getString(R.string.trip_ongoing));

                }

            } else {

                holder.startTimeTextView.setText(DateTimeUtil.getDateTimeString(trip.getStartTime(), DateTimeUtil.TIME_HOUR_MINUTE) + " - " + context.getString(R.string.trip_unconfirmed));
                holder.durationTextView.setText("");
                holder.endTimeTextView.setText("");

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
