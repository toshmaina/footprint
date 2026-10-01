package ke.co.skyworld.internship.domain.beans.returns;


public class ProcessReturnLineRequest {
    private String condition; // 'resellable' or 'damaged'
    private String notes;
    private String licensePlateCode;

    public ProcessReturnLineRequest() {
    }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getLicensePlateCode() { return licensePlateCode; }
    public void setLicensePlateCode(String licensePlateCode) { this.licensePlateCode = licensePlateCode; }
}
