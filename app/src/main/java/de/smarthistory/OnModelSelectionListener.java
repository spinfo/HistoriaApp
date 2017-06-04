package de.smarthistory;

import de.smarthistory.data.Area;
import de.smarthistory.data.Tour;

/**
 * An interface for objectes listening to changes of the current area or tour.
 */
interface OnModelSelectionListener {
    void onTourSelected(Tour tour);
    void onAreaSelected(Area area);
}
