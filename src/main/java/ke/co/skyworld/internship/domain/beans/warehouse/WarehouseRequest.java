package ke.co.skyworld.internship.domain.beans.warehouse;

public class WarehouseRequest {
    private String code;
    private String name;
    private String address;

    public WarehouseRequest() {
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}