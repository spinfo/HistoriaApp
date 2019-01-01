package de.historia_app.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Data object for an Area containing Tours (otherwise: a name connected to a geographic rectangle)
 */
public class Area implements Serializable, AreaSortUtil.ObjectWithName {

    // the server's id for this area
    @DatabaseField(columnName = "id", id = true)
    private long id;

    // The name of the area as displayed to the user
    @DatabaseField(columnName = "name")
    private String name;

    // The tours within this area, not to be fetched eagerly as they do some eager fetching
    // themselves.
    @ForeignCollectionField(columnName = "tours", orderColumnName = "version", orderAscending = false)
    private Collection<Tour> tours;

    // One corner of the area's rectangle
    @DatabaseField(columnName = "point1", foreign = true, foreignAutoRefresh = true)
    private PersistentGeoPoint point1;

    // The other corner of the area's rectangle
    @DatabaseField(columnName = "point2", foreign = true, foreignAutoRefresh = true)
    private PersistentGeoPoint point2;

    public Area() {
        this.tours = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get a a copy of the area's tours as a list
     */
    public List<Tour> getTours() {
        return new ArrayList<>(tours);
    }

    public void setTours(Collection<Tour> tours) {
        this.tours = tours;
    }

    public PersistentGeoPoint getPoint1() {
        return point1;
    }

    public void setPoint1(PersistentGeoPoint point1) {
        this.point1 = point1;
    }

    public PersistentGeoPoint getPoint2() {
        return point2;
    }

    public void setPoint2(PersistentGeoPoint point2) {
        this.point2 = point2;
    }
}
