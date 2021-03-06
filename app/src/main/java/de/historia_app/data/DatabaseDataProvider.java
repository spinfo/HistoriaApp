package de.historia_app.data;


import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.historia_app.ErrUtil;
import de.historia_app.mappables.TourOnMap;

public class DatabaseDataProvider {

    private static final String LOG_TAG = DatabaseDataProvider.class.getSimpleName();

    private DatabaseHelper dbHelper;

    DatabaseDataProvider(Context context) {
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

    private long getTourVersion(long tourId, long defaultTo) {
        try {
            String[] result = dbHelper.getTourDao().queryBuilder()
                    .selectColumns("version").where().idEq(tourId)
                    .queryRaw().getFirstResult();
            if (result == null || result.length == 0 || result[0] == null) {
                return defaultTo;
            }
            return Long.parseLong(result[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    TourRecord.InstallStatus determineInstallStatus(TourRecord record) {
        long defaultVersion = -1L;
        TourRecord.InstallStatus result;
        long versionInstalled = getTourVersion(record.getTourId(), defaultVersion);
        if (versionInstalled == defaultVersion) {
            result = TourRecord.InstallStatus.NOT_INSTALLED;
        } else  if (versionInstalled == record.getVersion()) {
            result = TourRecord.InstallStatus.UP_TO_DATE;
        } else {
            result = TourRecord.InstallStatus.UPDATE_AVAILABLE;
        }
        return result;
    }

    /**
     * Get the amount of tours in the area without fetching them.
     *
     * @param area The area to find out about.
     * @return The amount of tours in the area.
     */
    long getTourAmount(Area area) {
        if(area == null) {
            Log.w(LOG_TAG, "Not counting tours on null area");
            return 0;
        }
        try{
            return dbHelper.getTourDao().queryBuilder().where().eq("area", area).countOf();
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
            return 0;
        }
    }

    boolean saveTour(Tour tour, Context context) {
        DatabaseTourInstaller installer = new DatabaseTourInstaller(context);
        return installer.saveTour(tour);
    }

    List<TourOnMap> getToursOnMap() {
        try {
            deleteInvalidToursOnMap();
            return dbHelper.getTourOnMapDao().queryForAll();
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
            return Collections.emptyList();
        }
    }

    private void deleteInvalidToursOnMap() {
        try {
            DeleteBuilder<TourOnMap, Long> builder = dbHelper.getTourOnMapDao().deleteBuilder();
            builder.where().notIn("tour", dbHelper.getTourDao().queryForAll());
            builder.delete();
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
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

    Place getPlaceById(long id) {
        try {
            return dbHelper.getPlaceDao().queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    boolean saveLexiconEntries(List<LexiconEntry> entries) {
        if(entries == null || entries.isEmpty()) {
            Log.w(LOG_TAG, "Attempt to save empty or null list of lexicon entries");
            return true;
        }

        try {
            for(LexiconEntry entry : entries) {
                dbHelper.getLexiconEntryDao().createOrUpdate(entry);
            }
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
            return false;
        }
        return true;
    }

    protected Lexicon getLexicon() {
        Lexicon lexicon = new Lexicon();

        List<LexiconEntry> entries = Collections.emptyList();
        try {
            entries = dbHelper.getLexiconEntryDao().queryForAll();
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
        }

        for (LexiconEntry entry : entries) {
            lexicon.addEntry(entry);
        }

        return lexicon;
    }

    protected LexiconEntry getLexiconEntryById(long id) {
        try {
            return dbHelper.getLexiconEntryDao().queryForId(id);
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
            return null;
        }
    }


    public List<Mediaitem> getMediaitemsFor(Tour tour) {
        // since the connection mapstop-page-mediaitem is eagerly fetched, no special query is needed
        List<Mediaitem> result = new ArrayList<>();
        for (Mapstop mapstop : tour.getMapstops()) {
            for (Page page : mapstop.getPages()) {
                result.addAll(page.getMedia());
            }
        }
        return result;
    }


    public Set<Long> getTourIdsInArea(long areaId) {
        QueryBuilder<Tour, Long> builder = dbHelper.getTourDao().queryBuilder();
        Set<Long> result = new TreeSet<>();
        try {
            GenericRawResults<String[]> results = builder.selectColumns("id").where().eq("area", areaId).queryRaw();
            for (String[] row : results.getResults()) {
                if (row != null && row.length == 1) {
                    result.add(Long.valueOf(row[0]));
                }
            }
        } catch (SQLException e) {
            ErrUtil.failInDebug(LOG_TAG, e);
            result = Collections.emptySet();
        }
        return result;
    }
}
