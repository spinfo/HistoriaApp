package de.smarthistory.data;

import java.util.List;

/**
 * Data object for a Mapstop which is part of a Tour and has a place and a few html pages.
 */
public class Mapstop {

    // server id of the mapstop
    private long id;

    // the place the mapstop is displayed on
    private Place place;

    // the mapstops name as shown to the user
    private String name;

    // a short description of the mapstop shown to the user
    private String description;

    // TODO: remove
    private int pageAmount;

    // the mapstop's main content: (html) pages
    private List<Page> pages;

    // empty constructor needed for YAML parsing
    protected Mapstop() {}

    public Mapstop(long id, Place place, String title, String shortDescription, int pageAmount) {
        this.id = id;
        this.place = place;
        this.name = title;
        this.description = shortDescription;
        this.pageAmount = pageAmount;
    }

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
        return pageAmount;
    }

    public void setPageAmount(int pageAmount) {
        this.pageAmount = pageAmount;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public boolean hasPage(int idx) {
        return (idx <= pageAmount && idx > 0);
    }
}
