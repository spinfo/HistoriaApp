package de.historia_app.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Data object for a Mapstop which is part of a Tour and has a place and a few html pages.
 */
public class Scene {

    @DatabaseField(columnName = "id", id = true, dataType = DataType.LONG)
    private long id;

    @DatabaseField(columnName = "pos")
    private int pos;

    @DatabaseField(columnName = "tour", foreign = true, foreignAutoRefresh = true)
    private Tour tour;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "title")
    private String title;

    @DatabaseField(columnName = "description")
    private String description;

    @DatabaseField(columnName = "excerpt")
    private String excerpt;

    @DatabaseField(columnName = "src")
    private String src;

    @ForeignCollectionField(columnName = "mapstops")
    private Collection<Mapstop> mapstops;

    @ForeignCollectionField(columnName = "coordinates")
    private Collection<Coordinate> coordinates;

    protected Scene() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSrc() { return src; }

    public void setSrc(String src) { this.src = src; }

    public List<Mapstop> getMapstops() {
        if(mapstops == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mapstops);
    }

    public void setMapstops(Collection<Mapstop> mapstops) {
        this.mapstops = mapstops;
    }

    public List<Coordinate> getCoordinates() {
        if(coordinates == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(coordinates);
    }

    public void setCoordinates(Collection<Coordinate> coordinates) { this.coordinates = coordinates; }

    public boolean hasCoordinates() { return this.coordinates != null && this.coordinates.size() > 0; }
}
