package ke.co.skyworld.internship.domain.beans.order;


import java.util.Date;
import java.util.List;

public class OrderResponse {
    private long orderId;
    private long customerId;
    private String status;
    private Date placedAt;
    private List<Line> lines;

    public OrderResponse() {
    }

    public OrderResponse(long orderId, long customerId, String status, Date placedAt, List<Line> lines) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
        this.placedAt = placedAt;
        this.lines = lines;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(Date placedAt) {
        this.placedAt = placedAt;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public static class Line {
        private long orderLineId;
        private long productId;
        private int quantityOrdered;
        private int quantityAllocated;
        private String status;
        private Long reservationId;   // set if reserved
        private Long backorderId;     // set if backordered

        public Line() {
        }

        public Line(long orderLineId, long productId, int quantityOrdered, int quantityAllocated, String status,
                    Long reservationId, Long backorderId) {
            this.orderLineId = orderLineId;
            this.productId = productId;
            this.quantityOrdered = quantityOrdered;
            this.quantityAllocated = quantityAllocated;
            this.status = status;
            this.reservationId = reservationId;
            this.backorderId = backorderId;
        }

        public long getOrderLineId() {
            return orderLineId;
        }

        public void setOrderLineId(long orderLineId) {
            this.orderLineId = orderLineId;
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

        public int getQuantityAllocated() {
            return quantityAllocated;
        }

        public void setQuantityAllocated(int quantityAllocated) {
            this.quantityAllocated = quantityAllocated;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getReservationId() {
            return reservationId;
        }

        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }

        public Long getBackorderId() {
            return backorderId;
        }

        public void setBackorderId(Long backorderId) {
            this.backorderId = backorderId;
        }
    }
}