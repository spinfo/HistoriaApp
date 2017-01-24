package de.smarthistory.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class to provide example data
 */
class ExampleDataProvider {

    private static final Logger LOGGER = Logger.getLogger(ExampleDataProvider.class.getName());

    private static final String EXAMPLE_DATA_FILE = "res/raw/example_data.json";

    private static final String EXAMPLE_PAGE_FILE_TEMPLATE = "file:///android_asset/mmapstopno_ppageno.html";

    private static final String EXAMPLE_PAGES_ERROR_PAGE = "file:///android_asset/error.html";

    private List<Area> areas = new ArrayList<>();

    private List<Mapstop> mapstops = new ArrayList<>();

    private Map<Long, Mapstop> mapstopsById = new HashMap<>();

    private Area currentArea;

    private Tour currentTour;

    protected ExampleDataProvider() {

        Map<Integer, Place> placeMap = new HashMap<>();

        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(EXAMPLE_DATA_FILE);
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

                    Tour.Type tourType = Tour.Type.valueOf(jTour.get("type").getAsString());
                    int walkLength = jTour.get("walk_length").getAsInt();
                    int duration = jTour.get("duration").getAsInt();
                    String tagWhat = jTour.get("tag_what").getAsString();
                    String tagWhen = jTour.get("tag_when").getAsString();
                    String tagWhere = jTour.get("tag_where").getAsString();
                    Date createdAt = new Date(jTour.get("created_at").getAsLong());
                    String accessibility = jTour.get("accessibility").getAsString();
                    String author = jTour.get("author").getAsString();

                    for (int k = 0; k < jMapstops.size(); k++) {
                        JsonObject jMapstop = jMapstops.get(k).getAsJsonObject();

                        String text = jMapstop.get("text").getAsString();
                        int pid = jMapstop.get("location_id").getAsInt();
                        long mid = jMapstop.get("id").getAsLong();
                        int pageAmount = jMapstop.get("page_amount").getAsInt();

                        Mapstop mapstop = new Mapstop(mid, placeMap.get(pid), text, pageAmount);
                        tourMapstops.add(mapstop);
                        this.mapstops.add(mapstop);
                        this.mapstopsById.put(mid, mapstop);

                        LOGGER.info("Mapstop " + pid + ": " + mapstop.getPlace().getName() + ", " + mapstop.getText());
                    }

                    Tour tour = new Tour(jTour.get("name").getAsString(), tourMapstops, tourType, walkLength, duration, tagWhat, tagWhen, tagWhere, createdAt, accessibility, author);
                    tours.add(tour);
                }

                Area area = new Area(jArea.get("name").getAsString(), tours);
                this.areas.add(area);
            }

        } catch(Exception e) {
            LOGGER.severe("Error while reading example data: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        this.currentArea = areas.get(0);

        this.currentTour = currentArea.getTours().get(0);

    }

    protected List<Area> getAreas() {
        return areas;
    }

    protected List<Mapstop> getMapstops() {
        return mapstops;
    }

    Mapstop getMapstopById(long id) { return mapstopsById.get(id); }

    protected Area getCurrentArea() { return currentArea; }

    protected Tour getCurrentTour() { return currentTour; }

    protected String getPageUriForMapstop(Mapstop mapstop, Integer pageNo) {
        String path = EXAMPLE_PAGE_FILE_TEMPLATE.replaceFirst("mapstopno", mapstop.getId().toString());
        path = path.replaceFirst("pageno", pageNo.toString());
        return path;
    }

}
