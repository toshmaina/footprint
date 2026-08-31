package ke.co.skyworld.internship.domain.beans.putawayconfirmation;


public class PutawayConfirmationRequest {
    private Long actualStorageLocationId;

    public PutawayConfirmationRequest() {
    }

    public Long getActualStorageLocationId() {
        return actualStorageLocationId;
    }

    public void setActualStorageLocationId(Long actualStorageLocationId) {
        this.actualStorageLocationId = actualStorageLocationId;
    }
}