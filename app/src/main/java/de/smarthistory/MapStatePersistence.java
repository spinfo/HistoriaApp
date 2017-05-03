package de.smarthistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.osmdroid.api.IGeoPoint;

import de.smarthistory.data.DataFacade;

public abstract class MapStatePersistence {

    private static final String K_CENTER_LAT = "centerLat";
    private static final String K_CENTER_LON = "centerLon";
    private static final String K_ZOOM = "zoom";
    private static final String K_TOUR_ID = "tourId";
    private static final String K_MAPSTOP_TAPPED_ID = "mapstopTappedId";

    private static final long NO_OBJECT = -1;

    private static final String[] KEYS = { K_CENTER_LAT, K_CENTER_LON, K_ZOOM,
            K_TOUR_ID, K_MAPSTOP_TAPPED_ID };

    public static void save(MapFragment.MapState state, SharedPreferences prefs) {
        IGeoPoint center = state.map.getMapCenter();

        SharedPreferences.Editor editor = prefs.edit();
        writeDouble(editor, K_CENTER_LAT, center.getLatitude());
        writeDouble(editor, K_CENTER_LON, center.getLongitude());
        editor.putInt(K_ZOOM, state.map.getZoomLevel(false));
        editor.putLong(K_TOUR_ID, state.currentTour.getId());
        // there might be no mapstop tapped at the moment
        if (state.mapstopTapped != null) {
            editor.putLong(K_MAPSTOP_TAPPED_ID, state.mapstopTapped.getId());
        } else {
            editor.putLong(K_MAPSTOP_TAPPED_ID, NO_OBJECT);
        }
        editor.apply();
    }

    // restores a MapState, will complain with an InconsistentMapStateException if a value is missing
    // sets center coordinates and zoom level on the map, but does not recreate any overlays
    public static void load(MapFragment.MapState state, SharedPreferences prefs, Context context) {
        for (String key : KEYS) {
            if (!prefs.contains(key)) {
                throw new InconsistentMapStateException("Map state without key '" + key + "'");
            }
        }
        if (state.map == null) {
            throw new InconsistentMapStateException("Can't load map state onto null view.");
        }
        // TODO: Bounds check or better default values
        double lat = readDouble(prefs, K_CENTER_LAT, 0.0);
        double lon = readDouble(prefs, K_CENTER_LON, 0.0);
        int zoom = prefs.getInt(K_ZOOM, 16);
        long tourId = prefs.getLong(K_TOUR_ID, NO_OBJECT);
        long mapstopTappedId = prefs.getLong(K_MAPSTOP_TAPPED_ID, NO_OBJECT);

        MapUtil.zoomTo(state.map, lat, lon, zoom);
        if (tourId != NO_OBJECT) {
            state.currentTour = DataFacade.getInstance(context).getTourById(tourId);
        } else {
            // a tour is always specified
            throw new InconsistentMapStateException("Tour id not specified.");
        }
        // a mapstop doesn't have to be specified, so don't throw an exception
        if (mapstopTappedId != NO_OBJECT) {
            state.mapstopTapped = DataFacade.getInstance(context).getMapstopById(mapstopTappedId);
        } else {
            state.mapstopTapped = null;
        }

    }

    // since shared prefs can't store doubles we need to store as long to not loose precision
    private static void writeDouble(SharedPreferences.Editor editor, String key, double value) {
        editor.putLong(key, Double.doubleToRawLongBits(value));
    }

    // since shared prefs can't store doubles we need to read a long, then convert
    private static Double readDouble(SharedPreferences prefs, String key, double defaultValue) {
        if (!prefs.contains(key)) {
            return defaultValue;
        }
        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

    public static class InconsistentMapStateException extends RuntimeException {

        String message;

        public InconsistentMapStateException(String message) {
            super(message);
            this.message = message;
        }

    }

}
