package de.smarthistory.data;

import org.osmdroid.util.GeoPoint;

/**
 * Data object for a place giving coordinates and a title to Mapstops
 */
public class Place {

    private final GeoPoint location;

    private final String name;

    public Place(String name, GeoPoint location) {
        this.name = name;
        this.location = location;
    }

    public Place(String name, double lat, double lon) {
        this.name = name;
        this.location = new GeoPoint(lat, lon);
    }

    public String getName() {
        return name;
    }

    public GeoPoint getLocation() {
        return  location;
    }

}
