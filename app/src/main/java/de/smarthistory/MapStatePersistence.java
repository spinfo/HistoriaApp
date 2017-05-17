package de.smarthistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.osmdroid.api.IGeoPoint;

import java.util.List;

import de.smarthistory.data.Area;
import de.smarthistory.data.DataFacade;
import de.smarthistory.data.TourOnMap;

public abstract class MapStatePersistence {

    private static final String LOG_TAG = MapStatePersistence.class.getSimpleName();

    private static final String K_CENTER_LAT = "centerLat";
    private static final String K_CENTER_LON = "centerLon";
    private static final String K_ZOOM = "zoom";
    private static final String K_MAPSTOP_TAPPED_ID = "mapstopTappedId";
    private static final String K_AREA_ID = "areaId";

    private static final long NO_OBJECT = -1;

    private static final String[] KEYS = { K_CENTER_LAT, K_CENTER_LON, K_ZOOM,
            K_AREA_ID, K_MAPSTOP_TAPPED_ID };

    public static void save(MapFragment.MapState state, SharedPreferences prefs, DataFacade data) {
        IGeoPoint center = state.map.getMapCenter();

        SharedPreferences.Editor editor = prefs.edit();
        writeDouble(editor, K_CENTER_LAT, center.getLatitude());
        writeDouble(editor, K_CENTER_LON, center.getLongitude());
        editor.putInt(K_ZOOM, state.map.getZoomLevel(false));

        // if there is no tour at the moment do not save any value.
        if(state.toursOnMap != null) {
            final boolean result = data.saveToursOnMap(state.toursOnMap);
            if(!result) {
                Log.w(LOG_TAG, "Failed to save state of tours on map.");
            }
        }

        // there should always be an area value to set
        if(state.area != null) {
            safeWriteIdValue(editor, K_AREA_ID, state.area.getId());
        }

        // there might be no mapstop tapped at the moment
        if (state.mapstopTapped != null) {
            safeWriteIdValue(editor, K_MAPSTOP_TAPPED_ID, state.mapstopTapped.getId());
        } else {
            safeWriteIdValue(editor, K_MAPSTOP_TAPPED_ID, NO_OBJECT);
        }
        editor.apply();
    }

    // restores a MapState, will complain with an InconsistentMapStateException if a value is missing
    // sets center coordinates and zoom level on the map, but does not recreate any overlays
    public static void load(MapFragment.MapState state, SharedPreferences prefs, DataFacade data, Context context) {
        for (String key : KEYS) {
            if (!prefs.contains(key)) {
                throw new InconsistentMapStateException("Map state without key '" + key + "'");
            }
        }
        if (state.map == null) {
            throw new InconsistentMapStateException("Can't load map state onto null view.");
        }
        // TODO: Bounds check or better default values
        final double lat = readDouble(prefs, K_CENTER_LAT, 0.0);
        final double lon = readDouble(prefs, K_CENTER_LON, 0.0);
        final int zoom = prefs.getInt(K_ZOOM, 16);
        final long mapstopTappedId = prefs.getLong(K_MAPSTOP_TAPPED_ID, NO_OBJECT);
        MapUtil.zoomTo(state.map, lat, lon, zoom);

        final List<TourOnMap> toursOnMap = data.getToursOnMap();
        if (toursOnMap == null || toursOnMap.isEmpty()) {
            // a tour is always specified
            throw new InconsistentMapStateException("No tour on map given.");
        } else {
            state.toursOnMap = toursOnMap;
        }
        // a mapstop doesn't have to be specified, so don't throw an exception
        if (mapstopTappedId != NO_OBJECT) {
            state.mapstopTapped = data.getMapstopById(mapstopTappedId);
        } else {
            state.mapstopTapped = null;
        }
        // an area should always be given. Read it from the preferences or set default from db
        state.area = getArea(prefs, data);
    }

    /**
     * Read the area from shared Preferences
     * @param prefs The SharedPreferences used to get the area id from.
     * @return Area object or null on db failure
     */
    public static Area getArea(SharedPreferences prefs, DataFacade data) {
        final Area result;
        if(prefs.contains(K_AREA_ID)) {
            final long id = prefs.getLong(K_AREA_ID, NO_OBJECT);
            if(id == NO_OBJECT) {
                result = data.getDefaultArea();
            } else {
                result = data.getAreaById(id);
            }
        } else {
            result = data.getDefaultArea();
        }
        return result;
    }

    private static void safeWriteIdValue(SharedPreferences.Editor editor, String key, Long id) {
        if(id == null || id < 0) {
            id = NO_OBJECT;
        }
        editor.putLong(key, id);
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

    static class InconsistentMapStateException extends RuntimeException {

        static final long serialVersionUID = 1L;

        String message;

        InconsistentMapStateException(String message) {
            super(message);
            this.message = message;
        }

    }

}
