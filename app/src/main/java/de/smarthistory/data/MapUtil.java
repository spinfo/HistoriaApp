package de.smarthistory.data;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MapUtil {

    public static void zoomTo(MapView map, double lat, double lon, int zoom) {
        IMapController controller = map.getController();
        controller.setCenter(new GeoPoint(lat, lon));
        controller.setZoom(zoom);
    }

    public static void setMapDefaults(MapView map) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
    }

    public static void zoomToMarkers(MapView map, List<Marker> markers) {
        BoundingBox box = BoundingBox.fromGeoPoints(getGeoPointsFromMarkers(markers));
        IMapController mapController = map.getController();
        mapController.setCenter(box.getCenter());
        mapController.setZoom(17);
    }

    public static List<IGeoPoint> getGeoPointsFromMarkers(List<Marker> markers) {
        final List<IGeoPoint> result = new ArrayList<>();
        for (Marker marker : markers) {
            result.add((marker.getPosition()));
        }
        return result;
    }

}
