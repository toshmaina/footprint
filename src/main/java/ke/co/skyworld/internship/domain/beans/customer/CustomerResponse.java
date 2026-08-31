package ke.co.skyworld.internship.domain.beans.customer;


import java.util.Date;

public class CustomerResponse {
    private long customerId;
    private String name;
    private long defaultWarehouseId;
    private String contactEmail;
    private String contactPhone;
    private Date dateCreated;
    private Date dateModified;

    public CustomerResponse() {
    }

    public CustomerResponse(long customerId, String name, long defaultWarehouseId, String contactEmail,
                            String contactPhone, Date dateCreated, Date dateModified) {
        this.customerId = customerId;
        this.name = name;
        this.defaultWarehouseId = defaultWarehouseId;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDefaultWarehouseId() {
        return defaultWarehouseId;
    }

    public void setDefaultWarehouseId(long defaultWarehouseId) {
        this.defaultWarehouseId = defaultWarehouseId;
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