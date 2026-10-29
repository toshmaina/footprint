package ke.co.skyworld.internship.domain.beans.storagelocation;


public record StorageLocationRequest(
        Long warehouseId,
        String zone,
        String aisle,
        String rack,
        String binCode,
        String locationType // 'receiving_dock','qa_hold','quarantine','sellable_storage','staging'
) {
}