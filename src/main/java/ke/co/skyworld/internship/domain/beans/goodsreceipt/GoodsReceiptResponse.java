package ke.co.skyworld.internship.domain.beans.goodsreceipt;

import java.util.Date;
import java.util.List;

public record GoodsReceiptResponse(
        long goodsReceiptId,
        Long advanceShippingNoticeId,
        long warehouseId,
        String receivedBy,
        Date receivedAt,
        String status,
        List<Line> lines,
        Date dateCreated,
        Date dateModified
) {
    public record Line(
            long goodsReceiptLineId,
            long productId,
            int quantityCounted,
            Integer quantityExpected,
            String lotNumber,
            String conditionFlag,
            boolean hasDiscrepancy
    ) {
    }
}