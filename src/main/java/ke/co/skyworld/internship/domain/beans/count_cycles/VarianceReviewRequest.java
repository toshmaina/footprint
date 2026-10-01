package ke.co.skyworld.internship.domain.beans.count_cycles;


public class VarianceReviewRequest {
    private String decision; // 'approved', 'rejected', 'recount_required'

    public VarianceReviewRequest() {
    }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
}