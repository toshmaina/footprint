package ke.co.skyworld.internship.domain.beans.returns;

import java.util.Date;
import java.util.List;

public class ReturnResponse {
    private long returnId;
    private long orderId;
    private long customerId;
    private long warehouseId;
    private String status;
    private List<Line> lines;
    private Date dateCreated;

    public ReturnResponse() {
    }

    public ReturnResponse(long returnId, long orderId, long customerId, long warehouseId, String status,
                          List<Line> lines, Date dateCreated) {
        this.returnId = returnId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.warehouseId = warehouseId;
        this.status = status;
        this.lines = lines;
        this.dateCreated = dateCreated;
    }

    public long getReturnId() { return returnId; }
    public void setReturnId(long returnId) { this.returnId = returnId; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }
    public long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(long warehouseId) { this.warehouseId = warehouseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }

    public static class Line {
        private long returnLineId;
        private long orderLineId;
        private long productId;
        private int quantity;
        private String condition;
        private String status;
        private Long licensePlateId;

        public Line() {
        }

        public Line(long returnLineId, long orderLineId, long productId, int quantity, String condition,
                    String status, Long licensePlateId) {
            this.returnLineId = returnLineId;
            this.orderLineId = orderLineId;
            this.productId = productId;
            this.quantity = quantity;
            this.condition = condition;
            this.status = status;
            this.licensePlateId = licensePlateId;
        }

        public long getReturnLineId() { return returnLineId; }
        public void setReturnLineId(long returnLineId) { this.returnLineId = returnLineId; }
        public long getOrderLineId() { return orderLineId; }
        public void setOrderLineId(long orderLineId) { this.orderLineId = orderLineId; }
        public long getProductId() { return productId; }
        public void setProductId(long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getLicensePlateId() { return licensePlateId; }
        public void setLicensePlateId(Long licensePlateId) { this.licensePlateId = licensePlateId; }
    }
}
