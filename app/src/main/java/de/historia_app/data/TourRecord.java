package de.historia_app.data;

/**
 * Represents a record kept by the server of an available tour.
 */
public class TourRecord {

    // id of the record (not the tour) assigned by the server
    private long id;

    // the publishing timestamp assigned by the server
    private long version;

    // name of the tour as shown to the user (should be the same as the name of the tour that will
    // be downloaded
    private String name;

    // id of the tour that this record references
    private long tourId;

    // the area that the tour is part of
    private long areaId;

    // the name of the tour's area
    private String areaName;

    // the url to download the tour content from
    private String mediaUrl;

    // the size of the tour content download in bytes
    private int downloadSize;

    public TourRecord() {
        // empty constructor for yaml parsing
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

    public long getTourId() {
        return tourId;
    }

    public void setTourId(long tourId) {
        this.tourId = tourId;
    }

    public long getAreaId() {
        return areaId;
    }

    public void setAreaId(long areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public int getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(int downloadSize) {
        this.downloadSize = downloadSize;
    }
}
