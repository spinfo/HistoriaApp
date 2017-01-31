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

        TourViewsHelper.injectTourDataIntoTourMetaView(convertView, tour);

        // all done
        return convertView;
    }
}
