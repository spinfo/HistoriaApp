package de.smarthistory;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Class for the very short osmdroid tutorial:
 *
 *      https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library#hello-osmdroid-world
 *
 * Assisted by the (slightly abbreviated and changed) implementation of permission checking from
 *
 *      https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/MainActivity.java
 */
public class MapViewActivity extends AppCompatActivity {

    private  static final Logger LOGGER = Logger.getLogger(MapViewActivity.class.getName());

    private DataFacade data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // Request permissions to support Android Marshmallow and above devices
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }

        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        // set up the data facade to use
        this.data = DataFacade.getInstance();

        // initialize the drawer menu
        initializeNavDrawerMenu();

        // set up the map to use
        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(17);
        // GeoPoint startPoint = new GeoPoint(51.22049, 6.79202);
        GeoPoint startPoint = new GeoPoint(50.95863, 6.94487);
        mapController.setCenter(startPoint);

        // add Overlay for POIs
        addPOIs(map);

        // add Overlay for current location
        addCurrentLocation(map);
    }

    /**
     * refreshes the current osmdroid cache paths with user preferences plus soe logic to work around
     * file system permissions on api23 devices. it's primarily used for out android tests.
     * @param ctx
     * @return current cache size in bytes
     */
    public static long updateStoragePrefreneces(Context ctx){

        //loads the osmdroid config from the shared preferences object.
        //if this is the first time launching this app, all settings are set defaults with one exception,
        //the tile cache. the default is the largest write storage partition, which could end up being
        //this app's private storage, depending on device config and permissions

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //also note that our preference activity has the corresponding save method on the config object, but it can be called at any time.


        File dbFile = new File(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME);
        if (Build.VERSION.SDK_INT >= 9 && dbFile.exists()) {
            return dbFile.length();
        }
        return -1;
    }


    /**
     * gets storage state and current cache size
     */
    private void updateStorageInfo(){

        long cacheSize = updateStoragePrefreneces(this);
        //cache management ends here

        // Original method cut of. Compare: https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/MainActivity.java#L168
        // TODO: Check if we should include something like that here
    }


    // START PERMISSION CHECK
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        String message = "osmdroid permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nLocation to show user location.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nStorage access to store map tiles.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            // the if condition silences an android studio warning. Actual version check is done
            // when calling checkPermissions()
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        } // else: We already have permissions, so handle as normal
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE
                Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (location && storage) {
                    // All Permissions Granted
                    Toast.makeText(MapViewActivity.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MapViewActivity.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
                            "\nLocation permission is required to show the user's location on map.", Toast.LENGTH_SHORT).show();
                }
                updateStorageInfo();
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    // END PERMISSION CHECK


    private void addCurrentLocation(MapView map) {
        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(this);
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(locationProvider, map);

        map.getOverlays().add(myLocationOverlay);
        myLocationOverlay.enableMyLocation();
    }

    private void addPOIs(MapView map) {
        List<Marker> markers = new ArrayList<>();

        for (Mapstop mapstop : data.getMapstops()) {
            Marker marker = new Marker(map);
            GeoPoint geoPoint = mapstop.getPlace().getLocation();
            marker.setPosition(geoPoint);
            marker.setTitle(mapstop.getPlace().getName());

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.map_marker_icon_blue_small));

            markers.add(marker);

            LOGGER.info("-- Mapstop: " + mapstop.getPlace().getName() + ", " + mapstop.getText() + " (" + mapstop.getPlace().getLocation().getLatitude() + "/" + mapstop.getPlace().getLocation().getLongitude() + ")");
        }

        map.getOverlays().addAll(markers);
    }


    // START drawer menu
    private void initializeNavDrawerMenu() {
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, getNavDrawerTitles()));

        // a click listener, empty for now
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    // The click listner for ListView in the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private String[] getNavDrawerTitles() {
        final Resources res = getResources();
        String[] result = {
                res.getString(R.string.menu_current_area) + data.getCurrentArea().getName(),
                res.getString(R.string.menu_current_tour) + data.getCurrentTour().getName(),
                res.getString(R.string.menu_tours_available),
                res.getString(R.string.menu_options_general)
        };

        return result;
    }


    // empty mehtod to be filled with code for when drawer Item is clicked
    private void selectItem(int position) { }
    // END drawer menu

}
