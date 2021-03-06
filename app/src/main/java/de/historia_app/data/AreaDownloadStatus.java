package de.historia_app.data;

public class AreaDownloadStatus implements AreaSortUtil.ObjectWithName {

    private long areaId;

    private String name;

    private int availableToursAmount;

    private int downloadedToursAmount;

    private long downloadedToursSize;

    private long lastVersion;

    AreaDownloadStatus(long areaId) {
        this.areaId = areaId;
        this.name = "";
        this.downloadedToursAmount = 0;
        this.downloadedToursSize = 0;
    }

    public long getAreaId() {
        return areaId;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public int getAvailableToursAmount() {
        return availableToursAmount;
    }

    void setAvailableToursAmount(int amount) {
        this.availableToursAmount = amount;
    }

    public int getDownloadedToursAmount() {
        return downloadedToursAmount;
    }

    void setDownloadedToursAmount(int downloadedToursAmount) {
        this.downloadedToursAmount = downloadedToursAmount;
    }

    public long getDownloadedToursSize() {
        return downloadedToursSize;
    }

    void setDownloadedToursSize(long downloadedToursSize) {
        this.downloadedToursSize = downloadedToursSize;
    }

    public long getLastVersion() {
        return lastVersion;
    }

    public long getLastVersionSeconds() {
        return lastVersion * 1000;
    }

    void setLastVersion(long lastVersion) {
        this.lastVersion = lastVersion;
    }
}
