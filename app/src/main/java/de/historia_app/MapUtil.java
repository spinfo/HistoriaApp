package de.historia_app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.DashPathEffect;
import android.support.v4.content.ContextCompat;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import de.historia_app.data.Mapstop;
import de.historia_app.data.Tour;
import de.historia_app.mappables.TourOnMap;

public abstract class MapUtil {

    public static void zoomTo(MapView map, double lat, double lon, int zoom) {
        IMapController controller = map.getController();
        controller.setCenter(new GeoPoint(lat, lon));
        controller.setZoom(zoom);
    }

    public static void zoomToDefaultLocation(MapView map) {
        // this zooms to the center of germany atm
        zoomTo(map, 51.163441, 10.447612, 6);
    }

    public static void setMapDefaults(MapView map) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
    }

    public static void zoomToOverlays(final MapView map, final List<Overlay> markers) {
        final BoundingBox box = BoundingBox.fromGeoPoints(getGeoPointsFromOverlays(markers));

        // the zoom to a bounding box is a bit buggy in osmdroid. It has to happen after layout
        // is fully rendered and it might fail if the map was not zoomed to a point before,
        // this contraption (the runnable and the double zoom) makes it work
        map.post(new Runnable() {
            @Override
            public void run() {
                zoomTo(map, box.getCenter().getLatitude(), box.getCenter().getLongitude(), 17);
                map.zoomToBoundingBox(box, true);
            }
        });
    }

    // A Polyline used to draw the track of a tour on the map
    public static Polyline makeEmptyTourTrackPolyline(Context context) {
        final Polyline line = new Polyline();
        Resources r = context.getResources();

        // basic options
        line.setColor(R.color.colorAccent);
        line.setWidth(r.getDimension(R.dimen.tour_track_line_width));

        // the dashed path to draw is given with on/off intervals in a float array
        float[] dashIntervalls = {
                r.getDimension(R.dimen.tour_track_dash_on),
                r.getDimension(R.dimen.tour_track_dash_off)
        };
        line.getPaint().setPathEffect(new DashPathEffect(dashIntervalls, 0));

        return line;
    }

    // A marker representing a mapstop on the map
    public static Marker makeMapstopMarker(Context context, MapView map, Mapstop mapstop) {
        Marker marker = new Marker(map);
        setMarkerDefaults(marker, mapstop);
        marker.setIcon(ContextCompat.getDrawable(context, R.drawable.map_marker_icon_blue_small));
        return marker;
    }

    // A marker representing the first mapstop of a tour
    public static Marker makeFirstMapstopMarkerInTour(Context context, MapView map, Mapstop mapstop) {
        Marker marker = new Marker(map);
        setMarkerDefaults(marker, mapstop);
        marker.setIcon(ContextCompat.getDrawable(context, R.drawable.map_marker_icon_red_small));
        return marker;
    }

    private static void setMarkerDefaults(Marker marker, Mapstop mapstop) {
        marker.setPosition(mapstop.getPlace().getLocation());
        marker.setTitle(mapstop.getName());
        marker.setSubDescription(mapstop.getDescription());
        marker.setRelatedObject(mapstop);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    }

    // TODO: This is messy especially for Polyline that does not actually save points as GeoPoints. The results of this should probably be cached somewhere
    private static List<IGeoPoint> getGeoPointsFromOverlays(List<Overlay> overlays) {
        final List<IGeoPoint> result = new ArrayList<>();

        for (Overlay overlay : overlays) {
            if (overlay instanceof Marker) {
                result.add(((Marker) overlay).getPosition());
            }
            else if (overlay instanceof Polyline) {
                result.addAll(((Polyline) overlay).getPoints());
            }
            else {
                // do nothing
            }
        }
        return result;
    }

}
