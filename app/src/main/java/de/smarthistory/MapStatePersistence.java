package de.smarthistory;

import android.content.SharedPreferences;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;

import java.util.logging.Logger;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.MapUtil;
import de.smarthistory.data.Tour;

public abstract class MapStatePersistence {

    private static final Logger LOGGER = Logger.getLogger(MapStatePersistence.class.getName());

    private static final String K_CENTER_LAT = "centerLat";
    private static final String K_CENTER_LON = "centerLon";
    private static final String K_ZOOM = "zoom";
    private static final String K_TOUR_ID = "tourId";

    private static final String[] KEYS = { K_CENTER_LAT, K_CENTER_LON, K_ZOOM, K_TOUR_ID };

    public static void save(MapFragment.MapState state, SharedPreferences prefs) {
        IGeoPoint center = state.map.getMapCenter();

        SharedPreferences.Editor editor = prefs.edit();
        writeDouble(editor, K_CENTER_LAT, center.getLatitude());
        writeDouble(editor, K_CENTER_LON, center.getLongitude());
        editor.putInt(K_ZOOM, state.map.getZoomLevel(false));
        editor.putLong(K_TOUR_ID, state.currentTour.getId());
        editor.apply();

        LOGGER.info("Saved instance state: " + center.getLatitude() + "/" + center.getLongitude() + ", " + state.map.getZoomLevel() + ", tour: " + state.currentTour.getId());
    }

    // restores a MapState, will complain with an InconsistentMapStateException if a value is missing
    public static void load(MapFragment.MapState state, SharedPreferences prefs) {
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
        long tourId = prefs.getLong(K_TOUR_ID, -1);

        MapUtil.zoomTo(state.map, lat, lon, zoom);
        state.currentTour = DataFacade.getInstance().getTourById(tourId);
        LOGGER.info("Restored instance state: " + lat + "/" + lon + ", " + zoom + ", tour: " + state.currentTour.getId());
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
