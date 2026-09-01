package ke.co.skyworld.internship.domain.beans.picktask;



public class PickTaskRequest {
    private Long orderLineId;
    private Long licensePlateId;
    private String assignedTo;

    public PickTaskRequest() {
    }

    public Long getOrderLineId() { return orderLineId; }
    public void setOrderLineId(Long orderLineId) { this.orderLineId = orderLineId; }
    public Long getLicensePlateId() { return licensePlateId; }
    public void setLicensePlateId(Long licensePlateId) { this.licensePlateId = licensePlateId; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}