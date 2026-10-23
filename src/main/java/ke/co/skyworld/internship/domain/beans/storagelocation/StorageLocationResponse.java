package ke.co.skyworld.internship.domain.beans.storagelocation;


import java.util.Date;


public record StorageLocationResponse(long storageLocationId, long warehouseId, String zone, String aisle, String rack,
                                      String binCode, String locationType, boolean active, Date dateCreated,
                                      Date dateModified) {
}