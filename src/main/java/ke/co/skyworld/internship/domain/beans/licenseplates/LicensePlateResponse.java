package ke.co.skyworld.internship.domain.beans.licenseplates;

import java.util.Date;

public class LicensePlateResponse {
    private long licensePlateId;
    private String code;
    private Long goodsReceiptLineId;
    private long productId;
    private int quantity;
    private String lotNumber;
    private long warehouseId;
    private Long currentStorageLocationId;
    private String status;
    private Long parentLicensePlateId;
    private Date dateCreated;
    private Date dateModified;

    public LicensePlateResponse() {
    }

    public LicensePlateResponse(long licensePlateId, String code, Long goodsReceiptLineId, long productId,
                                int quantity, String lotNumber, long warehouseId, Long currentStorageLocationId,
                                String status, Long parentLicensePlateId, Date dateCreated, Date dateModified) {
        this.licensePlateId = licensePlateId;
        this.code = code;
        this.goodsReceiptLineId = goodsReceiptLineId;
        this.productId = productId;
        this.quantity = quantity;
        this.lotNumber = lotNumber;
        this.warehouseId = warehouseId;
        this.currentStorageLocationId = currentStorageLocationId;
        this.status = status;
        this.parentLicensePlateId = parentLicensePlateId;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public long getLicensePlateId() {
        return licensePlateId;
    }

    public void setLicensePlateId(long licensePlateId) {
        this.licensePlateId = licensePlateId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getGoodsReceiptLineId() {
        return goodsReceiptLineId;
    }

    public void setGoodsReceiptLineId(Long goodsReceiptLineId) {
        this.goodsReceiptLineId = goodsReceiptLineId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getCurrentStorageLocationId() {
        return currentStorageLocationId;
    }

    public void setCurrentStorageLocationId(Long currentStorageLocationId) {
        this.currentStorageLocationId = currentStorageLocationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getParentLicensePlateId() {
        return parentLicensePlateId;
    }

    public void setParentLicensePlateId(Long parentLicensePlateId) {
        this.parentLicensePlateId = parentLicensePlateId;
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

