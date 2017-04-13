package de.smarthistory.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data object for an Area containing Tours
 */
public class Area {

    private String name;

    private List<Tour> tours;

    private long id;

    public Area() {
        this.tours = new ArrayList<>();
    }

    public Area(final String name, final List<Tour> tours, final long id) {
        this.name = name;
        this.tours = tours;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<Tour> getTours() {
        return tours;
    }

    public long getId() {
        return id;
    }
}
