package de.historia_app.mappables;


import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
        this.initializeFromTours(tours);
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

            if (tour.isIndoor()) {
                Collections.sort(stops, new Comparator<Mapstop>() {
                    @Override
                    public int compare(Mapstop o1, Mapstop o2) {
                    if (o1.getScene().getPos() == o2.getScene().getPos()) {
                        return o1.getPos() - o2.getPos();
                    }

                    return o1.getScene().getPos() - o2.getScene().getPos();
                    }
                });
            }

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

                if(tour.isIndoor()) {
                    stopOnMap.setIsPartOfIndoorTour(true);
                }

                // link stop and place together
                placeOnMap.addMapstopOnMap(stopOnMap);

                // on map only show first stop of indoor tours
                if(tour.isIndoor()) {
                    break;
                }
            }

            // save a TourOnMap
            toursOnMap.add(new TourOnMap(tour));
        }

        this.toursOnMap = toursOnMap;
        this.placesOnMap = placeIdsToPlacesOnMap.values();
    }

}
