package de.smarthistory.data;

import android.content.res.AssetManager;

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

    public Area getAreaById(long id) {
        return dataProvider.getAreaById(id);
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

    public String getLexiconEntryUri(long lexiconEntryId) {
        return dataProvider.getLexiconEntryUri(lexiconEntryId);
    }

    public Tour getTourById(long id) { return dataProvider.getTourById(id); }

    public Lexicon getLexicon() {
        return dataProvider.getLexicon();
    }

    public List<LexiconEntry> getLexiconEntries() {
        return dataProvider.getLexiconEntries();
    }

    public LexiconEntry getLexiconEntryById(long id) {
        return dataProvider.getLexiconEntryById(id);
    }

    public void prepareAssets(AssetManager assetManager, File externalDir) {
        dataProvider.prepareAssets(assetManager, externalDir);
    }
}
