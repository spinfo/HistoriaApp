package de.historia_app.mappables;

import de.historia_app.data.Mapstop;

/**
 * A wrapper for a mapstop's state one the map
 */
public class MapstopOnMap {

    // the mapstop of which this is the representation on the map
    private Mapstop mapstop;

    // whether the mapstop is the first in a tour
    private boolean isFirstInTour;

    public MapstopOnMap(Mapstop mapstop) {
        this.mapstop = mapstop;
        this.isFirstInTour = false;
    }

    public boolean isFirstInTour() {
        return isFirstInTour;
    }

    public void setFirstInTour(boolean firstInTour) {
        isFirstInTour = firstInTour;
    }

    public Mapstop getMapstop() {
        return mapstop;
    }
}
