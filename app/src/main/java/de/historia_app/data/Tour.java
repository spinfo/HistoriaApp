package de.historia_app.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Data object for a Tour which is part of an area and contains Mapstops
 */
public class Tour implements Serializable {

    public enum Type {
        RoundTour,
        Tour,
        PublicTransportTour,
        BikeTour,
        IndoorTour;

        private String representation;

        static {
            RoundTour.representation = "Rundgang";
            Tour.representation = "Spaziergang";
            PublicTransportTour.representation = "ÖPNV-Tour";
            BikeTour.representation = "Fahrrad-Tour";
            IndoorTour.representation = "Indoor-Tour";
        }

        public String getRepresentation() {
            return representation;
        }
    };

    @DatabaseField(columnName = "id", id = true)
    private long id;

    // The backend's publishing timestamp
    @DatabaseField(columnName = "version")
    private long version;

    @DatabaseField(columnName = "name")
    private String name;

    @ForeignCollectionField(columnName = "mapstops")
    private Collection<Mapstop> mapstops;

    @ForeignCollectionField(columnName = "scenes")
    private Collection<Scene> scenes;

    @DatabaseField(columnName = "area", foreign = true, foreignAutoRefresh = true)
    private Area area;

    @DatabaseField(columnName = "type")
    private Type type;

    // the tour's length in meters
    @DatabaseField(columnName = "walk_length")
    private int walkLength;

    // the tour's duration in minutes
    @DatabaseField(columnName = "duration")
    private int duration;

    @DatabaseField(columnName = "tag_what")
    private String tagWhat;

    @DatabaseField(columnName = "tag_when")
    private String tagWhen;

    @DatabaseField(columnName = "tag_where")
    private String tagWhere;

    @DatabaseField(columnName = "accessibility")
    private String accessibility;

    @DatabaseField(columnName = "author")
    private String author;

    @DatabaseField(columnName = "intro")
    private String intro;

    @ForeignCollectionField(columnName = "track", eager = true)
    private Collection<PersistentGeoPoint> track;

    @DatabaseField(columnName = "created_at")
    private Date createdAt;

    // a tour may have a number of lexicon entries associated to it, especially during installation.
    // The connection to these however is not persisted to the database.
    private List<LexiconEntry> lexiconEntries;

    protected Tour() {}

    // TODO: Remove
    public Tour(String name, List<Mapstop> mapstops, Type type, long id, int walkLength, int duration, String tagWhat, String tagWhen, String tagWhere, Date createdAt, String accessibility, String author, String introduction, List<PersistentGeoPoint> track, List<Scene> scenes) {
        this.name = name;
        this.mapstops = mapstops;
        this.type = type;
        this.id = id;
        this.walkLength = walkLength;
        this.duration = duration;
        this.tagWhat = tagWhat;
        this.tagWhen = tagWhen;
        this.tagWhere = tagWhere;
        this.createdAt = createdAt;
        this.accessibility = accessibility;
        this.author = author;
        this.intro = introduction;
        this.track = track;
        this.scenes = scenes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Mapstop> getMapstops() {
        return new ArrayList<>(mapstops);
    }

    public void setMapstops(Collection<Mapstop> mapstops) {
        this.mapstops = mapstops;
    }

    public List<Scene> getScenes() {
        return new ArrayList<>(scenes);
    }

    public void setScenes(Collection<Scene> scenes) {
        this.scenes = scenes;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getWalkLength() {
        return walkLength;
    }

    public void setWalkLength(int walkLength) {
        this.walkLength = walkLength;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTagWhat() {
        return tagWhat;
    }

    public void setTagWhat(String tagWhat) {
        this.tagWhat = tagWhat;
    }

    public String getTagWhen() {
        return tagWhen;
    }

    public void setTagWhen(String tagWhen) {
        this.tagWhen = tagWhen;
    }

    public String getTagWhere() {
        return tagWhere;
    }

    public void setTagWhere(String tagWhere) {
        this.tagWhere = tagWhere;
    }

    public String getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(String accessibility) {
        this.accessibility = accessibility;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    /**
     * Returns a copy of the tour's track points
     */
    public List<? extends IGeoPoint> getTrack() {
        return new ArrayList<>(track);
    }

    /**
     * If the IGeoPoint interface does not suffice, this method converts the tour's track to
     * GeoPoints.
     * @return Never null.
     */
    public List<GeoPoint> getTrackAsGeoPoints() {
        final List<GeoPoint> result = new ArrayList<>(this.track.size());
        for(IGeoPoint point : this.track) {
            result.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
        }
        return result;
    }

    Collection<PersistentGeoPoint> getPersistableTrack() {
        return this.track;
    }

    public void setTrack(List<PersistentGeoPoint> track) {
        this.track = track;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    List<LexiconEntry> getLexiconEntries() {
        return lexiconEntries;
    }

    void setLexiconEntries(List<LexiconEntry> lexiconEntries) {
        this.lexiconEntries = lexiconEntries;
    }

    public boolean isIndoor() { return this.type == Tour.Type.IndoorTour; }
}
