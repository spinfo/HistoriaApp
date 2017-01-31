package de.smarthistory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.smarthistory.data.Mapstop;
import de.smarthistory.data.Tour;

public abstract class TourViewsHelper {


    public static void injectTourDataIntoTourMetaView(final View tourMetaView, final Tour tour) {
        // get the text views to be populated
        TextView nameView = (TextView) tourMetaView.findViewById(R.id.tour_meta_name);
        TextView essentialsView = (TextView) tourMetaView.findViewById(R.id.tour_meta_essentials);
        TextView tagView = (TextView) tourMetaView.findViewById(R.id.tour_meta_tags);

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
    }

    // set the "from"-text in a tour introduction
    public static void setFromTextInTourIntro(View tourIntro, Tour tour) {
        final TextView textView = (TextView) tourIntro.findViewById(R.id.tour_intro_from);

        Calendar cal = Calendar.getInstance();
        cal.setTime(tour.getCreatedAt());

        final String text = String.format(Locale.getDefault(), "Von: %s, %02d.%02d.%d",
                tour.getAuthor(),
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
        textView.setText(text);
    }

    // set the introductory text in a tour introduction
    public static void setIntroductionTextInTourIntro(View tourIntro, Tour tour) {
        final TextView textView = (TextView) tourIntro.findViewById(R.id.tour_intro_introduction);
        textView.setText(tour.getIntroduction());
    }


    public static void setMapstopMiniInfoAdapterForTourIntro(final View tourIntro, final Tour tour) {
        final ListView mapstopListView =
                (ListView) tourIntro.findViewById(R.id.tour_intro_mapstop_list);

        final ArrayList<Mapstop> mapstops = new ArrayList<>(tour.getMapstops());

        final MapstopMiniInfoAdaper adapter =
                new MapstopMiniInfoAdaper(mapstopListView.getContext(), mapstops);
        mapstopListView.setAdapter(adapter);
    }


    // an ArrayAdapter for a short list of tours shown in a tour introduction
    private static class MapstopMiniInfoAdaper extends ArrayAdapter<Mapstop> {

        private final Context context;

        private MapstopMiniInfoAdaper(final Context context, final ArrayList<Mapstop> mapstops) {
            super(context, 0, mapstops);
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            Mapstop mapstop = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.mapstop_mini_info, null);
            }

            TextView textView = (TextView) convertView;
            String text = String.format(Locale.getDefault(), "%02d. %s",
                position + 1, mapstop.getTitle());
            textView.setText(text);

            return textView;
        }
    }

}
