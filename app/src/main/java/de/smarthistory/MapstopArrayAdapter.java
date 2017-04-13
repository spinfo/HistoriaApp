package de.smarthistory;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Locale;

import de.smarthistory.data.Mapstop;

public class MapstopArrayAdapter extends ArrayAdapter<Mapstop> {

    private final Context context;

    public MapstopArrayAdapter(Context context, Mapstop[] data) {
        super(context, 0, data);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // the mapstop to be displayed
        final Mapstop mapstop = getItem(position);

        // if a view is not reused by the adapter, create one
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mapstop_meta, null);
        }

        // get the text views to be populated
        TextView titleLineView = (TextView) convertView.findViewById(R.id.mapstop_meta_title_line);
        TextView descriptionView = (TextView) convertView.findViewById(R.id.mapstop_meta_description);

        // set title
        String posString = (new Integer(position + 1)).toString();
        String titleLine = String.format(Locale.getDefault(), "%s. %s (%s)",
                posString, mapstop.getName(), mapstop.getPlace().getName());
        SpannableStringBuilder sb = new SpannableStringBuilder(titleLine);
        StyleSpan bold = new StyleSpan(Typeface.BOLD);
        int titleEnd = posString.length() + 2 + mapstop.getName().length();
        sb.setSpan(bold, 0, titleEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        titleLineView.setText(sb);

        // set description
        descriptionView.setText(mapstop.getDescription());

        return convertView;
    }
}
