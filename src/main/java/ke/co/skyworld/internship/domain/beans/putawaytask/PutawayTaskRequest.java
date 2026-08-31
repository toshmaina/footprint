package ke.co.skyworld.internship.domain.beans.putawaytask;


public class PutawayTaskRequest {
    private Long suggestedStorageLocationId;
    private String assignedTo; // nullable - task can be created unassigned

    public PutawayTaskRequest() {
    }

    public Long getSuggestedStorageLocationId() {
        return suggestedStorageLocationId;
    }

    public void setSuggestedStorageLocationId(Long suggestedStorageLocationId) {
        this.suggestedStorageLocationId = suggestedStorageLocationId;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}