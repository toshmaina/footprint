package ke.co.skyworld.internship.domain.beans.discrepancysolution;


public class DiscrepancyResolutionRequest {
    private String status; // 'resolved_accepted' or 'resolved_rejected'
    private String notes;

    public DiscrepancyResolutionRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

