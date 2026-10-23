package ke.co.skyworld.internship.domain.beans.customer;


public record CustomerRequest (String name,Long defaultWarehouseId,String contactEmail,String contactPhone){}