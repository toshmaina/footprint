package ke.co.skyworld.internship.domain.beans.count_cycles;


public record CycleCountScheduleRequest(Long warehouseId, String productClassification, Integer countFrequencyDays) {}