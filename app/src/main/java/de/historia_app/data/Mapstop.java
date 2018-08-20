package de.historia_app.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Data object for a Mapstop which is part of a Tour and has a place and a few html pages.
 */
public class Mapstop {

    public enum Type {
        Info,
        Route;

        private String representation;

        static {
            Info.representation = "info";
            Route.representation = "route";
        }

        public String getRepresentation() {
            return representation;
        }
    };

    // server id of the mapstop
    @DatabaseField(columnName = "id", id = true, dataType = DataType.LONG)
    private long id;

    @DatabaseField(columnName = "pos")
    private int pos;

    // the place the mapstop is displayed on
    @DatabaseField(columnName = "place", foreign = true, foreignAutoRefresh = true)
    private Place place;

    // the tour the mapstop belongs to
    @DatabaseField(columnName = "tour", foreign = true, foreignAutoRefresh = true)
    private Tour tour;

    // the scene the mapstop belongs to
    @DatabaseField(columnName = "scene", foreign = true, foreignAutoRefresh = true)
    private Scene scene;

    // mapstop type for indoor tour scenes
    @DatabaseField(columnName = "type")
    private String type;

    // the coordinate the mapstop belongs to
    @DatabaseField(columnName = "coordinate", foreign = true, foreignAutoRefresh = true)
    private Coordinate coordinate;

    // the mapstops name as shown to the user
    @DatabaseField(columnName = "name")
    private String name;

    // a short description of the mapstop shown to the user
    @DatabaseField(columnName = "description")
    private String description;

    // the mapstop's main content: (html) pages
    @ForeignCollectionField(columnName = "pages", eager = true)
    private Collection<Page> pages;

    // empty constructor needed for YAML parsing
    protected Mapstop() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) { this.coordinate = coordinate; }

    public String getType() {
        return type;
    }

    public void setType(String type) { this.type = type; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPageAmount() {
        return this.pages.size();
    }

    public List<Page> getPages() {
        return new ArrayList<>(pages);
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public boolean hasPages() { return this.pages != null && this.getPageAmount() > 0; }

    // pages are 1-indexed in the views. Check that the page exists
    public boolean hasPage(int pageNo) {
        return (pageNo <= this.getPageAmount() && pageNo > 0);
    }
}
