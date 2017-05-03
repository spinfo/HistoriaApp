package de.smarthistory.data;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.util.List;

/**
 * A Singleton Facade to hide all data access behind
 * TODO: Is a singleton a good choice, espacially now that it needs a Context? Investigate!
 */
public class DataFacade {

    private static DataFacade instance = null;

    private ExampleDataProvider exampleDataProvider;

    private DatabaseDataProvider dbDataProvider;

    // private constructor to disallow other instances
    private DataFacade(Context context) {
        this.exampleDataProvider = new ExampleDataProvider();
        this.dbDataProvider = new DatabaseDataProvider(context);
    };

    // return the singleton instance that manages access to all data
    public static DataFacade getInstance(Context context) {
        if (instance == null) {
            instance = new DataFacade(context);
        }
        return instance;
    }

    public Area getAreaById(long id) {
        return dbDataProvider.getAreaById(id);
    }

    public List<Area> getAreas() {
        return dbDataProvider.getAreas();
    }

    public Mapstop getMapstopById(long id) { return dbDataProvider.getMapstopById(id); }

    public Area getDefaultArea() { return dbDataProvider.getDefaultArea(); }

    public Tour getDefaultTour() { return dbDataProvider.getDefaultTour(); }

    public Tour getTourById(long id) { return dbDataProvider.getTourById(id); }

    public boolean saveTour(Tour tour) { return dbDataProvider.saveTour(tour); }


    public Lexicon getLexicon() {
        return exampleDataProvider.getLexicon();
    }

    public List<LexiconEntry> getLexiconEntries() {
        return exampleDataProvider.getLexiconEntries();
    }

    public LexiconEntry getLexiconEntryById(long id) {
        return exampleDataProvider.getLexiconEntryById(id);
    }
}
