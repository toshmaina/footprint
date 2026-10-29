package ke.co.skyworld.internship.domain.beans.order;


import java.util.List;

public record OrderRequest(Long customerId, List<Line> lines) {
    public record Line(Long productId, Integer quantityOrdered) {
    }
}
