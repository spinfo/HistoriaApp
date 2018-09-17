package de.historia_app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.SqlTileWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.historia_app.data.Area;
import de.historia_app.data.DataFacade;
import de.historia_app.data.FileService;
import de.historia_app.data.Tour;
import de.historia_app.mappables.PlaceOnMap;

/**
 * The main activity bascially does the following
 *  - checks if we were restarted on error and if yes, display it
 *  - checks if we have all necessary permissions (necessary at runtime on API level 23+)
 *  - sets up example data on a fresh installation
 *  - sets up a navigation drawer (main menu on the left of the screen)
 *  - switches between Fragments (which implement most behavior), these are:
 *      - a MapFragment to display tours and stops on a map
 *      - an ExploreDataFragment to display tours and stops etc, but not on a map
 *      - a TourDownloadFragment to download new tours
 */
public class MainActivity extends AppCompatActivity implements OnModelSelectionListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private DataFacade data;

    // A couple of strings to discern between fragments
    private static final String MAP_FRAGMENT_TAG = "map_fragment";
    private static final String EXPLORE_FRAGMENT_TAG = "explore_data_fragment";
    private static final String TOUR_DOWNLOAD_FRAGMENT_TAG = "tour_download_fragment";

    // variables to interact with the main menu in the navigation drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    // the main toolbar that is set up as the action bar
    private Toolbar mainToolbar;

    // the normal toolbar title (i.e. when the drawer is not toggled)
    private String defaultToolbarTitle;

    // an interface for the Fragments created by this class
    interface MainActivityFragment {
        boolean reactToBackButtonPressed();
    }

    // the drawer items shown in the left menu and an adapter to hold them
    private ArrayList<NavDrawerItem> navDrawerItems;
    private ArrayAdapter<NavDrawerItem> navDrawerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if we were re-started after a previous error, show it in a dialog and don't do anything
        // else so as not to provoke further errors
        String previousErrorMessage = getIntent()
                .getStringExtra(getString(R.string.extra_key_restart_error_message));
        if(previousErrorMessage != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(R.string.restart_error);
            alertDialog.setMessage(previousErrorMessage);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.resume_app),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // start anew removing the intent
                            Intent restartWithoutError = getIntent();
                            restartWithoutError.removeExtra(getString(R.string.extra_key_restart_error_message));
                            finish();
                            startActivity(restartWithoutError);
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.close_app),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
            alertDialog.show();

            // do not show any view, just the dialog
            return;
        }

        // This checks and requests permissions to support the map on Marshmallow and above devices
        boolean hasPermissions = checkMapPermissions();
        Log.d("main", "Permission check shows: " + hasPermissions);
        if (hasPermissions) {
            this.data = new DataFacade(this);

            // if there is no data yet, set it up
            Tour defaultTour = this.data.getDefaultTour();
            if(defaultTour == null) {
                Log.d(LOG_TAG, "No default tour found. Initializing example data.");
                FileService fs = new FileService(this);
                boolean result = fs.initializeExampleData();
                if(!result) {
                    ErrUtil.failInDebug(LOG_TAG, "Failed to initialize example data.");
                }
            }

            setContentView(R.layout.activity_main);

            // setup main tool bar as action bar, it's title is just the app name at first
            this.defaultToolbarTitle = getString(R.string.app_name);
            this.mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
            this.setSupportActionBar(mainToolbar);

            // initialize the drawer menu
            initializeNavDrawerMenu();

            // initialize the main fragment (the map)
            // make sure, that the fragment container is present
            if (findViewById(R.id.main_fragment_container) != null) {
                // if we are not restored from a previous state, or this is a restart after a per-
                // mission granting, create a new map fragment
                Log.d("main", "savedInstanceState: " + savedInstanceState);
                if (savedInstanceState == null) {
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
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null) {
            mapFragment = new MapFragment();
        }

        // setup this activity to listen to selections made from the map fragment
        mapFragment.setOnModelSelectionListener(this);
        setupFragmentAsMainFragment(mapFragment, MAP_FRAGMENT_TAG, addToBackStack);
        return mapFragment;
    }

    private ExploreDataFragment switchMainFragmentToExploreData(boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ExploreDataFragment exploreFragment = (ExploreDataFragment) fragmentManager.findFragmentByTag(EXPLORE_FRAGMENT_TAG);
        if (exploreFragment == null) {
            exploreFragment = new ExploreDataFragment();
        }
        setupFragmentAsMainFragment(exploreFragment, EXPLORE_FRAGMENT_TAG, addToBackStack);
        return exploreFragment;
    }

    private TourDownloadFragment switchMainFragmentToTourDownload(boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TourDownloadFragment downloadFragment = (TourDownloadFragment) fragmentManager.findFragmentByTag(TOUR_DOWNLOAD_FRAGMENT_TAG);
        if (downloadFragment == null) {
            downloadFragment = new TourDownloadFragment();
        }
        setupFragmentAsMainFragment(downloadFragment, TOUR_DOWNLOAD_FRAGMENT_TAG, addToBackStack);
        return downloadFragment;
    }

    // replace the fragment and add (if wished) add this action to the transaction back stack
    // to be able to switch back later
    private void setupFragmentAsMainFragment(Fragment fragment, String fragmentTag, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment, fragmentTag);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
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
        if (dbFile.exists()) {
            return dbFile.length();
        }
        return -1;
    }

    // START PERMISSION CHECK
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private boolean checkMapPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            // if we we are below API Level 23, permissions have been granted on installation
            return true;
        }

        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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

                    updateStoragePrefreneces(this);

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

        final ListView drawerList = (ListView) mDrawerLayout.findViewById(R.id.left_drawer);
        navDrawerItems = createDefaultNavDrawerItems();
        navDrawerAdapter = new NavDrawerAdapter(MainActivity.this, navDrawerItems);
        drawerList.setAdapter(navDrawerAdapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectNavDrawerItem((NavDrawerItem) parent.getItemAtPosition(position));
            }
        });

        // interaction of the drawer with action bar
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mainToolbar, R.string.menu_title, R.string.app_name) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(defaultToolbarTitle);
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

    private ArrayList<NavDrawerItem> createDefaultNavDrawerItems() {
        final NavDrawerItem[] navItems = {
                new NavDrawerItem(NavDrawerItem.ID.SELECT_AREA, getString(R.string.menu_select_area), false),
                new NavDrawerItem(NavDrawerItem.ID.SELECT_TOUR, getString(R.string.menu_select_tour), false),
                new NavDrawerItem(NavDrawerItem.ID.EXPLORE_DATA, getString(R.string.menu_explore_data), false),
                new NavDrawerItem(NavDrawerItem.ID.LOAD_TOUR, getString(R.string.menu_download_tours), false),
                new NavDrawerItem(NavDrawerItem.ID.ABOUT, getString(R.string.menu_about), false)
        };

        return new ArrayList<>(Arrays.asList(navItems));
    }

    private void toggleNavDrawerItemToExploreMap(boolean enable, Area area) {
        if(navDrawerAdapter == null || navDrawerAdapter.getItem(1) == null) {
            Log.e(LOG_TAG, "Drawer items not or wrongly initialized");
            return;
        }

        boolean itemIsPresent = (navDrawerAdapter.getItem(1).getId() == NavDrawerItem.ID.EXPLORE_AREA);

        if(enable && !itemIsPresent) {
            final String title = getString(R.string.menu_explore_area);
            final NavDrawerItem item  = new NavDrawerItem(NavDrawerItem.ID.EXPLORE_AREA, title, true);
            item.setRelatedObject(area);
            navDrawerItems.add(1, item);
            navDrawerAdapter.notifyDataSetChanged();
        } else if(!enable && itemIsPresent) {
            navDrawerItems.remove(1);
            navDrawerAdapter.notifyDataSetChanged();
        }
    }

    // React to a click on a nav drawer item
    private void selectNavDrawerItem(NavDrawerItem item) {
        if(item == null) {
            Log.e(LOG_TAG, "No drawer item to select");
            return;
        }
        final MapFragment mapFragment;

        switch (item.getId()) {
            case SELECT_AREA:
                mapFragment = switchMainFragmentToMap(true);
                mapFragment.showAreaSelection();
                break;
            case EXPLORE_AREA:
                mapFragment = switchMainFragmentToMap(true);
                mapFragment.onAreaSelected((Area) item.getRelatedObject());
                break;
            case SELECT_TOUR:
                mapFragment = switchMainFragmentToMap(true);
                mapFragment.showTourSelection();
                break;
            case EXPLORE_DATA:
                switchMainFragmentToExploreData(true);
                toggleNavDrawerItemToExploreMap(false, null);
                break;
            case LOAD_TOUR:
                switchMainFragmentToTourDownload(true);
                toggleNavDrawerItemToExploreMap(false, null);
                break;
            case ABOUT:
                toggleNavDrawerItemToExploreMap(false, null);
                final Intent intent = new Intent(MainActivity.this, AboutPageAcitvity.class);
                MainActivity.this.startActivity(intent);
                break;
            default:
                Toast.makeText(this, "Selected: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                break;
        }

        mDrawerLayout.closeDrawers();
    }
    // END drawer menu

    @Override
    public void onBackPressed() {
        // if the drawer is open, just close it and return
        if(mDrawerLayout.isDrawerOpen(findViewById(R.id.left_drawer))) {
            mDrawerLayout.closeDrawers();
            return;
        }
        // inform the current fragment of the back press and only get active if it wasn't handled
        MainActivityFragment fragment = (MainActivityFragment) getVisibleFragment();
        boolean fragmentReacted = fragment.reactToBackButtonPressed();
        if (!fragmentReacted) {
            super.onBackPressed();
        }
    }

    private void setDefaultActionBarTitleWithSuffix(String titleSuffix) {
        defaultToolbarTitle = getString(R.string.app_name);
        if(titleSuffix != null && !titleSuffix.isEmpty()) {
            defaultToolbarTitle += ": " + titleSuffix;
        }

        ActionBar bar = getSupportActionBar();
        if(bar != null) {
            bar.setTitle(defaultToolbarTitle);
        }
    }

    @Override
    public void onAreaSelected(Area area) {
        if(area != null) {
            this.setDefaultActionBarTitleWithSuffix(area.getName());
        }
        // disable the nav drawer item to explore the current map
        toggleNavDrawerItemToExploreMap(false, null);
    }

    @Override
    public void onTourSelected(Tour tour) {
        // setup the nav drawer item to explore the current map
        toggleNavDrawerItemToExploreMap(true, tour.getArea());
    }

    @Override
    public void onPlaceTapped(PlaceOnMap placeOnMap) {
        // do nothing
    }

    @Override
    public void onMapstopSelected(PlaceOnMap placeOnMap, int position) {
        // do nothing
    }

    @Override
    public void onMapstopSwitched(int position) {
        // do nothing
    }

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();

        //noinspection RestrictedApi (this disables Android Studio linting on the next line)
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
