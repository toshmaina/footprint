package ke.co.skyworld.internship.domain.beans.stocksreport;


import java.util.Date;

public record StockReportResponse(String sku, long productId, long warehouseId, Date asOf, int availableQuantity) {
}