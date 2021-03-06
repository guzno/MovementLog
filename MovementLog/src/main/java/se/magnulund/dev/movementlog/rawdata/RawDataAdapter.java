package se.magnulund.dev.movementlog.rawdata;// Created by Gustav on 19/01/2014.

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

public class RawDataAdapter extends CursorAdapter {
    private static final String TAG = "RawDataAdapter";

    public RawDataAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private class ViewHolder {
        TextView timeTextView;
        TextView typeTextView;
        TextView confidenceTextView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.raw_data_list_item, parent, false);

        if (view != null) {

            ViewHolder holder = new ViewHolder();

            holder.timeTextView = (TextView) view.findViewById(R.id.raw_data_time);
            holder.typeTextView = (TextView) view.findViewById(R.id.raw_data_type);
            holder.confidenceTextView = (TextView) view.findViewById(R.id.raw_data_confidence);

            view.setTag(holder);
        }
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        RawData rawData = RawData.fromCursor(cursor);

        holder.typeTextView.setText(rawData.getActivityName());

        holder.confidenceTextView.setText(Integer.toString(rawData.getConfidence()) + context.getString(R.string.rawdata_pct_confidence));

        Date date = new Date(rawData.getTimestamp());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d - kk:mm:ss");

        String time = dateFormat.format(date);

        holder.timeTextView.setText(time);
    }
}
