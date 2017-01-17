package de.smarthistory.data;

import java.io.Serializable;

/**
 * Data object for a Mapstop which is part of a Tour and has a place.
 */
public class Mapstop {

    private final long id;

    private final Place place;

    private final String text;

    private int pageAmount;

    public Mapstop(long id, Place place, String text, int pageAmount) {
        this.id = id;
        this.place = place;
        this.text = text;
        this.pageAmount = pageAmount;
    }

    public Long getId() { return id; }

    public Place getPlace() {
        return place;
    }

    public String getText() {
        return text;
    }

    public int getPageAmount() { return pageAmount; }

    public boolean hasPage(int idx) {
        return (idx <= pageAmount && idx > 0);
    }
}
