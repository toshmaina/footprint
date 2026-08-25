package ke.co.skyworld.internship.domain.beans.supplier;

public class SupplierRequest {
    private String name;
    private String contactEmail;
    private String contactPhone;
    private Integer defaultLeadTimeDays;

    public SupplierRequest() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public Integer getDefaultLeadTimeDays() { return defaultLeadTimeDays; }
    public void setDefaultLeadTimeDays(Integer defaultLeadTimeDays) { this.defaultLeadTimeDays = defaultLeadTimeDays; }
}