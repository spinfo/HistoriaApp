package de.smarthistory.data;

import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

class TourDeserialiser {

    private static final String LOG_TAG = TourDeserialiser.class.getName();

    private static Yaml yaml = new Yaml();

    private static final Yaml MAPSTOP_YAML;
    static {
        Constructor constructor = new Constructor(Mapstop.class);
        TypeDescription mapstopDescription = new TypeDescription(Mapstop.class);
        mapstopDescription.putListPropertyType("pages", Page.class);
        MAPSTOP_YAML = new Yaml(constructor);
    }

    private TourDeserialiser() {}

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
            tourType = Tour.Type.Spaziergang;
        } else if("round-tour".matches(type)) {
            tourType = Tour.Type.Rundgang;
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
            String mapstopTest = yaml.dump(mapstopInput);
            Mapstop mapstop = (Mapstop) MAPSTOP_YAML.load(mapstopTest);
            mapstops.add(mapstop);
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

        Tour result = new Tour(name, mapstops, tourType, id, walkLength, duration, tagWhat, tagWhen, tagWhere, createdAt, accessibility, author, intro, track);

        // set area for the tour and the tour's places
        result.setArea(area);
        for(Mapstop mapstop : result.getMapstops()) {
            mapstop.getPlace().setArea(area);
        }

        // if there is a version timestamp, set it, else default to zero
        long version = Long.valueOf((int) map.get("version"));
        if(map.get("version") == null) {
            result.setVersion(0l);
        } else {
            result.setVersion(version);
        }

        return result;
    }

}