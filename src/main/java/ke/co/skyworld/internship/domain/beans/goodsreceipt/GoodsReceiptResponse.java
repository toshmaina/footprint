package ke.co.skyworld.internship.domain.beans.goodsreceipt;

import java.util.Date;
import java.util.List;

public class GoodsReceiptResponse {
    private long goodsReceiptId;
    private Long advanceShippingNoticeId;
    private long warehouseId;
    private String receivedBy;
    private Date receivedAt;
    private String status;
    private List<Line> lines;
    private Date dateCreated;
    private Date dateModified;

    public GoodsReceiptResponse() {
    }

    public GoodsReceiptResponse(long goodsReceiptId, Long advanceShippingNoticeId, long warehouseId,
                                String receivedBy, Date receivedAt, String status, List<Line> lines,
                                Date dateCreated, Date dateModified) {
        this.goodsReceiptId = goodsReceiptId;
        this.advanceShippingNoticeId = advanceShippingNoticeId;
        this.warehouseId = warehouseId;
        this.receivedBy = receivedBy;
        this.receivedAt = receivedAt;
        this.status = status;
        this.lines = lines;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public long getGoodsReceiptId() {
        return goodsReceiptId;
    }

    public void setGoodsReceiptId(long goodsReceiptId) {
        this.goodsReceiptId = goodsReceiptId;
    }

    public Long getAdvanceShippingNoticeId() {
        return advanceShippingNoticeId;
    }

    public void setAdvanceShippingNoticeId(Long advanceShippingNoticeId) {
        this.advanceShippingNoticeId = advanceShippingNoticeId;
    }

    public long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        private long goodsReceiptLineId;
        private long productId;
        private int quantityCounted;
        private Integer quantityExpected;
        private String lotNumber;
        private String conditionFlag;
        private boolean hasDiscrepancy;

        public Line() {
        }

        public Line(long goodsReceiptLineId, long productId, int quantityCounted, Integer quantityExpected,
                    String lotNumber, String conditionFlag, boolean hasDiscrepancy) {
            this.goodsReceiptLineId = goodsReceiptLineId;
            this.productId = productId;
            this.quantityCounted = quantityCounted;
            this.quantityExpected = quantityExpected;
            this.lotNumber = lotNumber;
            this.conditionFlag = conditionFlag;
            this.hasDiscrepancy = hasDiscrepancy;
        }

        public long getGoodsReceiptLineId() {
            return goodsReceiptLineId;
        }

        public void setGoodsReceiptLineId(long goodsReceiptLineId) {
            this.goodsReceiptLineId = goodsReceiptLineId;
        }

        public long getProductId() {
            return productId;
        }

        public void setProductId(long productId) {
            this.productId = productId;
        }

        public int getQuantityCounted() {
            return quantityCounted;
        }

        public void setQuantityCounted(int quantityCounted) {
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

        public boolean isHasDiscrepancy() {
            return hasDiscrepancy;
        }

        public void setHasDiscrepancy(boolean hasDiscrepancy) {
            this.hasDiscrepancy = hasDiscrepancy;
        }
    }
}
