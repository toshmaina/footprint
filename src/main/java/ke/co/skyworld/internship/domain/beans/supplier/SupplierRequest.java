package ke.co.skyworld.internship.domain.beans.supplier;

public record SupplierRequest(String name, String contactEmail, String contactPhone, Integer defaultLeadTimeDays) {
}