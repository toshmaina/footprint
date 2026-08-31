package ke.co.skyworld.internship.domain.beans.stocksreport;


import java.util.Date;

public class StockReportResponse {
    private String sku;
    private long productId;
    private long warehouseId;
    private Date asOf;
    private int availableQuantity;

    public StockReportResponse() {
    }

    public StockReportResponse(String sku, long productId, long warehouseId, Date asOf, int availableQuantity) {
        this.sku = sku;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.asOf = asOf;
        this.availableQuantity = availableQuantity;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Date getAsOf() {
        return asOf;
    }

    public void setAsOf(Date asOf) {
        this.asOf = asOf;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
