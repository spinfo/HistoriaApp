package de.historia_app;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.historia_app.data.DataFacade;
import de.historia_app.data.TourRecord;
import de.historia_app.data.TourRecord.InstallStatus;

public class TourRecordAdapter extends ArrayAdapter<TourRecord> {

    private final DataFacade data;

    private final Context context;

    public TourRecordAdapter(Context context, ArrayList<TourRecord> records) {
        super(context, 0, records);
        this.context = context;
        this.data = new DataFacade(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // the record to be shown
        TourRecord record = super.getItem(position);
        InstallStatus status = data.determineInstallStatus(record);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tour_record_meta, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.tour_record_icon);
        icon.setImageResource(determineIconFor(status));

        final Spinner spinner = (Spinner) convertView.findViewById(R.id.tour_record_spinner);
        String[] texts = { "bla", "blubb" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(texts);
        spinner.setAdapter(adapter);

        View iconView = convertView.findViewById(R.id.tour_record_icon_area);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });

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

    private int determineIconFor(InstallStatus status) {
        int result;
        switch (status) {
            case NOT_INSTALLED:
                result = R.drawable.download_circular_button_symbol;
                break;
            case UP_TO_DATE:
                result = R.drawable.verification_sign_in_a_circle_outline;
                break;
            case UPDATE_AVAILABLE:
                result = R.drawable.circular_arrow_with_clockwise_rotation;
                break;
            default:
                throw new RuntimeException("Unknown enum value: " + status.name());
        }
        return result;
    }
}
