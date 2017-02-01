package de.smarthistory.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.DashPathEffect;

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
import java.util.ResourceBundle;

import de.smarthistory.R;

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

    public static void zoomToOverlays(MapView map, List<Overlay> markers) {
        BoundingBox box = BoundingBox.fromGeoPoints(getGeoPointsFromOverlays(markers));
        IMapController mapController = map.getController();
        mapController.setCenter(box.getCenter());
        mapController.setZoom(17);
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
