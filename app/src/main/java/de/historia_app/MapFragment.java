package de.historia_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.historia_app.data.Area;
import de.historia_app.data.DataFacade;
import de.historia_app.data.Place;
import de.historia_app.data.Tour;
import de.historia_app.mappables.PlaceOnMap;
import de.historia_app.mappables.TourCollectionOnMap;
import de.historia_app.mappables.TourOnMap;


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
        Place placeTapped;
        int mapstopSwitchedPos;
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

    private MapUpdatingGpsLocationProvider mapUpdatingGpsLocationProvider;

    public MapFragment() { }

     @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.data = new DataFacade(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        // Inflate the layout for this fragment
        mapFragmentView = (FrameLayout) inflater.inflate(R.layout.fragment_map, container, false);

        // initialize the state of this fragment with a new map
        state = new MapState();
        state.map = mapFragmentView.findViewById(R.id.map);

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
            if (state.placeTapped != null) {
                reopenInfoWindowFor(state.placeTapped, state.mapstopSwitchedPos);
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

        // if there is only a single tour selected, tell the listener as well
        if (state.toursOnMap != null && state.toursOnMap.size() == 1) {
            onModelSelectionListener.onTourSelected(state.toursOnMap.get(0).getTour());
        }

        // recreate popup from bundle if needed
        if (savedInstanceState != null) {
            popupManager.restorePopupStateFrom(savedInstanceState, this);
        }

        // return the container for the map view
        return mapFragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // add the overlay showing the user's location and enable location options
        addUserLocationOptions(state.map);

        // make the link to the osm license clickable
        setupOSMLicenseLinkForUserInteraction(mapFragmentView.findViewById(R.id.osm_legal_reference));
    }

    @Override
    public void onPause() {
        super.onPause();
        mapUpdatingGpsLocationProvider.pauseListeningForLocationUpdates();
        MapStatePersistence.save(state, getPrefs(), data);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapUpdatingGpsLocationProvider.resumeListeningForLocationUpdates();
    }

    // This saves data not being needed between complete restarts (e.g. the open popup) and
    // therefore not in the SharedPreferences
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // unlike the main map state, the popup state is saved in a bundle
        popupManager.savePopupStateTo(outState);

        // dismiss the active popup, else it will be memory-leaked
        popupManager.dismissActivePopup();
    }

    private SharedPreferences getPrefs() {
        return getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    private void setupOSMLicenseLinkForUserInteraction(final View view) {
        if(view == null) {
            Log.e(LOGTAG, "Cannot setup osm legal notice");
            return;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = getString(R.string.osm_copyright_url);
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private void addUserLocationOptions(final MapView map) {
        // setup an overlay for the user's location
        mapUpdatingGpsLocationProvider = new MapUpdatingGpsLocationProvider(map);

        // setup the button to center the map on the user's location
        ImageButton btCenterMap = mapFragmentView.findViewById(R.id.ic_center_map);
        btCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean successfullyCentered = mapUpdatingGpsLocationProvider.centerOnUser();
                if (!successfullyCentered) {
                    Toast.makeText(getContext(), getString(R.string.no_location_available_message), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<Overlay> makeTourOverlays(MapView map, TourCollectionOnMap tourCollectionOnMap) {
        List<Overlay> overlays = new ArrayList<>();

        // the tour's track is drawn with a polyline
        // add this first for it to be drawn before the markers
        for (TourOnMap tourOnMap : tourCollectionOnMap.getToursOnMap()) {
            Tour tour = tourOnMap.getTour();
            final Polyline line = MapUtil.makeEmptyTourTrackPolyline(getContext());
            line.setPoints(tour.getTrackAsGeoPoints());
            overlays.add(line);
        }

        // get the tour mapstops only once. This proofed relevant for performance with ormlite.
        MarkerInfoWindow window;
        Marker marker;
        for (PlaceOnMap placeOnMap : tourCollectionOnMap.getPlacesOnMap()) {
            window = new PlaceMarkerInfoWindow(R.layout.mapstop_bubble, map, this);
            marker = MapUtil.makeMapstopMarker(getContext(), state.map, placeOnMap);
            marker.setInfoWindow(window);
            overlays.add(marker);
        }

        return overlays;
    }

    // switches the current tour to the single tour supplied by getting/creating markers.
    // Returns those markers.
    private List<Overlay> switchTourOverlays(TourOnMap tourOnMap) {
        if (tourOnMap == null || tourOnMap.getTour() == null) {
            Log.w(LOGTAG, "Empty tour as input.");
            return Collections.emptyList();
        }

        final List<TourOnMap> substituteList = new ArrayList<>(1);
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

        // create a collection of the tours
        final List<Tour> tours = new ArrayList<>(toursOnMap.size());
        for(TourOnMap tourOnMap : toursOnMap) {
            tours.add(tourOnMap.getTour());
        }
        TourCollectionOnMap tourCollectionOnMap = new TourCollectionOnMap(tours);

        final List<Overlay> overlays = makeTourOverlays(state.map, tourCollectionOnMap);
        state.map.getOverlays().addAll(overlays);

        state.toursOnMap = toursOnMap;
        return overlays;
    }

    // opens the info window belonging to a place marker if the map contains a marker that
    // has the provided place as a related object, switches the infowindow bubble to the mapstop
    // given by pos.
    private void reopenInfoWindowFor(final Place place, final int pos) {
        final long needle = place.getId();
        Object relatedObject;
        for (Overlay overlay : state.map.getOverlays()) {
            // only look at marker overlays
            if (overlay instanceof Marker) {

                final Marker marker = (Marker) overlay;
                relatedObject = marker.getRelatedObject();

                // only look at those markers related to a PlaeceOnMap
                if(relatedObject instanceof  PlaceOnMap) {
                    if(((PlaceOnMap) relatedObject).getPlace().getId() == needle) {
                        // delay opening the info window after the map view has been rendered
                        state.map.post(new Runnable() {
                            @Override
                            public void run() {
                                marker.showInfoWindow();
                                PlaceMarkerInfoWindow window = (PlaceMarkerInfoWindow) marker.getInfoWindow();
                                window.switchToPosition(pos);
                            }
                        });
                    }
                }
            }
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

    @Override
    public void onPlaceTapped(PlaceOnMap placeOnMap) {
        // treat null as the signal that no place is tapped.
        if(placeOnMap == null) {
            this.state.placeTapped = null;
        } else {
            this.state.placeTapped = placeOnMap.getPlace();
        }
    }

    @Override
    public void onMapstopSwitched(int position) {
        this.state.mapstopSwitchedPos = position;
    }

    @Override
    public void onMapstopSelected(PlaceOnMap placeOnMap, int position) {
        popupManager.showMapstop(placeOnMap.getMapstopsOnMap().get(position).getMapstop());
    }

    public void setOnModelSelectionListener(OnModelSelectionListener listener) {
        this.onModelSelectionListener = listener;
    }

    @Override
    public boolean reactToBackButtonPressed() {
        return false;
    }

}
