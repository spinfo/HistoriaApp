package de.smarthistory.data;

/**
 * Data object for a Mapstop which is part of a Tour and has a place.
 */
public class Mapstop {

    private final long id;

    private final Place place;

    private final String title;

    private final String shortDescription;

    private int pageAmount;

    public Mapstop(long id, Place place, String title, String shortDescription, int pageAmount) {
        this.id = id;
        this.place = place;
        this.title = title;
        this.shortDescription = shortDescription;
        this.pageAmount = pageAmount;
    }

    public Long getId() { return id; }

    public Place getPlace() {
        return place;
    }

    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public int getPageAmount() { return pageAmount; }

    public boolean hasPage(int idx) {
        return (idx <= pageAmount && idx > 0);
    }
}
