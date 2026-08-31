package ke.co.skyworld.internship.domain.beans.supplier;

import java.util.Date;

public class SupplierResponse {
    private long supplierId;
    private String name;
    private String contactEmail;
    private String contactPhone;
    private Integer defaultLeadTimeDays;
    private boolean active;
    private Date dateCreated;
    private Date dateModified;

    public SupplierResponse() {
    }

    public SupplierResponse(long supplierId, String name, String contactEmail, String contactPhone,
                            Integer defaultLeadTimeDays, boolean active, Date dateCreated, Date dateModified) {
        this.supplierId = supplierId;
        this.name = name;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.defaultLeadTimeDays = defaultLeadTimeDays;
        this.active = active;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getDefaultLeadTimeDays() {
        return defaultLeadTimeDays;
    }

    public void setDefaultLeadTimeDays(Integer defaultLeadTimeDays) {
        this.defaultLeadTimeDays = defaultLeadTimeDays;
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