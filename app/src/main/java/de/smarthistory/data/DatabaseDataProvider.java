package de.smarthistory.data;


import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

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
            // TODO: THis might not be effective. Is there a better way?
            return dbHelper.getAreaDao().queryForAll().get(0);
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
            Area area = getDefaultArea();
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

}
