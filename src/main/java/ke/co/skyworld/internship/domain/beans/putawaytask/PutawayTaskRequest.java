package ke.co.skyworld.internship.domain.beans.putawaytask;


public record PutawayTaskRequest(Long suggestedStorageLocationId, String assignedTo) {
} // nullable - task can be created unassigned