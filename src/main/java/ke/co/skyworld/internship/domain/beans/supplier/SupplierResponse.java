package ke.co.skyworld.internship.domain.beans.supplier;

import java.util.Date;

public record SupplierResponse(long supplierId, String name, String contactEmail, String contactPhone,
                               Integer defaultLeadTimeDays, boolean active, Date dateCreated, Date dateModified) {
}