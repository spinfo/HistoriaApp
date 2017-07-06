package de.historia_app;

import de.historia_app.data.Area;
import de.historia_app.data.Tour;
import de.historia_app.mappables.PlaceOnMap;

/**
 * An interface for objects listening to selections of models, e.g. on the map or in a menu
 */
interface OnModelSelectionListener {
    void onTourSelected(Tour tour);
    void onAreaSelected(Area area);
    void onPlaceTapped(PlaceOnMap placeOnMap);
    void onMapstopSwitched(int position);
    void onMapstopSelected(PlaceOnMap placeOnMap, int position);

}
