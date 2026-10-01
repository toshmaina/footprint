package ke.co.skyworld.internship.domain.beans.returns;


import java.util.List;

public class ReturnRequest {
    private Long orderId;
    private Long warehouseId;
    private List<Line> lines;

    public ReturnRequest() {
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }

    public static class Line {
        private Long orderLineId;
        private Integer quantity;

        public Line() {
        }

        public Long getOrderLineId() { return orderLineId; }
        public void setOrderLineId(Long orderLineId) { this.orderLineId = orderLineId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
