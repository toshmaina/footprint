package ke.co.skyworld.internship.domain.beans.warehouse;

import java.util.Date;

public class WarehouseResponse {
    private long warehouseId;
    private String code;
    private String name;
    private String address;
    private boolean active;
    private Date dateCreated;
    private Date dateModified;

    public WarehouseResponse() {
    }

    public WarehouseResponse(long warehouseId, String code, String name, String address, boolean active,
                             Date dateCreated, Date dateModified) {
        this.warehouseId = warehouseId;
        this.code = code;
        this.name = name;
        this.address = address;
        this.active = active;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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