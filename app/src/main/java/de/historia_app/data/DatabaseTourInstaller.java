package de.historia_app.data;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;

import de.historia_app.ErrUtil;

/**
 * A class to handle the saving of a tour to the database. Deletes the old tour and saves the
 * new one.
 */
public class DatabaseTourInstaller {

    private static final String TAG = DatabaseTourInstaller.class.getSimpleName();

    private DatabaseHelper dbHelper;

    private Dao<Place, Long> placeDao;
    private Dao<Mapstop, Long> mapstopDao;
    private Dao<Page, Long> pageDao;
    private Dao<Mediaitem, Long> mediaitemDao;
    private Dao<Tour, Long> tourDao;
    private Dao<PersistentGeoPoint, Long> pointDao;
    private Dao<Area, Long> areaDao;

    DatabaseTourInstaller(final Context context) {
        this.dbHelper = new DatabaseHelper(context);

        this.placeDao = dbHelper.getPlaceDao();
        this.mapstopDao = dbHelper.getMapstopDao();
        this.pageDao = dbHelper.getPageDao();
        this.mediaitemDao = dbHelper.getMediaitemDao();
        this.tourDao = dbHelper.getTourDao();
        this.pointDao = dbHelper.getGeopointDao();
        this.areaDao = dbHelper.getAreaDao();

        // TODO: this could need some kind of a mutex structure, to make sure, that only one  installer is
        // running at any time
    }

    /**
     * Saves a tour. Does it's best to restore an old version if something fails, but does not
     * guarantee anything at the moment.
     *
     * @param tour The tour to save
     * @return  Whether the save went ok
     */
    boolean saveTour(Tour tour) {
        // TODO: Use transaction here
        // (Would need some synchronization in case of multiple saves at the same time.)
        // But see NOTE below for one of the risks. This might easily break in the future

        // first get the old version of the tour into memory
        final Tour oldTour;
        try {
            oldTour = tourDao.queryForId(tour.getId());
        } catch (SQLException e) {
            ErrUtil.failInDebug(TAG, e);
            return false;
        }

        // if there is a previous version, delete it but keep a copy in memory, that might be
        // restored on error
        if(oldTour != null) {
            // set tour's mapstops manually for re-insertion as they are not eager-fetched
            // NOTE: The big ugly assumption here is, that everything else IS eager-fetched...
            oldTour.setMapstops(oldTour.getMapstops());

            boolean deleteOk = deleteTour(oldTour);
            if(!deleteOk) {
                boolean insertAfterFailedDeleteOk = createOrUpdateTour(oldTour);
                Log.w(TAG, "Status of reinsert after a failed delete: " + insertAfterFailedDeleteOk);
                return false;
            }
        }

        // insert the new tour
        boolean insertOk = createOrUpdateTour(tour);
        if(!insertOk && oldTour != null) {
            // try to reinsert
            boolean reinsertAfterFailedInsert = createOrUpdateTour(oldTour);
            Log.w(TAG, "Status of reinsert after a failed insert: " + reinsertAfterFailedInsert);
            return false;
        }

        return insertOk;
    }


    private boolean createOrUpdateTour(Tour tour) {
        try {
            // setup the tour's mapstops with all related items
            for (Mapstop mapstop : tour.getMapstops()) {
                mapstop.setTour(tour);
                placeDao.createOrUpdate(mapstop.getPlace());
                mapstopDao.createOrUpdate(mapstop);

                // setup the mapstop's pages
                for (Page page : mapstop.getPages()) {
                    page.setMapstop(mapstop);
                    pageDao.createOrUpdate(page);

                    // and the page's mediaitems
                    for (Mediaitem item : page.getMedia()) {
                        item.setPage(page);
                        mediaitemDao.createOrUpdate(item);
                    }
                }
            }
            tourDao.createOrUpdate(tour);

            // save the tour track
            for (PersistentGeoPoint point : tour.getPersistableTrack()) {
                point.setTour(tour);
                pointDao.createOrUpdate(point);
            }

            // create with or update the area to the values supplied
            createOrUpdateArea(tour.getArea());
        } catch (SQLException e) {
            ErrUtil.failInDebug(TAG, e);
            return false;
        }
        return true;
    }



    private boolean deleteTour(Tour tour) {
        try {
            // Delete the tour track
            deleteEq(pointDao, "tour", tour);

            // Iterate over mapstops, delete all related items
            for (Mapstop mapstop : tour.getMapstops()) {

                // delete mediaitems of connected pages
                for (Page page : mapstop.getPages()) {
                    deleteEq(mediaitemDao, "page", page);
                }

                // delete the pages themselves
                deleteEq(pageDao, "mapstop", mapstop);

                // delete the place if this mapstop is the only mapstop connected to it
                long count = mapstopDao.queryBuilder()
                        .where().eq("place", mapstop.getPlace())
                        .countOf();
                if(count ==  1) {
                    placeDao.delete(mapstop.getPlace());
                }
            }

            // delete all the mapstops
            deleteEq(mapstopDao, "tour", tour);

            // delete the area if this is the only tour for it
            long count = tourDao.queryBuilder().setCountOf(true)
                            .where().eq("area", tour.getArea())
                            .countOf();
            if(count == 1) {
                areaDao.delete(tour.getArea());
            }

            // finally delete the tour
            tourDao.delete(tour);

            return true;
        } catch (SQLException e) {
            ErrUtil.failInDebug(TAG, e);
            return false;
        }
    }

    private boolean createOrUpdateArea(Area area) {
        try {
            Area fromDb = areaDao.queryForId(area.getId());
            if (fromDb != null) {
                // transfer id values of the points and update them
                area.getPoint1().setId(fromDb.getPoint1().getId());
                area.getPoint2().setId(fromDb.getPoint2().getId());
            }
            pointDao.createOrUpdate(area.getPoint1());
            pointDao.createOrUpdate(area.getPoint2());
            areaDao.createOrUpdate(area);

            return true;
        } catch (SQLException e) {
            ErrUtil.failInDebug(TAG, e);
            return false;
        }
    }

    // convenience method to issue a delete using the dao for every row having the given field value
    private static int deleteEq(Dao dao, String fieldName, Object value) throws SQLException {
        final DeleteBuilder deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq(fieldName, value);
        return deleteBuilder.delete();
    }


}
