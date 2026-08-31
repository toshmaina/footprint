package ke.co.skyworld.internship.domain.beans.purchaseorder;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class PurchaseOrderResponse {
    private long purchaseOrderId;
    private long supplierId;
    private long warehouseId;
    private String status;
    private Date expectedDate;
    private String createdBy;
    private List<Line> lines;
    private Date dateCreated;
    private Date dateModified;

    public PurchaseOrderResponse() {
    }

    public PurchaseOrderResponse(long purchaseOrderId, long supplierId, long warehouseId, String status,
                                 Date expectedDate, String createdBy, List<Line> lines,
                                 Date dateCreated, Date dateModified) {
        this.purchaseOrderId = purchaseOrderId;
        this.supplierId = supplierId;
        this.warehouseId = warehouseId;
        this.status = status;
        this.expectedDate = expectedDate;
        this.createdBy = createdBy;
        this.lines = lines;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(Date expectedDate) {
        this.expectedDate = expectedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
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

    public static class Line {
        private long purchaseOrderLineId;
        private long productId;
        private int quantityOrdered;
        private BigDecimal unitCost;
        private String status;

        public Line() {
        }

        public Line(long purchaseOrderLineId, long productId, int quantityOrdered, BigDecimal unitCost, String status) {
            this.purchaseOrderLineId = purchaseOrderLineId;
            this.productId = productId;
            this.quantityOrdered = quantityOrdered;
            this.unitCost = unitCost;
            this.status = status;
        }

        public long getPurchaseOrderLineId() {
            return purchaseOrderLineId;
        }

        public void setPurchaseOrderLineId(long purchaseOrderLineId) {
            this.purchaseOrderLineId = purchaseOrderLineId;
        }

        public long getProductId() {
            return productId;
        }

        public void setProductId(long productId) {
            this.productId = productId;
        }

        public int getQuantityOrdered() {
            return quantityOrdered;
        }

        public void setQuantityOrdered(int quantityOrdered) {
            this.quantityOrdered = quantityOrdered;
        }

        public BigDecimal getUnitCost() {
            return unitCost;
        }

        public void setUnitCost(BigDecimal unitCost) {
            this.unitCost = unitCost;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}