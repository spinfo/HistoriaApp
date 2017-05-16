package de.smarthistory.data;

import com.j256.ormlite.field.DatabaseField;

/**
 * A wrapper for a tour's state one the map, that may be persisted to the database
 */
public class TourOnMap {

    @DatabaseField(columnName = "id", generatedId = true)
    private long id;

    // the actual tour that this object represents a drawable state of
    @DatabaseField(columnName = "tour", foreign = true, foreignAutoRefresh = true)
    private Tour tour;

    // whether all mapstops of the tour are displayed as markers (or only the first)
    @DatabaseField(columnName = "all_mapstops_displayed")
    private boolean displayingAllMapstops;

    // default constructor for ormlite
    public TourOnMap() {
        displayingAllMapstops = true;
    }

    public TourOnMap(Tour tour) {
        super();
        setTour(tour);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public boolean isDisplayingAllMapstops() {
        return displayingAllMapstops;
    }

    public void setDisplayingAllMapstops(boolean displayingAllMapstops) {
        this.displayingAllMapstops = displayingAllMapstops;
    }
}
