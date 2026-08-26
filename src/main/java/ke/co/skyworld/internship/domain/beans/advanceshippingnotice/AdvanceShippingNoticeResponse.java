package ke.co.skyworld.internship.domain.beans.advanceshippingnotice;


import java.util.Date;
import java.util.List;

public class AdvanceShippingNoticeResponse {
    private long advanceShippingNoticeId;
    private Long purchaseOrderId;
    private long supplierId;
    private long warehouseId;
    private String carrier;
    private Date expectedArrival;
    private String status;
    private List<Line> lines;
    private Date dateCreated;
    private Date dateModified;

    public AdvanceShippingNoticeResponse() {
    }

    public AdvanceShippingNoticeResponse(long advanceShippingNoticeId, Long purchaseOrderId, long supplierId,
                                         long warehouseId, String carrier, Date expectedArrival, String status,
                                         List<Line> lines, Date dateCreated, Date dateModified) {
        this.advanceShippingNoticeId = advanceShippingNoticeId;
        this.purchaseOrderId = purchaseOrderId;
        this.supplierId = supplierId;
        this.warehouseId = warehouseId;
        this.carrier = carrier;
        this.expectedArrival = expectedArrival;
        this.status = status;
        this.lines = lines;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public long getAdvanceShippingNoticeId() { return advanceShippingNoticeId; }
    public void setAdvanceShippingNoticeId(long advanceShippingNoticeId) { this.advanceShippingNoticeId = advanceShippingNoticeId; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public long getSupplierId() { return supplierId; }
    public void setSupplierId(long supplierId) { this.supplierId = supplierId; }
    public long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(long warehouseId) { this.warehouseId = warehouseId; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public Date getExpectedArrival() { return expectedArrival; }
    public void setExpectedArrival(Date expectedArrival) { this.expectedArrival = expectedArrival; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }

    public static class Line {
        private long advanceShippingNoticeLineId;
        private long productId;
        private int quantityExpected;
        private String lotNumber;
        private String packagingType;

        public Line() {
        }

        public Line(long advanceShippingNoticeLineId, long productId, int quantityExpected,
                    String lotNumber, String packagingType) {
            this.advanceShippingNoticeLineId = advanceShippingNoticeLineId;
            this.productId = productId;
            this.quantityExpected = quantityExpected;
            this.lotNumber = lotNumber;
            this.packagingType = packagingType;
        }

        public long getAdvanceShippingNoticeLineId() { return advanceShippingNoticeLineId; }
        public void setAdvanceShippingNoticeLineId(long advanceShippingNoticeLineId) { this.advanceShippingNoticeLineId = advanceShippingNoticeLineId; }
        public long getProductId() { return productId; }
        public void setProductId(long productId) { this.productId = productId; }
        public int getQuantityExpected() { return quantityExpected; }
        public void setQuantityExpected(int quantityExpected) { this.quantityExpected = quantityExpected; }
        public String getLotNumber() { return lotNumber; }
        public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
        public String getPackagingType() { return packagingType; }
        public void setPackagingType(String packagingType) { this.packagingType = packagingType; }
    }
}