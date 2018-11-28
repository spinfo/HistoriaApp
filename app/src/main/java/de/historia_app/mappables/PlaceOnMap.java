package de.historia_app.mappables;

import java.util.ArrayList;
import java.util.List;

import de.historia_app.data.Place;

/**
 * A wrapper for a place's state one the map
 */
public class PlaceOnMap {

    // the mapstopsOnMap the place is connected to
    private List<MapstopOnMap> mapstopsOnMap;

    // whether the place has a mapstop that is the begin of a tour
    private boolean hasTourBeginMapstop;

    // whether the place has a mapstop that is part of an indoor tour
    private boolean hasIndoorTourMapstop;

    // the place of which this is the representation on the map
    private Place place;

    public PlaceOnMap(final Place place) {
        this.place = place;
        // initialize to a capacity of 1 as that is the most common scenario
        this.mapstopsOnMap = new ArrayList<>(1);
        this.hasTourBeginMapstop = false;
        this.hasIndoorTourMapstop = false;
    }

    public List<MapstopOnMap> getMapstopsOnMap() {
        return mapstopsOnMap;
    }

    public void addMapstopOnMap(MapstopOnMap mapstopOnMap) {
        if(mapstopOnMap.isFirstInTour()) {
            this.hasTourBeginMapstop = true;
        }
        if(mapstopOnMap.isPartOfIndoorTour()) {
            this.hasIndoorTourMapstop = true;
        }
        this.mapstopsOnMap.add(mapstopOnMap);
    }

    public boolean hasTourBeginMapstop() {
        return hasTourBeginMapstop;
    }

    public boolean hasIndoorTourMapstop() {
        return hasIndoorTourMapstop;
    }

    public Place getPlace() {
        return place;
    }
}

