package de.smarthistory.data;

/**
 * Data object for a Mapstop which is part of a Tour and has a place.
 */
public class Mapstop {

    private final Place place;

    private final String text;

    public Mapstop(Place place, String text) {
        this.place = place;
        this.text = text;
    }

    public Place getPlace() {
        return place;
    }

    public String getText() {
        return text;
    }
}
