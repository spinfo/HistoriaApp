package de.smarthistory.data;


import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import de.smarthistory.ErrUtil;

public class DatabaseDataProvider {

    private static final String LOG_TAG = DatabaseDataProvider.class.getSimpleName();

    private DatabaseHelper dbHelper;

    public DatabaseDataProvider(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    Area getAreaById(long id) {
        try {
            return dbHelper.getAreaDao().queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Area getDefaultArea() {
        try {
            // the default area is the first area listed
            PreparedQuery<Area> query = dbHelper.getAreaDao().queryBuilder().prepare();
            return dbHelper.getAreaDao().queryForFirst(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<Area> getAreas() {
        try {
            return dbHelper.getAreaDao().queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Mapstop getMapstopById(long id) {
        try {
            return dbHelper.getMapstopDao().queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Tour getDefaultTour() {
        try {
            // the default tour belongs to the default area, return null if there is none
            Area area = getDefaultArea();
            if(area == null) {
                return null;
            }
            // return the first tour of the default area
            Dao<Tour, Long> tourDao = dbHelper.getTourDao();
            PreparedQuery<Tour> query = tourDao.queryBuilder().where().eq("area", area).prepare();
            return tourDao.queryForFirst(query);
        }  catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Tour getTourById(long id) {
        try {
            return dbHelper.getTourDao().queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    boolean saveTour(Tour tour) {
        Dao<Place, Long> placeDao = dbHelper.getPlaceDao();
        Dao<Mapstop, Long> mapstopDao = dbHelper.getMapstopDao();
        Dao<Page, Long> pageDao = dbHelper.getPageDao();
        Dao<Mediaitem, Long> mediaitemDao = dbHelper.getMediaitemDao();
        Dao<Tour, Long> tourDao = dbHelper.getTourDao();
        Dao<PersistentGeoPoint, Long> pointDao = dbHelper.getGeopointDao();
        Dao<Area, Long> areaDao = dbHelper.getAreaDao();

        try {
            for (Mapstop m : tour.getMapstops()) {
                m.setTour(tour);
                placeDao.createOrUpdate(m.getPlace());
                mapstopDao.createOrUpdate(m);
                for (Page p : m.getPages()) {
                    p.setMapstop(m);
                    pageDao.createOrUpdate(p);
                    for (Mediaitem item : p.getMedia()) {
                        item.setPage(p);
                        // only persist the mediaitem if there is not one already for this mapstop
                        // with the same name
                        PreparedQuery<Mediaitem> existingMediaitemsQuery = mediaitemDao.queryBuilder()
                                .setCountOf(true)
                                .where().eq("guid", item.getGuid())
                                .and().eq("page", p).prepare();
                        Long count = mediaitemDao.countOf(existingMediaitemsQuery);
                        if (count == 0) {
                            mediaitemDao.create(item);
                        }
                    }
                }
            }
            tourDao.createOrUpdate(tour);

            // delete a previous track if there is any, then persist the new one
            DeleteBuilder trackDeleteBuilder = pointDao.deleteBuilder();
            trackDeleteBuilder.where().eq("tour", tour);
            trackDeleteBuilder.delete();
            for (PersistentGeoPoint point : tour.getPersistableTrack()) {
                point.setTour(tour);
                pointDao.createOrUpdate(point);
            }

            // create or update the area
            Area newArea = tour.getArea();
            Area fromDb = areaDao.queryForId(newArea.getId());
            if (fromDb != null) {
                // transfer id values of the points and update them
                newArea.getPoint1().setId(fromDb.getPoint1().getId());
                newArea.getPoint2().setId(fromDb.getPoint2().getId());
            }
            pointDao.createOrUpdate(newArea.getPoint1());
            pointDao.createOrUpdate(newArea.getPoint2());
            areaDao.createOrUpdate(newArea);
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
            return false;
        }
        return true;
    }

    List<TourOnMap> getToursOnMap() {
        try {
            return dbHelper.getTourOnMapDao().queryForAll();
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
            return Collections.emptyList();
        }
    }

    boolean saveToursOnMap(List<TourOnMap> tours) {
        try {
            // clear the whole table
            TableUtils.clearTable(dbHelper.getConnectionSource(), TourOnMap.class);

            // insert the given tours
            if(tours != null) {
                int rowsInserted = dbHelper.getTourOnMapDao().create(tours);
                return (rowsInserted == tours.size());
            } else {
                return false;
            }
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
            return false;
        }
    }

}
