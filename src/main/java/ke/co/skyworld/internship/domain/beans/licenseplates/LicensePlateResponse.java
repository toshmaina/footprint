package ke.co.skyworld.internship.domain.beans.licenseplates;

import java.util.Date;

    public record LicensePlateResponse(long licensePlateId, String code, Long goodsReceiptLineId, long productId,
                                int quantity, String lotNumber, long warehouseId, Long currentStorageLocationId,
                                String status, Long parentLicensePlateId, Date dateCreated, Date dateModified) { }

