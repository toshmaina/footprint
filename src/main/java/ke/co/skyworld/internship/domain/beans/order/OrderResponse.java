package ke.co.skyworld.internship.domain.beans.order;


import java.util.Date;
import java.util.List;

public record OrderResponse(long orderId, long customerId, String status, Date placedAt, List<Line> lines) {
    public record Line(
            long orderLineId,
            long productId,
            int quantityOrdered,
            int quantityAllocated,
            String status,
            Long reservationId,  // set if reserved
            Long backorderId    // set if backordered
    ) {
    }
}