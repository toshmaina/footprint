package ke.co.skyworld.internship.domain.beans.packages;


public class PackageLineRequest {
    private Long orderLineId;
    private Integer quantity;

    public PackageLineRequest() {
    }

    public Long getOrderLineId() { return orderLineId; }
    public void setOrderLineId(Long orderLineId) { this.orderLineId = orderLineId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
