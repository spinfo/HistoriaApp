package de.historia_app;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapUpdatingGpsLocationProvider implements IMyLocationProvider, LocationListener {

    private static final String TAG = MapUpdatingGpsLocationProvider.class.getSimpleName();

    private static final int MIN_DELAY_TILL_UPDATE = 500;
    private static final float MIN_DISTANCE_FOR_UPDATE = 1.0f;

    private LocationManager locationManager;
    private MapView mapView;
    private MyLocationNewOverlay overlay;

    MapUpdatingGpsLocationProvider(@NonNull MapView mapView) {
        this.mapView = mapView;

        Context context = mapView.getContext();
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        showLocationOverlay();
    }

    private void showLocationOverlay() {
        if (overlay == null) {
            overlay = new MyLocationNewOverlay(this, mapView);
            overlay.enableMyLocation();
        }
        addOverlayAtMostOnce();
    }

    private void addOverlayAtMostOnce() {
        if (!mapView.getOverlays().contains(overlay)) {
            mapView.getOverlays().add(overlay);
        }
    }

    private void hideLocationOverlay() {
        mapView.getOverlays().remove(overlay);
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            showLocationOverlay();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            hideLocationOverlay();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        overlay.onLocationChanged(location, this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        if (overlay.equals(myLocationConsumer)) {
            return startLocationProvider();
        }
        Log.w(TAG, "Supplying external consumers is not supported.");
        return false;
    }

    private boolean startLocationProvider() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_DELAY_TILL_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);
            return true;
        } catch (SecurityException e) {
            Log.i(TAG, "Not listening for gps updates as that raised a Security Exception.");
            return false;
        }
    }

    @Override
    public void stopLocationProvider() {
        locationManager.removeUpdates(this);
    }

    @Override
    public Location getLastKnownLocation() {
        return overlay.getLastFix();
    }

    @Override
    public void destroy() {
        Log.i(TAG, "Location provider will cease to update the map.");
        stopLocationProvider();
        overlay.disableMyLocation();
        mapView.getOverlays().remove(overlay);
        overlay = null;
    }

    protected boolean centerOnUser() {
        GeoPoint last = overlay.getMyLocation();
        if (last != null) {
            mapView.getController().animateTo(last);
            return true;
        }
        return false;
    }

    protected void pauseListeningForLocationUpdates() {
        // the order of these is important and reverses "resumeListeningForLocationUpdates"
        overlay.disableMyLocation();
        hideLocationOverlay();
    }

    protected void resumeListeningForLocationUpdates() {
        // the order of these is important and reverses "pauseListeningForLocationUpdates"
        showLocationOverlay();
        overlay.enableMyLocation();
    }
}
