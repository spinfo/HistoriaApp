package de.smarthistory.data;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class FileService {

    private static final String LOG_TAG = FileService.class.getSimpleName();

    private Context context;

    private File mainContentDir;

    public FileService(Context context) {
        this.context = context;

        // initialize the main content directory if it does not exist
        boolean status = initializeMainContentDir();
        if (!status) {
            throw new RuntimeException("Could not create the main content directory.");
        }

        // initialize the example tour if the main content dir is empty
        if (mainContentDir.list() == null || mainContentDir.list().length == 0) {
            status = initializeExampleData();
            if(!status) {
                throw new RuntimeException("Could not initialize the example data.");
            }
        }
    }

    private boolean initializeMainContentDir() {
        File topDir = context.getFilesDir();
        String path = topDir.getPath() + "/smart-history-tours";
        mainContentDir = new File(path);
        Log.i(LOG_TAG, "Using as main content dir: " + mainContentDir.getAbsolutePath());
        try {
            if(!mainContentDir.exists()) {
                mainContentDir.mkdir();
            }
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Error while creating main content dir: " + e.getMessage());
            return false;
        }
        return (mainContentDir.exists() && mainContentDir.isDirectory());
    }

    private boolean initializeExampleData() {
        try {
            InputStream exampleIs = context.getAssets().open("example-tour.yaml");
            Tour tour = TourDeserialiser.parseTour(exampleIs);

            // TODO: Move to a different place, DataFacade?
            DatabaseHelper db = new DatabaseHelper(this.context);
            Dao<Place, Long> placeDao = db.getPlaceDao();
            Dao<Mapstop, Long> mapstopDao = db.getMapstopDao();
            Dao<Page, Long> pageDao = db.getPageDao();
            Dao<Mediaitem, Long> mediaitemDao = db.getMediaitemDao();
            Dao<Tour, Long> tourDao = db.getTourDao();
            Dao<PersistentGeoPoint, Long> pointDao = db.getGeopointDao();
            Dao<Area, Long> areaDao = db.getAreaDao();

            for(Mapstop m : tour.getMapstops()) {
                m.setTour(tour);
                placeDao.createOrUpdate(m.getPlace());
                mapstopDao.createOrUpdate(m);
                for(Page p : m.getPages()) {
                    p.setMapstop(m);
                    pageDao.createOrUpdate(p);
                    for(Mediaitem item : p.getMedia()) {
                        item.setPage(p);
                        // only persist the mediaitem if there is not one already for this mapstop
                        // with the same name
                        PreparedQuery<Mediaitem> existingMediaitemsQuery = mediaitemDao.queryBuilder()
                                .setCountOf(true)
                                .where().eq("guid", item.getGuid())
                                .and().eq("page", p).prepare();
                        Long count = mediaitemDao.countOf(existingMediaitemsQuery);
                        if(count == 0) {
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
            for(PersistentGeoPoint point : tour.getPersistableTrack()) {
                point.setTour(tour);
                pointDao.createOrUpdate(point);
            }

            // create or update the area
            Area newArea = tour.getArea();
            Area fromDb = areaDao.queryForId(newArea.getId());
            if(fromDb != null) {
                // transfer id values of the points and update them
                newArea.getPoint1().setId(fromDb.getPoint1().getId());
                newArea.getPoint2().setId(fromDb.getPoint2().getId());
            }
            pointDao.createOrUpdate(newArea.getPoint1());
            pointDao.createOrUpdate(newArea.getPoint2());
            areaDao.createOrUpdate(newArea);


            // TODO: Remove below
            tour = tourDao.queryForId(1l);

            Place p = placeDao.queryForId(3l);
            Log.i("---", p.getName());

            Log.i("---", tour.getArea().getName());
            Log.i("---", p.getArea().getName());

            Mapstop m = mapstopDao.queryForId(31l);
            Log.i("---", m.getName());
            Log.i("---", m.getPlace().getName());
            Log.i("---", m.getPages().get(1).getGuid());

            Log.i("---", m.getTour().getName());
            Log.i("---", tour.getName());
            Log.i("---", "" + tour.getTrack().get(3).getLatitude());

            Log.i("---", "" + m.getPages().get(2).getMedia().size());
            Log.i("---", "" + m.getPages().get(2).getMedia().get(1).getGuid());

            Log.i("---", "areas: " + areaDao.countOf());
            Log.i("---", "mapstops: " + mapstopDao.countOf());
            Log.i("---", "mediaitems: " + mediaitemDao.countOf());
            Log.i("---", "pages: " + pageDao.countOf());
            Log.i("---", "points: " + pointDao.countOf());
            Log.i("---", "places: " + placeDao.countOf());
            Log.i("---", "tours: " + tourDao.countOf());


        } catch(IOException | SQLException e){
            Log.e(LOG_TAG, "Error while creating the example tour: " + e.getMessage());
            return false;
        }
        return true;
    }

}
