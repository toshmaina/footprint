package ke.co.skyworld.internship.domain.beans.goodsreceipt;


import java.util.List;

public class GoodsReceiptRequest {
    private Long advanceShippingNoticeId; // nullable - goods can arrive unlinked
    private Long dockAppointmentId;       // nullable
    private Long warehouseId;
    private List<Line> lines;

    public GoodsReceiptRequest() {
    }

    public Long getAdvanceShippingNoticeId() {
        return advanceShippingNoticeId;
    }

    public void setAdvanceShippingNoticeId(Long advanceShippingNoticeId) {
        this.advanceShippingNoticeId = advanceShippingNoticeId;
    }

    public Long getDockAppointmentId() {
        return dockAppointmentId;
    }

    public void setDockAppointmentId(Long dockAppointmentId) {
        this.dockAppointmentId = dockAppointmentId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public static class Line {
        private Long purchaseOrderLineId; // nullable - unlinked receipts happen
        private Long productId;
        private Integer quantityCounted;
        private Integer quantityExpected;   // nullable - if given, mismatch auto-creates a discrepancy
        private String lotNumber;
        private String conditionFlag; // 'unverified' (default), 'damaged_visible', 'ok_visible'

        public Line() {
        }

        public Long getPurchaseOrderLineId() {
            return purchaseOrderLineId;
        }

        public void setPurchaseOrderLineId(Long purchaseOrderLineId) {
            this.purchaseOrderLineId = purchaseOrderLineId;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantityCounted() {
            return quantityCounted;
        }

        public void setQuantityCounted(Integer quantityCounted) {
            this.quantityCounted = quantityCounted;
        }

        public Integer getQuantityExpected() {
            return quantityExpected;
        }

        public void setQuantityExpected(Integer quantityExpected) {
            this.quantityExpected = quantityExpected;
        }

        public String getLotNumber() {
            return lotNumber;
        }

        public void setLotNumber(String lotNumber) {
            this.lotNumber = lotNumber;
        }

        public String getConditionFlag() {
            return conditionFlag;
        }

        public void setConditionFlag(String conditionFlag) {
            this.conditionFlag = conditionFlag;
        }
    }
}