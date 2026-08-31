package ke.co.skyworld.internship.domain.beans.storagelocation;


import java.util.Date;

public class StorageLocationResponse {
    private long storageLocationId;
    private long warehouseId;
    private String zone;
    private String aisle;
    private String rack;
    private String binCode;
    private String locationType;
    private boolean active;
    private Date dateCreated;
    private Date dateModified;

    public StorageLocationResponse() {
    }

    public StorageLocationResponse(long storageLocationId, long warehouseId, String zone, String aisle, String rack,
                                   String binCode, String locationType, boolean active, Date dateCreated, Date dateModified) {
        this.storageLocationId = storageLocationId;
        this.warehouseId = warehouseId;
        this.zone = zone;
        this.aisle = aisle;
        this.rack = rack;
        this.binCode = binCode;
        this.locationType = locationType;
        this.active = active;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public long getStorageLocationId() {
        return storageLocationId;
    }

    public void setStorageLocationId(long storageLocationId) {
        this.storageLocationId = storageLocationId;
    }

    public long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getAisle() {
        return aisle;
    }

    public void setAisle(String aisle) {
        this.aisle = aisle;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public String getBinCode() {
        return binCode;
    }

    public void setBinCode(String binCode) {
        this.binCode = binCode;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }
}