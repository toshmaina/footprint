package ke.co.skyworld.internship.domain.beans.warehouse;

import java.util.Date;


public record WarehouseResponse(long warehouseId, String code, String name, String address, boolean active,
                             Date dateCreated, Date dateModified) {}