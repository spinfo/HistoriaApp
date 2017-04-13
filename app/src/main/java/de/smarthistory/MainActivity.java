package de.smarthistory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import de.smarthistory.data.FilesDataProvider;

public class MainActivity extends AppCompatActivity {

    private  static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

    private DataFacade data = DataFacade.getInstance();

    private static final String MAP_FRAGMENT_TAG = "map_fragment";
    private static final String EXPLORE_FRAGMENT_TAG = "explore_data_fragment";

    // variables to interact with the main menu in the navigation drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    // the main toolbar that is set up as the action bar
    private Toolbar mainToolbar;

    // an interface for the Fragments created by this class
    interface MainActivityFragment {
        boolean reactToBackButtonPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // prepare the asset files if neccessary
        data.prepareAssets(getAssets(), getExternalFilesDir(null));

        // This checks and requests permissions to support the map on Marshmallow and above devices
        boolean hasPermissions = checkMapPermissions();
        Log.d("main", "Permission check shows: " + hasPermissions);
        if (hasPermissions) {
            setContentView(R.layout.activity_main);

            // setup main tool bar as action bar
            this.mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
            this.setSupportActionBar(mainToolbar);

            // initialize the drawer menu
            initializeNavDrawerMenu();

            // initialize the main fragment (the map)
            // make sure, that the fragment container is present
            if (findViewById(R.id.main_fragment_container) != null) {
                // if we are not restored from a previous state, or t"afterPermissionsGrantedToken"his is a restart after a per-
                // mission granting, create a new map fragment
                Log.d("main", "savedInstanceState: " + savedInstanceState);
                if (savedInstanceState == null) {
                    // TODO: 'switch' is the wrong verb here, 'initOrSwitch'?
                    switchMainFragmentToMap(false);
                }
                // check if this is a restart after a permission grant
                else if (getIntent().getBooleanExtra("afterPermissionsGrantedToken", false)) {
                    getIntent().removeExtra("afterPermissionsGrantedToken");
                }

            }
        } else {
            // set the content to a message indicating the need for further permissions
            setContentView(R.layout.permissions_required);

            // a re-check of permissions is triggered by just recreating the whole MainActivity
            Button checkAgainButton = (Button) findViewById(R.id.trigger_permission_check);
            checkAgainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.recreate();
                }
            });
        }
    }

    private MapFragment switchMainFragmentToMap(boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // look, if there is a main map fragment already, create a new one if there isn't
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null) {
            mapFragment = new MapFragment();
        }

        // replace the fragment and add to the transaction to the back stack to be able to switch
        // to it later
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, mapFragment, MAP_FRAGMENT_TAG);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        return mapFragment;
    }

    private ExploreDataFragment switchMainFragmentToExploreData(boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // look, if there is a main map fragment already, create a new one if there isn't
        ExploreDataFragment exploreFragment = (ExploreDataFragment) fragmentManager.findFragmentByTag(EXPLORE_FRAGMENT_TAG);
        if (exploreFragment == null) {
            exploreFragment = new ExploreDataFragment();
        }

        // replace the fragment and add to the transaction to the back stack to be able to switch
        // to it later
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, exploreFragment, EXPLORE_FRAGMENT_TAG);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        return exploreFragment;
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
     * sets storage state and current cache size
     */
    private void updateStorageInfo(){
        long cacheSize = updateStoragePrefreneces(this);
    }

    // START PERMISSION CHECK
    // TODO: The whole permission code could go to the map fragment probably
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private boolean checkMapPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            // if we we are below API Level 23, permissions have been granted on installation
            return true;
        }

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
            // when calling checkMapPermissions()
            requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            // return false to indicate that additional permissions are needed/asked for
            return false;
        }
        // else: We have permissions
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean allGranted = false;

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

                    // TODO is this still needed?
                    updateStorageInfo();

                    allGranted = true;
                }
                // TODO Handle the case when location is not granted but external storage is. (Map may be shown but without location)
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        // Recreate activity to show the default map view
        if (allGranted) {
            Intent intent = new Intent();
            intent.putExtra("afterPermissionsGrantedToken", true);
            MainActivity.this.finish();
            MainActivity.this.startActivity(getIntent());
        }
    }
    // END PERMISSION CHECK

    // START drawer menu
    private void initializeNavDrawerMenu() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
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
                res.getString(R.string.menu_select_area),
                res.getString(R.string.menu_select_tour),
                res.getString(R.string.menu_explore_data),
                res.getString(R.string.menu_options_general),
                res.getString(R.string.menu_about)
        };

        return result;
    }

    // empty mehtod to be filled with code for when drawer Item is clicked
    private void selectItem(int position) {
        if (position == 1) {
            MapFragment mapFragment = switchMainFragmentToMap(true);
            mapFragment.showTourSelection(data.getCurrentArea());
            mDrawerLayout.closeDrawers();
        } else if (position == 2) {
            Fragment exploreDataFragment = new ExploreDataFragment();
            switchMainFragmentToExploreData(true);
            mDrawerLayout.closeDrawers();
        } else {
            Toast.makeText(this, "Selected: " + position, Toast.LENGTH_SHORT).show();
        }
    }
    // END drawer menu

    @Override
    public void onBackPressed() {
        // inform the current fragment of the back press and only get active if it wasn't handled
        MainActivityFragment fragment = (MainActivityFragment) getVisibleFragment();
        boolean fragmentReacted = fragment.reactToBackButtonPressed();
        if (!fragmentReacted) {
            super.onBackPressed();
        }
    }

    // TODO this will need changing once we have more than one visible fragment (e.g. on bigger devices)
    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

}
