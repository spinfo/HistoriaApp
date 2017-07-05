package de.historia_app.mappables;


import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.historia_app.data.Mapstop;
import de.historia_app.data.Place;
import de.historia_app.data.Tour;

public class TourCollectionOnMap {

    private static final String TAG = TourCollectionOnMap.class.getSimpleName();

    // the tours that are shown on the map
    private Collection<TourOnMap> toursOnMap;

    // the places that are shown on the map
    private Collection<PlaceOnMap> placesOnMap;

    public TourCollectionOnMap(List<Tour> tours) {
        long start = System.currentTimeMillis();
        this.initializeFromTours(tours);
        // TODO: Remove
        Log.i("--->", "Used: " + (System.currentTimeMillis() - start) + " ms");
    }

    public Collection<TourOnMap> getToursOnMap() {
        return toursOnMap;
    }

    public Collection<PlaceOnMap> getPlacesOnMap() {
        return placesOnMap;
    }

    private void initializeFromTours(List<Tour> tours) {

        // collect the TourOnMap objects created
        final Collection<TourOnMap> toursOnMap = new ArrayList<>(tours.size());

        // keep a map of PlacesOnMap created for each place id
        final Map<Long, PlaceOnMap> placeIdsToPlacesOnMap = new HashMap<>();

        // iterate over the tours provided
        for(Tour tour : tours) {

            final List<Mapstop> stops = tour.getMapstops();
            boolean firstStop = true;
            for(final Mapstop stop : stops) {
                // each mapstop in the tour is made a MapstopOnMap
                final MapstopOnMap stopOnMap = new MapstopOnMap(stop);

                // the place of ech mapstop is made a PlaceOnMap if one was not previously created
                final Place place = stop.getPlace();
                PlaceOnMap placeOnMap = placeIdsToPlacesOnMap.get(place.getId());
                if(placeOnMap == null) {
                    placeOnMap = new PlaceOnMap(place);
                    placeIdsToPlacesOnMap.put(place.getId(), placeOnMap);
                }

                // set all necessary attributes on the MapstopOnMap
                if(firstStop) {
                    stopOnMap.setFirstInTour(true);
                    firstStop = false;
                }

                // link stop and place together
                placeOnMap.addMapstopOnMap(stopOnMap);
            }

            // save a TourOnMap
            toursOnMap.add(new TourOnMap(tour));
        }

        this.toursOnMap = toursOnMap;
        this.placesOnMap = placeIdsToPlacesOnMap.values();
    }

}