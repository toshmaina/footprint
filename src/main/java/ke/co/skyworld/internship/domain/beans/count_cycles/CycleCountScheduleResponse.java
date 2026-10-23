package ke.co.skyworld.internship.domain.beans.count_cycles;


    public record CycleCountScheduleResponse(long scheduleId, long warehouseId, String productClassification, int countFrequencyDays) {}