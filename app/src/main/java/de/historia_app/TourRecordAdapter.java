package de.historia_app;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.historia_app.data.TourRecord;

public class TourRecordAdapter extends ArrayAdapter<TourRecord> {

    public TourRecordAdapter(Context context, ArrayList<TourRecord> records) {
        super(context, 0, records);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // the record to be shown
        TourRecord record = super.getItem(position);

        // if we do not have a view to convert, get one
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tour_record_meta, parent, false);
        }

        // set the tour title on the title view
        TextView nameView = (TextView) convertView.findViewById(R.id.tour_record_name);
        nameView.setText(record.getName());

        // set the other record infos on the essentials View
        StringBuilder sb = new StringBuilder();
        sb.append(record.getAreaName());
        sb.append(" - (");
        sb.append(String.format(Locale.getDefault(), "%.2f", (record.getDownloadSize() / 1000000.0)));
        sb.append(" MB)");
        TextView essentialsView = (TextView) convertView.findViewById(R.id.tour_record_essentials);
        essentialsView.setText(sb.toString());

        return convertView;
    }
}
