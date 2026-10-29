package ke.co.skyworld.internship.domain.beans.product;

import java.util.Date;

public record ProductResponse(long productId, String sku, String name, String category, int reorderThreshold,
                              String classification, boolean active, Date dateCreated, Date dateModified) {
}