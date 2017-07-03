package de.historia_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
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

import de.historia_app.data.Area;
import de.historia_app.data.DataFacade;
import de.historia_app.data.Mapstop;
import de.historia_app.data.Tour;
import de.historia_app.data.TourOnMap;


/**
 * The fragment handling the map view
 */
public class MapFragment extends Fragment implements MainActivity.MainActivityFragment, OnModelSelectionListener, LocationListener {

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
    private DataFacade data;

    // the view that this will instantiate, has to be a FrameLayout for us to be able to dim
    // the map on creating a popup
    private FrameLayout mapFragmentView;

    // if model selection are registered by this fragment they will be passed to this listener
    private OnModelSelectionListener onModelSelectionListener;

    // a simple cache for tour overlays used by this map
    // TODO: Check if this is really improving performance (on orientation change?)
    private Map<Tour, List<Overlay>> tourOverlayCache = new HashMap<>();

    // the user's location (if recorded)
    private Location userLocation;

    // default empty constructor
    public MapFragment() { }

     @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.data = new DataFacade(getContext());
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

        // setup the activity this fragment was created by as the selection listener
        // NOTE: this is the MainActivity
        if(this.onModelSelectionListener == null) {
            this.onModelSelectionListener = (OnModelSelectionListener) getActivity();
        }

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

        // request location updates from the system
        final LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, this);
        } catch (SecurityException e) {
            Log.d(LOGTAG, "Caught Security exception for GPS update request.");
            // do nothing if the user does not want to be followed
        }

        // return the container for the map view
        return mapFragmentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // add the overlay showing the user's location and enable location options
        addUserLocationOptions(state.map);

        // make the link to the osm license clickable
        createOSMLicenseLink(mapFragmentView.findViewById(R.id.osm_legal_reference));
    }

    // Permanently save the basic map view on pause
    @Override
    public void onPause() {
        super.onPause();
        MapStatePersistence.save(state, getPrefs(), data);
    }

    // This saves data not being needed between complete restarts (e.g. the open popup) and
    // therefore not in the SharedPreferences
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // unlike the main map state, the popup state is saved in a bundle
        popupManager.savePopupStateTo(outState);

        // dismiss the active popup, else it will be memory-leaked
        popupManager.dismissActivePopup();
    }

    // Convenience method to get SharedPreferences in a standard way
    private SharedPreferences getPrefs() {
        return getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    // sets up the view as a link to the open street map license page
    private void createOSMLicenseLink(final View view) {
        Log.i("--->", "Called for view: " + view);
        if(view == null) {
            Log.e(LOGTAG, "Cannot setup osm legal notice");
            return;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("--->", "Clicked...");
                final String url = getString(R.string.osm_copyright_url);
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private void addUserLocationOptions(final MapView map) {
        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(getContext());

        // setup an overlay for the user's location
        final MyLocationNewOverlay userLocationOverlay = new MyLocationNewOverlay(locationProvider, map);
        userLocationOverlay.enableMyLocation();
        userLocationOverlay.setOptionsMenuEnabled(true);

        // setup the button to center the map on the user's location
        ImageButton btCenterMap = (ImageButton) mapFragmentView.findViewById(R.id.ic_center_map);
        btCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MapFragment.this.userLocation != null) {
                    GeoPoint userPosition = new GeoPoint(MapFragment.this.userLocation);
                    map.getController().animateTo(userPosition);
                } else {
                    Toast.makeText(getContext(), getString(R.string.no_location_available_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

        map.getOverlays().add(userLocationOverlay);
    }

    private List<Overlay> getOrMakeTourOverlays(MapView map, Tour tour) {
        if(tour == null) {
            Log.w(LOGTAG, "Not creating tour overlays for empty input.");
            return Collections.emptyList();
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

        // get the tour mapstops only once. This prooved relevant for performance with ormlite.
        final List<Mapstop> mapstops = tour.getMapstops();
        if(mapstops != null && !mapstops.isEmpty()) {
            // markers for mapstops share a custom info window
            final MarkerInfoWindow window = new MapFragment.MapstopMarkerInfoWindow(R.layout.map_my_bonuspack_bubble, map);
            Marker marker;
            Mapstop mapstop;
            // as the osmdroid Marker overlay does not support a z-index draw the normal mapstop markers
            // first, then add the first marker to let them appear on top
            for(int i = 1; i < mapstops.size(); i++) {
                marker = MapUtil.makeMapstopMarker(getContext(), state.map, mapstops.get(i));
                marker.setInfoWindow(window);
                overlays.add(marker);
            }
            marker = MapUtil.makeFirstMapstopMarkerInTour(getContext(), state.map, mapstops.get(0));
            marker.setInfoWindow(window);
            overlays.add(marker);
        } else {
            Log.w(LOGTAG, "No mapstops for tour. Overlay creation skipped.");
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

        // close all infowindows that might still be open.
        InfoWindow.closeAllInfoWindowsOn(state.map);

        // clear the map of all markers and polylines
        for (Overlay overlay : state.map.getOverlays()) {
            if (overlay instanceof Marker || overlay instanceof Polyline) {
                state.map.getOverlays().remove(overlay);
            }
        }

        // add new markers and return them
        final List<Overlay> markers = new ArrayList<>();
        for(TourOnMap tourOnMap : toursOnMap) {
            markers.addAll(getOrMakeTourOverlays(state.map, tourOnMap.getTour()));
            state.map.getOverlays().addAll(markers);
        }
        state.toursOnMap = toursOnMap;
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


    // METHODS for LocationListener interface

    @Override
    public void onLocationChanged(Location location) {
        this.userLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // do nothing
    }

    @Override
    public void onProviderEnabled(String provider) {
        // do nothing
    }

    @Override
    public void onProviderDisabled(String provider) {
        // do nothing
    }

}
