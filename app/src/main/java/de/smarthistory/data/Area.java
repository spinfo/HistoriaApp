package de.smarthistory.data;

import java.util.List;

/**
 * Data object for an Area containing Tours
 */
public class Area {

    private final String name;

    private final List<Tour> tours;

    public Area(String name, List<Tour> tours) {
        this.name = name;
        this.tours = tours;
    }

    public String getName() {
        return name;
    }

    public List<Tour> getTours() {
        return tours;
    }
}
