package de.smarthistory.data;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Data object for a Mapstop which is part of a Tour and has a place and a few html pages.
 */
public class Mapstop {

    // server id of the mapstop
    @DatabaseField(columnName = "id", id = true, dataType = DataType.LONG)
    private long id;

    // the place the mapstop is displayed on
    @DatabaseField(columnName = "place", foreign = true, foreignAutoRefresh = true)
    private Place place;

    // the tour the mapstop belongs to
    @DatabaseField(columnName = "tour", foreign = true, foreignAutoRefresh = true)
    private Tour tour;

    // the mapstops name as shown to the user
    @DatabaseField(columnName = "name")
    private String name;

    // a short description of the mapstop shown to the user
    @DatabaseField(columnName = "description")
    private String description;

    // the mapstop's main content: (html) pages
    @ForeignCollectionField(columnName = "pages", eager = true)
    private Collection<Page> pages;

    // empty constructor needed for YAML parsing
    protected Mapstop() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPageAmount() {
        return this.pages.size();
    }

    public List<Page> getPages() {
        return new ArrayList<>(pages);
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public boolean hasPage(int idx) {
        return (idx <= this.getPageAmount() && idx > 0);
    }
}
