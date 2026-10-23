package ke.co.skyworld.internship.domain.beans.advanceshippingnotice;

import java.util.Date;
import java.util.List;

public record AdvanceShippingNoticeResponse(
        long advanceShippingNoticeId,
        Long purchaseOrderId,
        long supplierId,
        long warehouseId,
        String carrier,
        Date expectedArrival,
        String status,
        List<Line> lines,
        Date dateCreated,
        Date dateModified
) {
    public record Line(
            long advanceShippingNoticeLineId,
            long productId,
            int quantityExpected,
            String lotNumber,
            String packagingType
    ) {
    }
}