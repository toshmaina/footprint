package ke.co.skyworld.internship.domain.beans.discrepancyresolution;


public record DiscrepancyResolutionRequest (
    String status, // 'resolved_accepted' or 'resolved_rejected'
    String notes){}
