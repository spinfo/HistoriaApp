package de.smarthistory.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

import org.osmdroid.util.GeoPoint;

/**
 * Data object for a place giving coordinates and a title to Mapstops
 */
public class Place {

    @DatabaseField(columnName = "id", id = true, dataType = DataType.LONG)
    private long id;

    @DatabaseField(columnName = "lat")
    private double lat;

    @DatabaseField(columnName = "lon")
    private double lon;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "area", foreign = true, foreignAutoRefresh = true)
    private Area area;

    public Place() {}

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

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }
}
