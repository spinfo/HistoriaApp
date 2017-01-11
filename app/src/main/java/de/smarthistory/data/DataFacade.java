package de.smarthistory.data;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * A Singleton Facade to hide all data access behind
 */
public class DataFacade {

    private static DataFacade instance = null;

    private ExampleDataProvider dataProvider;

    // private constructor to disallow other instances
    private DataFacade() {
        dataProvider = new ExampleDataProvider();
    };

    // return the Singleton instance that manages all data
    public static DataFacade getInstance() {
        if (instance == null) {
            instance = new DataFacade();
        }
        return instance;
    }


    public List<Area> getAreas() {
        return dataProvider.getAreas();
    }

    public List<Mapstop> getMapstops() {
        return dataProvider.getMapstops();
    }

    public Mapstop getMapstopById(long id) { return dataProvider.getMapstopById(id); }

    public Area getCurrentArea() { return dataProvider.getCurrentArea(); }

    public Tour getCurrentTour() { return dataProvider.getCurrentTour(); }

    public String getPageUriForMapstop(Mapstop mapstop, Integer pageNo) {
        return dataProvider.getPageUriForMapstop(mapstop, pageNo);
    }

}
