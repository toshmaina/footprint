package ke.co.skyworld.internship.domain.beans;



import java.util.Date;
import java.util.List;

public class AdvanceShippingNoticeRequest {
    private Long purchaseOrderId; // nullable - unlinked ASNs happen
    private Long supplierId;
    private Long warehouseId;
    private String carrier;
    private Date expectedArrival;
    private List<Line> lines;

    public AdvanceShippingNoticeRequest() {
    }

    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public Date getExpectedArrival() { return expectedArrival; }
    public void setExpectedArrival(Date expectedArrival) { this.expectedArrival = expectedArrival; }
    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }

    public static class Line {
        private Long productId;
        private Integer quantityExpected;
        private String lotNumber;
        private String packagingType;

        public Line() {
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantityExpected() { return quantityExpected; }
        public void setQuantityExpected(Integer quantityExpected) { this.quantityExpected = quantityExpected; }
        public String getLotNumber() { return lotNumber; }
        public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
        public String getPackagingType() { return packagingType; }
        public void setPackagingType(String packagingType) { this.packagingType = packagingType; }
    }
}