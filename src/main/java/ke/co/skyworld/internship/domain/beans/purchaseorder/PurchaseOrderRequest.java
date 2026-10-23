package ke.co.skyworld.internship.domain.beans.purchaseorder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record PurchaseOrderRequest(
        Long supplierId,
        Long warehouseId,
        Date expectedDate,
        List<Line> lines
) {
    public record Line(
            Long productId,
            Integer quantityOrdered,
            BigDecimal unitCost
    ) {
    }
}