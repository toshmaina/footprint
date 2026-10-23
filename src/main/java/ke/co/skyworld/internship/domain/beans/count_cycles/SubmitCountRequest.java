package ke.co.skyworld.internship.domain.beans.count_cycles;


public class SubmitCountRequest {

    private Integer countedQuantity;

    public SubmitCountRequest() {
        // required for JSON deserialization
    }

    public SubmitCountRequest(Integer countedQuantity) {
        this.countedQuantity = countedQuantity;
    }

    public Integer getCountedQuantity() {
        return countedQuantity;
    }

    public void setCountedQuantity(Integer countedQuantity) {
        this.countedQuantity = countedQuantity;
    }
}