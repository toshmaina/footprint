package ke.co.skyworld.internship.domain.beans.pickupwave;


import java.util.Date;

public record PickWaveRequest(Long warehouseId, Date cutoffTime) {
}