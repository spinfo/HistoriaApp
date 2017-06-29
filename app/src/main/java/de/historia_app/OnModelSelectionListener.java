package de.historia_app;

import de.historia_app.data.Area;
import de.historia_app.data.Tour;

/**
 * An interface for objectes listening to changes of the current area or tour.
 */
interface OnModelSelectionListener {
    void onTourSelected(Tour tour);
    void onAreaSelected(Area area);
}
