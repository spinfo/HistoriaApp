package de.smarthistory.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class to provide example data
 */
class ExampleDataProvider {

    private static final Logger LOGGER = Logger.getLogger(ExampleDataProvider.class.getName());

    private List<Area> areas = new ArrayList<>();

    private List<Mapstop> mapstops = new ArrayList<>();

    protected ExampleDataProvider() {

        Map<Integer, Place> placeMap = new HashMap<>();

        try {
            String file = "res/raw/example_data.json";
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder json = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            Gson gson = new Gson();
            JsonObject data = gson.fromJson(json.toString(), JsonElement.class).getAsJsonObject();

            JsonArray jAreas = data.getAsJsonArray("areas");
            for (int i = 0; i < jAreas.size(); i++) {
                JsonObject jArea = jAreas.get(i).getAsJsonObject();

                JsonArray jPlaces = jArea.get("places").getAsJsonArray();

                for (int j = 0; j < jPlaces.size(); j++) {
                    JsonObject jPlace = jPlaces.get(j).getAsJsonObject();

                    String placeName = jPlace.get("name").getAsString();
                    double lat = jPlace.get("lat").getAsDouble();
                    double lon = jPlace.get("lon").getAsDouble();
                    int pid = jPlace.get("id").getAsInt();

                    Place place = new Place(placeName, lat, lon);
                    placeMap.put(pid, place);

                    LOGGER.info("Place: " + jPlace.get("name").getAsString());
                }

                JsonArray jTours = jArea.get("tours").getAsJsonArray();

                List<Tour> tours = new ArrayList<>();

                for (int j = 0; j < jTours.size(); j++) {
                    JsonObject jTour = jTours.get(j).getAsJsonObject();

                    List<Mapstop> tourMapstops = new ArrayList<>();

                    JsonArray jMapstops = jTour.getAsJsonArray("mapstops");

                    for (int k = 0; k < jMapstops.size(); k++) {
                        JsonObject jMapstop = jMapstops.get(k).getAsJsonObject();

                        String text = jMapstop.get("text").getAsString();
                        int pid = jMapstop.get("location_id").getAsInt();

                        Mapstop mapstop = new Mapstop(placeMap.get(pid), text);
                        tourMapstops.add(mapstop);
                        this.mapstops.add(mapstop);

                        LOGGER.info("Mapstop " + pid + ": " + mapstop.getPlace().getName() + ", " + mapstop.getText());
                    }

                    Tour tour = new Tour(jTour.get("name").getAsString(), tourMapstops);
                    tours.add(tour);
                }

                Area area = new Area(jArea.get("name").getAsString(), tours);
                this.areas.add(area);
            }

        } catch(Exception e) {
            LOGGER.severe("Error while reading example data: " + e.getLocalizedMessage());
            e.printStackTrace();
        }


    }

    protected List<Area> getAreas() {
        return areas;
    }

    protected List<Mapstop> getMapstops() {
        return mapstops;
    }

}
