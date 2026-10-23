package ke.co.skyworld.internship.domain.beans.customer;


import java.util.Date;

public record CustomerResponse(long customerId, String name, long defaultWarehouseId, String contactEmail,
                               String contactPhone, Date dateCreated, Date dateModified) {
}