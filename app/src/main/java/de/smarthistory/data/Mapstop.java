package de.smarthistory.data;

import java.io.Serializable;

/**
 * Data object for a Mapstop which is part of a Tour and has a place.
 */
public class Mapstop {

    private final long id;

    private final Place place;

    private final String text;

    public Mapstop(long id, Place place, String text) {
        this.id = id;
        this.place = place;
        this.text = text;
    }

    public Long getId() { return id; }

    public Place getPlace() {
        return place;
    }

    public String getText() {
        return text;
    }
}
