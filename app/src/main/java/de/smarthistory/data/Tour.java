package de.smarthistory.data;

import java.util.Date;
import java.util.List;

/**
 * Data object for a Tour which is part of an area and contains Mapstops
 */
public class Tour {

    public enum Type { Rundgang, Spaziergang };

    private String name;

    private List<Mapstop> mapstops;

    private Type type;

    private int walkLength;

    private int duration;

    private Date createdAt;

    private String tagWhat, tagWhen, tagWhere;

    private String accessibility;

    private String author;

    public Tour(String name, List<Mapstop> mapstops, Type type, int walkLength, int duration, String tagWhat, String tagWhen, String tagWhere, Date createdAt, String accessibility, String author) {
        this.name = name;
        this.mapstops = mapstops;
        this.type = type;
        this.walkLength = walkLength;
        this.duration = duration;
        this.tagWhat = tagWhat;
        this.tagWhen = tagWhen;
        this.tagWhere = tagWhere;
        this.createdAt = createdAt;
        this.accessibility = accessibility;
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public List<Mapstop> getMapstops() {
        return mapstops;
    }

    public Type getType() {
        return type;
    }

    public int getWalkLength() {
        return walkLength;
    }

    public int getDuration() {
        return duration;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getTagWhat() {
        return tagWhat;
    }

    public String getTagWhen() {
        return tagWhen;
    }

    public String getTagWhere() {
        return tagWhere;
    }

    public String getAccessibility() {
        return accessibility;
    }

    public String getAuthor() {
        return author;
    }
}
