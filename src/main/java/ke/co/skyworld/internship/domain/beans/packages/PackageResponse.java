package ke.co.skyworld.internship.domain.beans.packages;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class PackageResponse {
    private long packageId;
    private long orderId;
    private BigDecimal weight;
    private String status;
    private List<Line> lines;
    private Date dateCreated;

    public PackageResponse() {
    }

    public PackageResponse(long packageId, long orderId, BigDecimal weight, String status, List<Line> lines, Date dateCreated) {
        this.packageId = packageId;
        this.orderId = orderId;
        this.weight = weight;
        this.status = status;
        this.lines = lines;
        this.dateCreated = dateCreated;
    }

    public long getPackageId() { return packageId; }
    public void setPackageId(long packageId) { this.packageId = packageId; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }

    public static class Line {
        private long packageLineId;
        private long orderLineId;
        private int quantity;

        public Line() {
        }

        public Line(long packageLineId, long orderLineId, int quantity) {
            this.packageLineId = packageLineId;
            this.orderLineId = orderLineId;
            this.quantity = quantity;
        }

        public long getPackageLineId() { return packageLineId; }
        public void setPackageLineId(long packageLineId) { this.packageLineId = packageLineId; }
        public long getOrderLineId() { return orderLineId; }
        public void setOrderLineId(long orderLineId) { this.orderLineId = orderLineId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}

