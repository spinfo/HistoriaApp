package de.smarthistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.smarthistory.data.Area;
import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;
import de.smarthistory.data.Tour;
import de.smarthistory.data.TourOnMap;


/**
 * The fragment handling the map view
 */
public class MapFragment extends Fragment implements MainActivity.MainActivityFragment, OnModelSelectionListener {

    private static final String LOGTAG = MapFragment.class.getSimpleName();

    // the state of the map view that will be persisted on view destruction/after closing the app
    public static class MapState {
        Area area;
        MapView map;
        List<TourOnMap> toursOnMap;
        Mapstop mapstopTapped;
    }
    private MapState state;

    private MapPopupManager popupManager;

    // the interface to retrieve all actual data from
    private DataFacade data = DataFacade.getInstance(getContext());

    // the view that this will instantiate, has to be a FrameLayout for us to be able to dim
    // the map on creating a popup
    private FrameLayout mapFragmentView;

    // if model selection are registered by this fragment they will be passed to this listener
    private OnModelSelectionListener onModelSelectionListener;

    // a simple cache for tour overlays used by this map
    private Map<Tour, List<Overlay>> tourOverlayCache = new HashMap<>();

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

        // initialize the state of this fragment with a new map
        state = new MapState();
        state.map = (MapView) mapFragmentView.findViewById(R.id.map);

        tourOverlayCache = new HashMap<>();
        MapUtil.setMapDefaults(state.map);

        // the object that will manage all popups on this map
        this.popupManager = new MapPopupManager(mapFragmentView);

        // set the map up to close all info windows on a click by providing a custom touch overlay
        final Overlay touchOverlay = new Overlay() {
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
            MapStatePersistence.load(state, getPrefs(), data, this.getContext());
            switchTourOverlays(state.toursOnMap);
            if (state.mapstopTapped != null) {
                reopenInfoWindowFor(state.mapstopTapped);
            }
            Log.d(LOGTAG, "Loaded state from prefs.");
        } catch (MapStatePersistence.InconsistentMapStateException e) {
            Log.d(LOGTAG, "Could not load map state. Will use defaults. Message: " + e.message);
            List<Overlay> tourOverlays = Collections.emptyList();
            final Tour defaultTour = data.getDefaultTour();
            if(defaultTour != null) {
                state.area = defaultTour.getArea();
                tourOverlays = switchTourOverlays(new TourOnMap(defaultTour));
            }
            if(tourOverlays.isEmpty()) {
                Log.w(LOGTAG, "No overlays to zoom to, using default.");
                state.area = data.getDefaultArea();
                MapUtil.zoomToDefaultLocation(state.map);
            } else {
                MapUtil.zoomToOverlays(state.map, tourOverlays);
            }
        }

        // pass the now selected area up to the model selection listener
        if (state.area != null) {
            onModelSelectionListener.onAreaSelected(state.area);
        }

        // recreate popup from bundle if needed
        if (savedInstanceState != null) {
            popupManager.restorePopupStateFrom(savedInstanceState, this);
        }

        // add Overlay for current location
        addCurrentLocation(state.map);

