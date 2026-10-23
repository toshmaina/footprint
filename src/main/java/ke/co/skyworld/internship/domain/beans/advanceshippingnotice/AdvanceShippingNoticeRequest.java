package ke.co.skyworld.internship.domain.beans.advanceshippingnotice;


import java.util.Date;
import java.util.List;

public record AdvanceShippingNoticeRequest(Long purchaseOrderId, Long supplierId, Long warehouseId, String carrier,
                                           Date expectedArrival, List<Line> lines) {
    public record Line(Long productId, Integer quantityExpected, String lotNumber, String packagingType) {
    }
}



