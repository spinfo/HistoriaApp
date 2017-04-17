package de.smarthistory.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

/**
 * A class implementing the IGeoPoint interface, but that we can also save with ormlite.
 */
@DatabaseTable(tableName = "geopoint")
class PersistentGeoPoint implements IGeoPoint {

    @DatabaseField(columnName = "id", generatedId = true)
    private long id;

    @DatabaseField(columnName = "latitude")
    private double latitude;

    @DatabaseField(columnName = "longitude")
    private double longitude;

    // if this geo point is connected to a tour (as part of the track), it is mapped here
    @DatabaseField(columnName = "tour", foreign = true)
    private Tour tour;

    // we need the default constructor for database persistence
    protected PersistentGeoPoint() {
        this(0.0, 0.0);
    }

    protected PersistentGeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // as this is a deprecated method in the interface, we do not implement it efficiently
    @Override
    @Deprecated
    public int getLatitudeE6() {
        return (new GeoPoint(this.latitude, this.longitude)).getLatitudeE6();
    }

    // as this is a deprecated method in the interface, we do not implement it efficiently
    @Override
    public int getLongitudeE6() {
        return (new GeoPoint(this.latitude, this.longitude)).getLongitudeE6();
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    @Override
    public double getLongitude() {
        return this.longitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    protected static PersistentGeoPoint useOrCreateNew(IGeoPoint point) {
        if(point.getClass() == PersistentGeoPoint.class) {
            return (PersistentGeoPoint) point;
        } else {
            return new PersistentGeoPoint(point.getLatitude(), point.getLongitude());
        }
    }
}
