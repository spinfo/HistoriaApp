package de.smarthistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.smarthistory.data.Area;
import de.smarthistory.data.DataFacade;
import de.smarthistory.data.MapUtil;
import de.smarthistory.data.Mapstop;
import de.smarthistory.data.Tour;


/**
 * The fragment handling the map view
 */
public class MapFragment extends Fragment implements MainActivity.MainActivityFragment {

    public static class MapState {
        MapView map;
        Tour currentTour;
    }

    private Logger LOGGER = Logger.getLogger(MapFragment.class.getName());

    private DataFacade data = DataFacade.getInstance();

    // the state of the map this fragment handles can be saved/restored via this object
    private MapState state;

    // the view that this will instantiate, has to be a FrameLayout for us to be able to dim
    // the map on creating a popup
    private FrameLayout mapFragmentView;

    // a simple cache for tour markers used by this map
    private Map<Tour, List<Marker>> tourMarkerCache = new HashMap<>();

    // dimensions for popups over the map
    // TODO put in some config for changeability on different screen types
    private static final float POPUP_WIDTH = 0.85f;
    private static final float POPUP_HEIGHT = 0.90f;

    public MapFragment() {
        // Required empty public constructor
    }

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        // Inflate the layout for this fragment
        mapFragmentView = (FrameLayout) inflater.inflate(R.layout.fragment_map, container, false);

        // we have a foreground in place to grey out the map if needed. But at first the map should
        // be fully visible
        mapFragmentView.getForeground().setAlpha(0);

        // initialize the state of this fragment with a new map
        // TODO: save some of these in bundle if possible
        state = new MapState();
        state.map = (MapView) mapFragmentView.findViewById(R.id.map);
        tourMarkerCache = new HashMap<>();
        MapUtil.setMapDefaults(state.map);

