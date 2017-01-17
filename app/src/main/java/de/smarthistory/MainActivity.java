package de.smarthistory;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.SqlTileWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;

public class MainActivity extends AppCompatActivity implements MapFragment.OnMapFragmentInteractionListener {

    private  static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

    private DataFacade data = DataFacade.getInstance();

    private Mapstop currentMapstop;

    private ActionBarDrawerToggle mDrawerToggle;

    // the main toolbar that is set up as the action bar
    private Toolbar mainToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup main tool bar as action bar
        this.mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        this.setSupportActionBar(mainToolbar);

        // Request permissions to support Android Marshmallow and above devices
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }

        // initialize the drawer menu
        initializeNavDrawerMenu();

        // initialize the main fragment (the map)
        // make sure, that the fragment container is present
        if (findViewById(R.id.main_fragment_container) != null) {

            // if we are restored from a previous state, don't initiate a new fragment
            if (savedInstanceState != null) {
                return;
            }

            MapFragment mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, mapFragment).commit();
        }
    }

    private void switchMainFragmentTo(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // replace and ensure that the user can navigate back if she wants to
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
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
                    Toast.makeText(MainActivity.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
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

    // START drawer menu
    private void initializeNavDrawerMenu() {
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, getNavDrawerTitles()));

        // a click listener, empty for now
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // interaction with action bar
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mainToolbar, R.string.menu_title, R.string.app_name) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
            }

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                getSupportActionBar().setTitle(getResources().getString(R.string.menu_title));
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    // Empty method for interaction with the map fragment if needed
    @Override
    public void onMapFragmentInteraction(Uri uri) {
        return;
    }

    /** TODO From tutorial. Find an equivalent. Do we need to override something like this?
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     // Pass the event to ActionBarDrawerToggle, if it returns
     // true, then it has handled the app icon touch event
     if (mDrawerToggle.onOptionsItemSelected(item)) {
     return true;
     }
     // Handle your other action bar items...

     return super.onOptionsItemSelected(item);
     }

     **/


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
                res.getString(R.string.menu_all_data),
                res.getString(R.string.menu_options_general),
                res.getString(R.string.menu_about)
        };

        return result;
    }

    // empty mehtod to be filled with code for when drawer Item is clicked
    private void selectItem(int position) {
        Toast.makeText(this, "Selected: " + position, Toast.LENGTH_SHORT).show();
    }
    // END drawer menu

}
