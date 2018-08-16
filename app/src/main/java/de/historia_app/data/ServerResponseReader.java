package de.historia_app.data;

import android.graphics.Bitmap;
import android.util.Log;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ServerResponseReader {

    private static final String LOG_TAG = ServerResponseReader.class.getName();

    private static final Yaml yaml = new Yaml();

    private static final Yaml TOUR_RECORD_YAML = new Yaml(new Constructor(TourRecord.class));

    private static final Yaml LEXICON_ENTRY_YAML = new Yaml(new Constructor(LexiconEntry.class));

    private static final Yaml MAPSTOP_YAML;
    static {
        Constructor constructor = new Constructor(Mapstop.class);
        TypeDescription mapstopDescription = new TypeDescription(Mapstop.class);
        mapstopDescription.putListPropertyType("pages", Page.class);
        MAPSTOP_YAML = new Yaml(constructor);
    }

    private static final Yaml SCENE_YAML;
    static {
        Constructor constructor = new Constructor(Scene.class);
        TypeDescription sceneDescription = new TypeDescription(Scene.class);
        sceneDescription.putListPropertyType("mapstops", Mapstop.class);
        sceneDescription.putListPropertyType("coordinates", Coordinate.class);
        SCENE_YAML = new Yaml(constructor);
    }

    private static final Yaml COORDINATE_YAML;
    static {
        Constructor constructor = new Constructor(Coordinate.class);
        TypeDescription coordinateDescription = new TypeDescription(Coordinate.class);
        COORDINATE_YAML = new Yaml(constructor);
    }

    public static AvailableTours parseAvailableTours(String input) {
        AvailableTours availableTours = new AvailableTours();
        for(Object object : TOUR_RECORD_YAML.loadAll(input)) {
            availableTours.addRecord((TourRecord) object);
        }
        return availableTours;
    }

    public static Tour parseTour(InputStream serializedTour) {
        // This is a bit of a mess at the moment. Parsing the tour is done using a basic map
        // each field of which is treated individually. Mapstops however are deserialised using
        // snake yamls internal object creation. The reason for this inconsistency is that
        // the tour contains some complex fields which are not easily deserialized automatically.
        // The process could probably be reworked to be more consistent, but it suffices for the
        // moment.
        Map<String, Object> map = (Map) yaml.load(serializedTour);

        String name = (String) map.get("name");
        String intro = (String) map.get("intro");
        String tagWhat = (String) map.get("tagWhat");
        String tagWhen = (String) map.get("tagWhen");
        String tagWhere = (String) map.get("tagWhere");
        String accessibility = (String) map.get("accessibility");
        String author = (String) map.get("author");

        // TODO: Snake yaml should be able to handle this correctly, without int casting
        Long id = Long.valueOf((int) map.get("id"));

        Integer walkLength = (Integer) map.get("walkLength");
        Integer duration = (Integer) map.get("duration");

        // handle the type
        String type = (String) map.get("type");
        Tour.Type tourType = null;
        if("tour".matches(type)) {
            tourType = Tour.Type.Tour;
        } else if("round-tour".matches(type)) {
            tourType = Tour.Type.RoundTour;
        } else if("public-transport-tour".matches(type)) {
            tourType = Tour.Type.PublicTransportTour;
        } else if("bike-tour".matches(type)) {
            tourType = Tour.Type.BikeTour;
        } else if("indoor-tour".matches(type)) {
            tourType = Tour.Type.IndoorTour;
        }

        // handle the tour track
        List trackPoints = (List) map.get("track");
        List<PersistentGeoPoint> track = new ArrayList<>();
        for(Object elem : trackPoints) {
            List<Double> coords = (List<Double>) elem;
            PersistentGeoPoint point = new PersistentGeoPoint(coords.get(0), coords.get(1));
            track.add(point);
        }

        // handle the mapstops
        List<Mapstop> mapstops = new ArrayList<>();
        List<Map<String, Object>> mapstopsInput = (List) map.get("mapstops");
        for(Map<String, Object> mapstopInput : mapstopsInput) {
            // TODO: There should be a better way to do this (not dumping, then loading)
            String mapstopTest = yaml.dump(mapstopInput);
            Mapstop mapstop = (Mapstop) MAPSTOP_YAML.load(mapstopTest);
            if (!mapstop.hasPages()) {
                mapstop.setPages(new ArrayList<Page>());
            }
            mapstops.add(mapstop);
        }

        // handle scenes
        List<Scene> scenes = new ArrayList<>();
        List<Map<String, Object>> scenesInput = (List) map.get("scenes");
        if (scenesInput != null) {
            int pos = 1;
            for (Map<String, Object> sceneInput : scenesInput) {
                String sceneTest = yaml.dump(sceneInput);
                Scene scene = (Scene) SCENE_YAML.load(sceneTest);
                if (!scene.hasCoordinates()) {
                    scene.setCoordinates(new ArrayList<Coordinate>());
                }
                List<Mapstop> sceneMapstops = scene.getMapstops();
                for (int i = 0; i < sceneMapstops.size(); i++) {
                    Mapstop oldMapstop = sceneMapstops.get(i);
                    for (Mapstop newMapstop : mapstops) {
                        if (newMapstop.getId() == oldMapstop.getId()) {
                            newMapstop.setScene(scene);
                            newMapstop.setPos(pos);
                            pos++;
                            sceneMapstops.set(i, newMapstop);
                            break;
                        }
                    }
                }
                scene.setMapstops(sceneMapstops);
                for (int i = 0; i < scene.getCoordinates().size(); i++) {
                    scene.getCoordinates().get(i).setScene(scene);
                    for (Mapstop newMapstop : scene.getMapstops()) {
                        if (newMapstop.getId() == scene.getCoordinates().get(i).getMapstop().getId()) {
                            newMapstop.setCoordinate(scene.getCoordinates().get(i));
                            scene.getCoordinates().get(i).setMapstop(newMapstop);
                        }
                    }
                }
                scenes.add(scene);
            }
        }

        // handle the Area
        Area area = new Area();
        Map<String, Object> areaInput = (Map) map.get("area");
        // TODO: Snake yaml should be able to handle this correctly, without int casting
        area.setId(Long.valueOf((int) areaInput.get(("id"))));
        area.setName((String) areaInput.get("name"));
        List<Double> coords = (List<Double>) areaInput.get("point1");
        area.setPoint1(new PersistentGeoPoint(coords.get(0), coords.get(1)));
        coords = (List<Double>) areaInput.get("point2");
        area.setPoint2(new PersistentGeoPoint(coords.get(0), coords.get(1)));

        // handle the creation timestamp
        Date createdAt = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            createdAt = format.parse((String) map.get("createdAt"));
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Could not parse creation date: " + e.getMessage());
        }

        Tour result = new Tour(name, mapstops, tourType, id, walkLength, duration, tagWhat, tagWhen, tagWhere, createdAt, accessibility, author, intro, track, scenes);

        // set area for the tour and the tour's places
        result.setArea(area);
        for(Mapstop mapstop : result.getMapstops()) {
            mapstop.getPlace().setArea(area);
        }

        // if there is a version timestamp, set it, else default to zero
        if(map.get("version") == null) {
            result.setVersion(0l);
        } else {
            long version = Long.valueOf((int) map.get("version"));
            result.setVersion(version);
        }

        // if there are lexicon entries, add them to the tour
        if(map.get("lexiconEntries") != null) {
            List<Map<String, Object>> entriesInput = (List) map.get("lexiconEntries");
            List<LexiconEntry> entries = new ArrayList<>(entriesInput.size());
            for(Map<String, Object> entryInput : entriesInput) {
                // TODO: There should be a better way to do this (not dumping, then loading)
                LexiconEntry entry = (LexiconEntry) LEXICON_ENTRY_YAML.load(yaml.dump(entryInput));
                entries.add(entry);
            }
            result.setLexiconEntries(entries);
        }

        return result;
    }

}