        // set the map up to close all info windows on a click by providing a custom touch overlay
        Overlay touchOverlay = new Overlay() {
            @Override
            public void draw(Canvas c, MapView osmv, boolean shadow) {
                // do nothing
            }
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                InfoWindow.closeAllInfoWindowsOn(state.map);
                return super.onSingleTapConfirmed(e, mapView);
            }
        };
        state.map.getOverlays().add(touchOverlay);

        // initialize from saved preferences or else start with the default tour
        try {
            MapStatePersistence.load(state, getPrefs());
            switchTour(state.currentTour);
            LOGGER.info("Loaded state from prefs.");
        } catch (MapStatePersistence.InconsistentMapStateException e) {
            LOGGER.info("Could not load map state. Will use defaults. Message: " + e.message);
            switchTour(data.getCurrentTour());
            MapUtil.zoomToMarkers(state.map, tourMarkerCache.get(data.getCurrentTour()));
        }

        // add Overlay for current location
        addCurrentLocation(state.map);

        // return the container for the map view
        return mapFragmentView;
    }

    @Override
    public void onPause() {
        super.onPause();
        MapStatePersistence.save(state, getPrefs());
    }

    // Convenience method to get SharedPreferences in a standard way
    private SharedPreferences getPrefs() {
        return getActivity().getPreferences(Context.MODE_PRIVATE);
    }


    private void addCurrentLocation(MapView map) {
        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(getContext());
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(locationProvider, map);

        map.getOverlays().add(myLocationOverlay);
        myLocationOverlay.enableMyLocation();
    }

    private List<Marker> getOrMakeTourMarkers(MapView map, Tour tour) {
        if (tourMarkerCache.containsKey(tour)) {
            return tourMarkerCache.get(tour);
        }
        List<Marker> markers = new ArrayList<>();

        // markers have a custom info window
        MarkerInfoWindow window = new MapFragment.MapstopMarkerInfoWindow(R.layout.map_my_bonuspack_bubble, map);

        for (Mapstop mapstop : tour.getMapstops()) {
            Marker marker = new Marker(map);

            marker.setPosition(mapstop.getPlace().getLocation());
            marker.setTitle(mapstop.getTitle());
            marker.setSubDescription(mapstop.getShortDescription());
            marker.setRelatedObject(mapstop);

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(ContextCompat.getDrawable(getContext().getApplicationContext(), R.drawable.map_marker_icon_blue_small));
            marker.setInfoWindow(window);

            markers.add(marker);
        }
        tourMarkerCache.put(tour, markers);
        return markers;
    }

    // switch the current tour by getting/creating markers. Return those markers.
    private List<Marker> switchTour(Tour tour) {
        List<Marker> markers;

        // remove old markers
        if (state.currentTour != null) {
            markers = getOrMakeTourMarkers(state.map, state.currentTour);
            state.map.getOverlays().removeAll(markers);
        }

        // add new markers
        markers = getOrMakeTourMarkers(state.map, tour);
        state.map.getOverlays().addAll(markers);
        state.currentTour = tour;

        return markers;
    }

    /**
     * A class controlling the behaviour of the bubble appearing above mapstop markers on the map
     */
    private class MapstopMarkerInfoWindow extends MarkerInfoWindow {

        private class MapstopMarkerInfoWindowOnclickListner implements View.OnClickListener {

            Mapstop mapstop;

            @Override
            public void onClick(View view) {
                showMapstop(this.mapstop);
            }

            public void setMapstop(Mapstop mapstop) {
                this.mapstop = mapstop;
            }
        };

        MapFragment.MapstopMarkerInfoWindow.MapstopMarkerInfoWindowOnclickListner onClickListener;

        MapView map;

        public MapstopMarkerInfoWindow(int layoutResId, final MapView mapView) {
            super(layoutResId, mapView);
            this.map = mapView;
            this.onClickListener = new MapFragment.MapstopMarkerInfoWindow.MapstopMarkerInfoWindowOnclickListner();
        }

        @Override
        public void onOpen(Object item) {
            super.onOpen(item);
            closeAllInfoWindowsOn(this.map);

            // set the right mapstop to use
            this.onClickListener.setMapstop((Mapstop) getMarkerReference().getRelatedObject());

            // set listener for containing view
            LinearLayout layout = (LinearLayout) getView().findViewById(R.id.map_my_bonuspack_bubble);
            layout.setClickable(true);
            layout.setOnClickListener(this.onClickListener);
        }
    }

    private void showMapstop(Mapstop mapstop) {
        // Get the mapstop view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mapstopLayout = inflater.inflate(R.layout.mapstop, null);

        // Bind mapstop view to a page loader
        MapstopPageView pageView = (MapstopPageView) mapstopLayout.findViewById(R.id.mapstop_page);
        TextView pageIndicatorView = (TextView) mapstopLayout.findViewById(R.id.mapstop_page_indicator);
        MapstopPageLoader pageLoader = new MapstopPageLoader(mapstop, pageView, pageIndicatorView);

        // actually display the mapstop as a popup window
        showAsPopup(mapstopLayout, false);
    }

    public void showTourSelection(Area area) {
        ListView listView = (ListView) getActivity().getLayoutInflater().inflate(R.layout.tour_or_mapstop_list, null);
        List<Tour> tourData = area.getTours();
        Tour[] tours = tourData.toArray(new Tour[tourData.size()]);
        TourArrayAdapter toursAdapter = new TourArrayAdapter(getContext(), tours);
        listView.setAdapter(toursAdapter);
        final PopupWindow popupWindow = showAsPopup(listView, false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tour tour = (Tour) parent.getItemAtPosition(position);
                popupWindow.dismiss();
                showTourIntro(tour);
            }
        });
    }

    public void showTourIntro(final Tour tour) {
        View tourIntroView = getActivity().getLayoutInflater().inflate(R.layout.tour_intro, null, false);

        // the tour intro reuses the tour meta view
        View tourMetaView = tourIntroView.findViewById(R.id.tour_meta);
        TourViewsHelper.injectTourDataIntoTourMetaView(tourMetaView, tour);

        // the other three views are filled as well as found by the helper
        TourViewsHelper.setMapstopsInTourIntro(tourIntroView, tour);
        TourViewsHelper.setFromTextInTourIntro(tourIntroView, tour);
        TourViewsHelper.setIntroductionTextInTourIntro(tourIntroView, tour);

        // the whole introduction is shown as a dialog popup
        final PopupWindow window = showAsPopup(tourIntroView, true);

        // the ok buttons of the dialog popup switches to the selected tour
        Button buttonOk = (Button) window.getContentView().findViewById(R.id.map_popup_dialog_button_ok);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Marker> markers = switchTour(tour);
                MapUtil.zoomToMarkers(state.map, markers);
                window.dismiss();
            }
        });

        // the cancel button returns to the tour selection
        Button buttonCancel = (Button) window.getContentView().findViewById(R.id.map_popup_dialog_button_cancel);
        buttonCancel.setText(getString(R.string.dialog_popup_back));
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                showTourSelection(data.getCurrentArea());
            }
        });
    }

    // this displays a view as a popup over the map
    private PopupWindow showAsPopup(View view, boolean asDialog) {
        // determine size for the popup
        int width = (int) (mapFragmentView.getWidth() * POPUP_WIDTH);
        int height = (int) (mapFragmentView.getHeight() * POPUP_HEIGHT);

        View popupContainer = getActivity().getLayoutInflater().inflate(R.layout.map_popup, null);

        // put the supplied view inside the popup view
        RelativeLayout popupContent = (RelativeLayout) popupContainer.findViewById(R.id.map_popup_content);
        popupContent.addView(view);

        // create the popup window
        final PopupWindow popupWindow = new PopupWindow(popupContainer, width, height, true);
        popupWindow.showAtLocation(popupContainer, Gravity.CENTER, 0, 0);

        // get the popup window button and set it to dismiss the popup
        Button button = (Button) popupContainer.findViewById(R.id.map_popup_dismiss);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        // dim the background
        mapFragmentView.getForeground().setAlpha(200);

        // give the popup window a listener to undim the background if dismissed
        popupWindow.setOnDismissListener(new MapFragmentPopupOnDismissListener());

        // remove dialog buttons if this is not supposed to be a dialog
        if (!asDialog) {
            View dialogButtonBar = popupContainer.findViewById(R.id.map_popup_dialog_buttons_bar);
            ((ViewGroup)dialogButtonBar.getParent()).removeView(dialogButtonBar);
        }

        return popupWindow;
    }

    // a listener to change un-dim the map on dismissing a popup
    private class MapFragmentPopupOnDismissListener implements PopupWindow.OnDismissListener {
        public void onDismiss() {
            mapFragmentView.getForeground().setAlpha(0);
        }
    }


    @Override
    public boolean reactToBackButtonPressed() {
        return false;
    }
}
