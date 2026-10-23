package ke.co.skyworld.internship.domain.beans.purchaseorder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record PurchaseOrderResponse(
        long purchaseOrderId,
        long supplierId,
        long warehouseId,
        String status,
        Date expectedDate,
        String createdBy,
        List<Line> lines,
        Date dateCreated,
        Date dateModified
) {
    public record Line(
            long purchaseOrderLineId,
            long productId,
            int quantityOrdered,
            BigDecimal unitCost,
            String status
    ) {
    }
}