package de.smarthistory.data;

import org.osmdroid.util.GeoPoint;

/**
 * Data object for a place giving coordinates and a title to Mapstops
 */
public class Place {

    private long id;

    private double lat;

    private double lon;

    private String name;

    public Place() {}

    public Place(String name, GeoPoint location) {
        this.name = name;
        this.lat = location.getLatitude();
        this.lon = location.getLongitude();
    }

    public Place(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public GeoPoint getLocation() {
        return  new GeoPoint(this.lat, this.lon);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
