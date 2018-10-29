package de.historia_app.data;

public class AreaDownloadStatus {

    private long areaId;

    private String name;

    private int availableToursAmount;

    private int downloadedToursAmount;

    private long downloadedToursSize;

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
}
