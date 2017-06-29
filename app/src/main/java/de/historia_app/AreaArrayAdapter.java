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

import de.historia_app.data.Area;
import de.historia_app.data.DataFacade;

/**
 * Displays a list of Areas
 */
public class AreaArrayAdapter extends ArrayAdapter<Area> {

    private static final String LOGTAG = AreaArrayAdapter.class.getSimpleName();

    private final Context context;

    private final DataFacade data;

    public AreaArrayAdapter(Context context, ArrayList<Area> areas) {
        super(context, 0, areas);
        this.context = context;
        this.data = new DataFacade(context);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // the area to be displayed
        final Area area = super.getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.area_meta, null);
        }

        // The view will consist of a title and a display of the amount of tours
        final String title;
        final long numTours;
        if(area == null) {
            ErrUtil.failInDebug(LOGTAG, "Cannot determine view content for null area");
            title = "-";
            numTours = 0;
        } else {
            title = area.getName();
            numTours = this.data.getToursAmount(area);
        }

        final TextView tvTitle = (TextView) convertView.findViewById(R.id.area_meta_title_line);
        final TextView tvDescription = (TextView) convertView.findViewById(R.id.area_meta_description);

        tvTitle.setText(title);
        final String template = context.getString(R.string.tour_amount_template);
        tvDescription.setText(String.format(Locale.getDefault(), template, numTours));

        return convertView;
    }
}
