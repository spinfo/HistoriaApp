package de.historia_app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import de.historia_app.mappables.TourOnMap;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String LOGTAG = DatabaseHelper.class.getSimpleName();

    // TODO: Set the final name (this will change during development to avoid dealing with version changes, but must be fixed on release)
    private static final String DATABASE_NAME = "historia-app-dev-3.db";
    private static final int DATABASE_VERSION = 2;

    private Dao<Place, Long> placeDao = null;
    private Dao<Mapstop, Long> mapstopDao = null;
    private Dao<Page, Long> pageDao = null;
    private Dao<Mediaitem, Long> mediaitemDao = null;
    private Dao<Tour, Long> tourDao = null;
    private Dao<PersistentGeoPoint, Long> geopointDao = null;
    private Dao<Area, Long> areaDao = null;
    private Dao<Scene, Long> sceneDao = null;
    private Dao<Coordinate, Long> coordinateDao = null;
    private Dao<TourOnMap, Long> tourOnMapDao = null;
    private Dao<LexiconEntry, Long> lexiconEntryDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Place.class);
            TableUtils.createTable(connectionSource, Mapstop.class);
            TableUtils.createTable(connectionSource, Page.class);
            TableUtils.createTable(connectionSource, Mediaitem.class);
            TableUtils.createTable(connectionSource, Tour.class);
            TableUtils.createTable(connectionSource, PersistentGeoPoint.class);
            TableUtils.createTable(connectionSource, Area.class);
            TableUtils.createTable(connectionSource, Scene.class);
            TableUtils.createTable(connectionSource, Coordinate.class);
            TableUtils.createTable(connectionSource, TourOnMap.class);
            TableUtils.createTable(connectionSource, LexiconEntry.class);
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            try {
                getMapstopDao().executeRaw("ALTER TABLE " + getMapstopDao().getTableName() + " ADD COLUMN `pos` int NULL DEFAULT null");
                getMapstopDao().executeRaw("ALTER TABLE " + getMapstopDao().getTableName() + " ADD COLUMN `scene` bigint NULL DEFAULT null");
                getMapstopDao().executeRaw("ALTER TABLE " + getMapstopDao().getTableName() + " ADD COLUMN `coordinate` bigint NULL DEFAULT null");
                getMapstopDao().executeRaw("ALTER TABLE " + getMapstopDao().getTableName() + " ADD COLUMN `type` string NULL DEFAULT null");
                db.execSQL("create table "
                        + getCoordinateDao().getTableName() + "("
                        + "id bigint primary key autoincrement, "
                        + "mapstop bigint, "
                        + "scene bigint, "
                        + "x float, "
                        + "y float);"
                );
                Log.i(LOGTAG, "Successfully upgraded database from version 1 to version 2.");
            } catch (SQLException e) {
                Log.e(LOGTAG, "Database upgrade from version 1 to version 2 failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Dao<Place, Long> getPlaceDao() {
        if(placeDao == null) {
            placeDao = getMyDaoRuntimeExcept(Place.class);
        }
        return placeDao;
    }

    public Dao<Mapstop, Long> getMapstopDao() {
        if(mapstopDao == null) {
            mapstopDao = getMyDaoRuntimeExcept(Mapstop.class);
        }
        return mapstopDao;
    }

    public Dao<Page, Long> getPageDao() {
        if(pageDao == null) {
            pageDao = getMyDaoRuntimeExcept(Page.class);
        }
        return pageDao;
    }

    public Dao<Mediaitem, Long> getMediaitemDao() {
        if(mediaitemDao == null) {
            mediaitemDao = getMyDaoRuntimeExcept(Mediaitem.class);
        }
        return mediaitemDao;
    }

    public Dao<Tour, Long> getTourDao() {
        if(tourDao == null) {
            tourDao = getMyDaoRuntimeExcept(Tour.class);
        }
        return tourDao;
    }

    public Dao<PersistentGeoPoint, Long> getGeopointDao() {
        if(geopointDao == null) {
            geopointDao = getMyDaoRuntimeExcept(PersistentGeoPoint.class);
        }
        return geopointDao;
    }

    public Dao<Area, Long> getAreaDao() {
        if(areaDao == null) {
            areaDao = getMyDaoRuntimeExcept(Area.class);
        }
        return areaDao;
    }

    public Dao<Scene, Long> getSceneDao() {
        if(sceneDao == null) {
            sceneDao = getMyDaoRuntimeExcept(Scene.class);
        }
        return sceneDao;
    }

    public Dao<Coordinate, Long> getCoordinateDao() {
        if(coordinateDao == null) {
            coordinateDao = getMyDaoRuntimeExcept(Coordinate.class);
        }
        return coordinateDao;
    }

    public Dao<TourOnMap, Long> getTourOnMapDao() {
        if(tourOnMapDao == null) {
            tourOnMapDao = getMyDaoRuntimeExcept(TourOnMap.class);
        }
        return tourOnMapDao;
    }

    public Dao<LexiconEntry, Long> getLexiconEntryDao() {
        if(lexiconEntryDao == null) {
            lexiconEntryDao = getMyDaoRuntimeExcept(LexiconEntry.class);
        }
        return lexiconEntryDao;
    }

    // convenience method that wraps the SQL Exception on Dao Creation
    private <D extends Dao<T, ?>, T> D getMyDaoRuntimeExcept(Class<T> clazz) {
        D result = null;
        try {
            result = getDao(clazz);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void close(){
        placeDao = null;
        mapstopDao = null;
        pageDao = null;
        mediaitemDao = null;
        tourDao = null;
        geopointDao = null;
        areaDao = null;
        sceneDao = null;
        tourOnMapDao = null;
        lexiconEntryDao = null;

        super.close();
    }
}
