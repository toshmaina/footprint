package ke.co.skyworld.internship.domain.beans.goodsreceipt;

import java.util.List;

public record GoodsReceiptRequest(
        Long advanceShippingNoticeId, // nullable - goods can arrive unlinked
        Long dockAppointmentId,       // nullable
        Long warehouseId,
        List<Line> lines
) {
    public record Line(
            Long purchaseOrderLineId, // nullable - unlinked receipts happen
            Long productId,
            Integer quantityCounted,
            Integer quantityExpected,   // nullable - if given, mismatch auto-creates a discrepancy
            String lotNumber,
            String conditionFlag        // 'unverified' (default), 'damaged_visible', 'ok_visible'
    ) {
    }
}