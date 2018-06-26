package de.historia_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.historia_app.data.Area;
import de.historia_app.data.DataFacade;
import de.historia_app.data.Lexicon;
import de.historia_app.data.LexiconEntry;
import de.historia_app.data.Mapstop;
import de.historia_app.data.Tour;


/**
 * Fragment for the "Lesemodus".
 */
public class ExploreDataFragment extends Fragment implements MainActivity.MainActivityFragment {

    private static final String LOGTAG = ExploreDataFragment.class.getSimpleName();

    private DataFacade data;

    private TabHost tabHost;

    private static final int TAP_POS_LEXICON = 0;
    private static final int TAB_POS_FAVORITES = 1;
    private static final int TAB_POS_TOURS = 2;

    public ExploreDataFragment() {
    }

    // these allow to navigate back to tour list from mapstop list
    private TourArrayAdapter tourAdapter;
    private ListView tourOrMapstopList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.data = new DataFacade(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_explore_data, container, false);

        // setup the tab host
        tabHost = (TabHost) view.findViewById(R.id.tabHost);
        tabHost.setup();

        // Tab1: Touren
        TabSpec tabSpec1 = tabHost.newTabSpec(getString(R.string.tours));
        tabSpec1.setContent(R.id.tourOrMapstopTab);
        tabSpec1.setIndicator(getString(R.string.tours));
        tabHost.addTab(tabSpec1);

        // the list view that will contain tours as well as mapstops
        tourOrMapstopList = (ListView) view.findViewById(R.id.tour_or_mapstop_list);

        // setup listener that either:
        //      * changes the list to mapstop on click on a tour
        //      * or displays mapstops on click on a mapstop
        final AdapterView.OnItemClickListener onTourOrMapstopClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Object item = parent.getItemAtPosition(position);
                if (item instanceof Mapstop) {
                    final Intent intent = new Intent(getActivity(), MapstopPageViewActivity.class);
                    intent.putExtra(getString(R.string.extra_key_mapstop_id), ((Mapstop) item).getId());
                    getActivity().startActivity(intent);
                } else if (item instanceof Tour) {
                    tourOrMapstopList.setAdapter(getMapstopListAdapterForTour((Tour) item));
                }
            }
        };

        // setup the list view with tours initially, use the area defined in preferences or db
        final Area area = MapStatePersistence.getArea(getPrefs(), data);
        final Tour[] tours;
        if(area != null) {
            List<Tour> tourData = area.getTours();
            tours = tourData.toArray(new Tour[tourData.size()]);
        } else {
            ErrUtil.failInDebug(LOGTAG, "Cannot determine area to use.");
            tours = new Tour[0];
        }
        tourAdapter = new TourArrayAdapter(getContext(), tours);
        tourOrMapstopList.setAdapter(tourAdapter);
        tourOrMapstopList.setOnItemClickListener(onTourOrMapstopClickListener);

        // Tab2: Lexikon
        TabSpec tabSpec2 = tabHost.newTabSpec("Lexikon");
        tabSpec2.setContent(R.id.lexiconTab);
        tabSpec2.setIndicator("Lexikon");
        tabHost.addTab(tabSpec2);

        Lexicon lexicon = data.getLexicon();

        if(lexicon.hasEntries()) {
            // remove the anouncement that no entries are present
            View noEntriesView = view.findViewById(R.id.no_lexicon_entries);
            ((ViewManager)noEntriesView.getParent()).removeView(noEntriesView);

            // fill the list of entries
            ArrayList<Object> lexData = LexiconAdapter.makeData(lexicon);
            LexiconAdapter lexiconAdapter = new LexiconAdapter(getContext(), lexData);
            ListView lexiconList = (ListView) view.findViewById(R.id.lexicon_list);
            lexiconList.setAdapter(lexiconAdapter);
            lexiconList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Object obj = parent.getItemAtPosition(position);
                    // react to click on lexicon entries by showing the activity, else do nothing
                    if (LexiconEntry.class.equals(obj.getClass())) {
                        Intent intent = new Intent(getActivity(), SimpleWebViewActivity.class);

                        // put lexicon entry content into the intent and render by the web view activity
                        final String content = ((LexiconEntry) obj).getContent();
                        intent.putExtra(getResources().getString(R.string.extra_key_simple_web_view_data), content);
                        getActivity().startActivity(intent);
                    }
                }
            });
        }

        return view;
    }

    private ListAdapter getMapstopListAdapterForTour(Tour tour) {
        List<Mapstop> mapstopData = tour.getMapstops();
        if (tour.isIndoor()) {
            Collections.sort(mapstopData, new Comparator<Mapstop>() {
                @Override
                public int compare(Mapstop o1, Mapstop o2) {
                    if (o1.getScene().getPos() == o2.getScene().getPos()) {
                        return o1.getPos() - o2.getPos();
                    }

                    return o1.getScene().getPos() - o2.getScene().getPos();
                }
            });
        }
        Mapstop[] mapstops = mapstopData.toArray(new Mapstop[mapstopData.size()]);
        return new MapstopArrayAdapter(getContext(), mapstops);
    }

    @Override
    public boolean reactToBackButtonPressed() {
        // default result is false to indicate that the button press was not handled
        boolean result = false;
        // if we are reading the tours mapstop and back is pressed, navigate to tours again
        if (tabHost != null && tabHost.getCurrentTab() == TAB_POS_TOURS) {
            if(tourOrMapstopList != null && tourOrMapstopList.getAdapter() != tourAdapter) {
                tourOrMapstopList.setAdapter(tourAdapter);
                result = true;
            }
        }
        return result;
    }

    // Convenience method to get SharedPreferences in a standard way
    private SharedPreferences getPrefs() {
        return getActivity().getPreferences(Context.MODE_PRIVATE);
    }
}
