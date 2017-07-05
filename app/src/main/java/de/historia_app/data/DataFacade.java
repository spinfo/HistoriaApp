package de.historia_app.data;

import android.content.Context;

import java.util.List;

import de.historia_app.mappables.TourOnMap;

/**
 * A Facade class to hide all data access behind. (Not a Singleton because it depends on a Context
 * that might get lost.)
 */
public class DataFacade {

    private DatabaseDataProvider dbDataProvider;

    private final Context context;

    // private constructor to disallow other instances
    public DataFacade(Context context) {
        this.context = context;
        this.dbDataProvider = new DatabaseDataProvider(context);
    };

    public Area getAreaById(long id) {
        return dbDataProvider.getAreaById(id);
    }

    public List<Area> getAreas() {
        return dbDataProvider.getAreas();
    }

    public Mapstop getMapstopById(long id) { return dbDataProvider.getMapstopById(id); }

    public Area getDefaultArea() { return dbDataProvider.getDefaultArea(); }

    public long getToursAmount(Area area) {
        return dbDataProvider.getTourAmount(area);
    }

    public Tour getDefaultTour() { return dbDataProvider.getDefaultTour(); }

    public Tour getTourById(long id) { return dbDataProvider.getTourById(id); }

    public boolean saveTour(Tour tour) {
        return dbDataProvider.saveTour(tour, this.context);
    }

    public List<TourOnMap> getToursOnMap() {
        return dbDataProvider.getToursOnMap();
    }

    public boolean saveToursOnMap(List<TourOnMap> toursOnMap) {
        return dbDataProvider.saveToursOnMap(toursOnMap);
    }

    public Lexicon getLexicon() {
        return dbDataProvider.getLexicon();
    }

    public LexiconEntry getLexiconEntryById(long id) {
        return dbDataProvider.getLexiconEntryById(id);
    }

    public boolean saveLexiconEntries(List<LexiconEntry> entries) {
        return dbDataProvider.saveLexiconEntries(entries);
    }
}
