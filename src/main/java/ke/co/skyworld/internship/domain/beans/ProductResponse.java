package ke.co.skyworld.internship.domain.beans;

import java.util.Date;

public class ProductResponse {

    private long productId;
    private String sku;
    private String name;
    private String category;
    private int reorderThreshold;
    private String classification;
    private boolean active;
    private Date dateCreated;
    private Date dateModified;

    public ProductResponse() {
    }

    public ProductResponse(long productId, String sku, String name, String category, int reorderThreshold,
                           String classification, boolean active, Date dateCreated, Date dateModified) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.reorderThreshold = reorderThreshold;
        this.classification = classification;
        this.active = active;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }
}