        // return the container for the map view
        return mapFragmentView;
    }

    // Permanently save the basic map view on pause
    @Override
    public void onPause() {
        super.onPause();
        MapStatePersistence.save(state, getPrefs(), data);
    }

    // temporarily save an open popup view in the preferences
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        popupManager.savePopupStateTo(outState);
        // dismiss the active popup, else it will be memory-leaked
        popupManager.dismissActivePopup();
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

    private List<Overlay> getOrMakeTourOverlays(MapView map, Tour tour) {
        if(tour == null) {
            Log.w(LOGTAG, "Not creating tour overlays for null input.");
            return new ArrayList<>();
        }

        // return the cached overly if one has been constructed previously
        if (tourOverlayCache.containsKey(tour)) {
            return tourOverlayCache.get(tour);
        }
        List<Overlay> overlays = new ArrayList<>();

        // the tour's track is drawn with a polyline
        // add this first for it to be drawn before the markers
        final Polyline line = MapUtil.makeEmptyTourTrackPolyline(getContext());
        line.setPoints(tour.getTrackAsGeoPoints());
        overlays.add(line);

        // markers for mapstops share a custom info window
        final MarkerInfoWindow window = new MapFragment.MapstopMarkerInfoWindow(R.layout.map_my_bonuspack_bubble, map);
        Marker marker;
        for (final Mapstop mapstop : tour.getMapstops()) {
            marker = MapUtil.makeMapstopMarker(getContext(), state.map, mapstop);
            marker.setInfoWindow(window);
            overlays.add(marker);
        }

        tourOverlayCache.put(tour, overlays);
        return overlays;
    }

    // switches the current tour to the single tour supplied by getting/creating markers.
    // Returns those markers.
    private List<Overlay> switchTourOverlays(TourOnMap tourOnMap) {
        if (tourOnMap == null || tourOnMap.getTour() == null) {
            Log.w(LOGTAG, "Empty tour as input.");
            return Collections.emptyList();
        }

        final List<TourOnMap> substituteList = new ArrayList<TourOnMap>(1);
        substituteList.add(tourOnMap);
        return switchTourOverlays(substituteList);
    }

    // switches the current tour to the tours supplied by getting/creating markers.
    // Returns those markers.
    private List<Overlay> switchTourOverlays(List<TourOnMap> toursOnMap) {
        final List<Overlay> markers = new ArrayList<>();

        // remove old markers
        // we cannot clear() the whole list because of the touch overlay registering general clicks
        // to the map.
        if (state.toursOnMap != null) {
            for (TourOnMap tourOnMap : toursOnMap) {
                markers.addAll(getOrMakeTourOverlays(state.map, tourOnMap.getTour()));
            }
            state.map.getOverlays().removeAll(markers);
            markers.clear();
        }

        // add new markers
        for(TourOnMap tourOnMap : toursOnMap) {
            markers.addAll(getOrMakeTourOverlays(state.map, tourOnMap.getTour()));
            state.map.getOverlays().addAll(markers);
        }
        state.toursOnMap = toursOnMap;

        // close a possible infowindow belonging to the old tour
        if(state.mapstopTapped != null) {
            InfoWindow.closeAllInfoWindowsOn(state.map);
        }

        return markers;
    }

    // opens the info window belonging to a mapstop marker if the map contains a marker that
    // has the provided mapstop as a related object
    private void reopenInfoWindowFor(Mapstop mapstop) {
        // TODO: there should be a better way to do this, than looping over all overlays
        Object relatedObject;
        for (Overlay overlay : state.map.getOverlays()) {
            // only look at marker overlays
            if (overlay instanceof Marker) {

                final Marker marker = (Marker) overlay;
                relatedObject = marker.getRelatedObject();

                // only look at those markers related to maptstops
                if(relatedObject instanceof  Mapstop) {
                    if(((Mapstop) relatedObject).getId() == mapstop.getId()) {

                        // delay opening the info window after the map view has been rendered
                        state.map.post(new Runnable() {
                            @Override
                            public void run() {
                                marker.showInfoWindow();
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * A class controlling the behaviour of the bubble appearing above mapstop markers on the map
     */
    private class MapstopMarkerInfoWindow extends MarkerInfoWindow {

        private class MapstopMarkerInfoWindowOnclickListner implements View.OnClickListener {

            Mapstop mapstop;

            @Override
            public void onClick(View view) {
                popupManager.showMapstop(this.mapstop);
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
            //opening one window closes all others on the map
            InfoWindow.closeAllInfoWindowsOn(this.map);

            // the mapstap this window refers to is saved as a related object of the marker
            final Mapstop mapstop = (Mapstop) getMarkerReference().getRelatedObject();

            // set the right mapstop to use for the onclick listener
            this.onClickListener.setMapstop(mapstop);

            // mark the mapstop infowindow as opened in map state for persistence
            MapFragment.this.state.mapstopTapped = mapstop;

            // set on click listener for the bubble/infowindow
            LinearLayout layout = (LinearLayout) getView().findViewById(R.id.map_my_bonuspack_bubble);
            layout.setClickable(true);
            layout.setOnClickListener(this.onClickListener);
        }

        @Override
        public void onClose() {
            super.onClose();

            // tell the map state that no mapstop is opened atm
            MapFragment.this.state.mapstopTapped = null;
        }
    }

    public void showTourSelection() {
        if(state.area == null) {
            ErrUtil.failInDebug(LOGTAG, "No area given to select tours for.");
        }
        popupManager.showTourSelection(state.area, this);
    }

    public void showAreaSelection() {
        popupManager.showAreaSelection(this);
    }

    @Override
    public void onTourSelected(Tour tour) {
        if(tour == null) {
            Log.w(LOGTAG, "Not selecting null tour.");
            return;
        }
        List<Overlay> overlays = switchTourOverlays(new TourOnMap(tour));
        MapUtil.zoomToOverlays(state.map, overlays);

        // pass the change on
        if(onModelSelectionListener != null) {
            onModelSelectionListener.onTourSelected(tour);
        }
    }

    @Override
    public void onAreaSelected(Area area) {
        // abort if the area is null or has no tours connected
        if(area == null || area.getTours() == null || area.getTours().isEmpty()) {
            Log.w(LOGTAG, "Not selecting null or empty area.");
            return;
        }
        // set the area in the state
        state.area = area;
        // show the tours of the area
        List<TourOnMap> toursOnMap = new ArrayList<>();
        for(Tour tour : area.getTours()) {
            toursOnMap.add(new TourOnMap(tour));
        }
        List<Overlay> overlays = switchTourOverlays(toursOnMap);
        MapUtil.zoomToOverlays(state.map, overlays);

        // pass the change on
        if(onModelSelectionListener != null) {
            onModelSelectionListener.onAreaSelected(area);
        }
    }

    public void setOnModelSelectionListener(OnModelSelectionListener listener) {
        this.onModelSelectionListener = listener;
    }

    @Override
    public boolean reactToBackButtonPressed() {
        return false;
    }
}
