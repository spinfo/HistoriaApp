package de.smarthistory.data;

import java.util.List;

/**
 * Data object for a Tour which is part of an area and contains Mapstops
 */
public class Tour {

    private String name;

    private List<Mapstop> mapstops;

    public Tour(String name, List<Mapstop> mapstops) {
        this.name = name;
        this.mapstops = mapstops;
    }

    public String getName() {
        return name;
    }

    public List<Mapstop> getMapstops() {
        return mapstops;
    }
}
