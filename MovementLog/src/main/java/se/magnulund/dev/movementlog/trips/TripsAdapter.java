package se.magnulund.dev.movementlog.trips;// Created by Gustav on 20/01/2014.

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.utils.DateTimeUtil;
import se.magnulund.dev.movementlog.utils.mIntentBuilder;

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
        Button showStartButton;
        Button showEndButton;
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
            holder.showStartButton = (Button) view.findViewById(R.id.start_location_button);
            holder.showEndButton = (Button) view.findViewById(R.id.end_location_button);

            view.setTag(holder);
        }

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        final Context mContext = context;
        try {

            final Trip trip = Trip.fromCursor(cursor);
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

                if (trip.hasStartCoords()) {

                    holder.showStartButton.setEnabled(true);
                    holder.showStartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = mIntentBuilder.getMapsIntent("Trip start", trip.getStartCoords());
                            mContext.startActivity(intent);
                        }
                    });
                } else {
                    holder.showStartButton.setEnabled(false);
                }

                if (trip.hasEndCoords()) {
                    holder.showEndButton.setEnabled(true);
                    holder.showEndButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = mIntentBuilder.getMapsIntent("Trip end", trip.getEndCoords());
                            mContext.startActivity(intent);
                        }
                    });
                } else {
                    holder.showEndButton.setEnabled(false);
                }

            } else {

                holder.startTimeTextView.setText(DateTimeUtil.getDateTimeString(trip.getStartTime(), DateTimeUtil.TIME_HOUR_MINUTE) + " - " + context.getString(R.string.trip_unconfirmed));
                holder.durationTextView.setText("");
                holder.endTimeTextView.setText("");
                holder.showStartButton.setEnabled(false);
                holder.showEndButton.setEnabled(false);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
