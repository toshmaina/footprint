package ke.co.skyworld.internship.domain.beans.product;

public record ProductRequest(String sku, String name, String category, Integer reorderThreshold,
                             String classification) {
}