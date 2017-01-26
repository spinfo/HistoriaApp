package de.smarthistory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.smarthistory.data.Tour;


public class TourArrayAdapter extends ArrayAdapter<Tour> {

    private final Context context;

    public TourArrayAdapter(Context context, Tour[] data) {
        super(context, 0, data);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // the tour to be displayed
        Tour tour = getItem(position);

        // if a view is not reused by the adapter, create one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tour_meta, parent, false);
        }

        // get the text views to be populated
        TextView nameView = (TextView) convertView.findViewById(R.id.tour_meta_name);
        TextView essentialsView = (TextView) convertView.findViewById(R.id.tour_meta_essentials);
        TextView tagView = (TextView) convertView.findViewById(R.id.tour_meta_tags);

        // set name
        nameView.setText(tour.getName());

        // set line with essentials
        String essentials = String.format(Locale.getDefault(),
                "%s, %d min., %.2f km (%s)",
                tour.getType().toString(), tour.getDuration(),
                tour.getWalkLength() / 1000.0, tour.getAccessibility());
        essentialsView.setText(essentials);

        // set line with tags
        String tags = String.format(Locale.getDefault(),
                "%s - %s - %s",
                tour.getTagWhat(), tour.getTagWhen(), tour.getTagWhere());
        tagView.setText(tags);

        // all done
        return convertView;
    }
}
