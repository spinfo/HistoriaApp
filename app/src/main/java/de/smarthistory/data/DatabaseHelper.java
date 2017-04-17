package de.smarthistory.data;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // TODO change back to private
    public static final String DATABASE_NAME = "smart-history.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Place, Long> placeDao = null;
    private Dao<Mapstop, Long> mapstopDao = null;
    private Dao<Page, Long> pageDao = null;
    private Dao<Mediaitem, Long> mediaitemDao = null;
    private Dao<Tour, Long> tourDao = null;
    private Dao<PersistentGeoPoint, Long> geopointDao = null;
    private Dao<Area, Long> areaDao = null;

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
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {}

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

        super.close();
    }
}
