package ke.co.skyworld.internship.domain.beans.qainspection;

public class QaInspectionRequest {
    private Integer sampleSize;
    private String method; // 'visual' (default), 'measured', 'tested'
    private String disposition; // 'pass', 'fail', 'partial' - required
    private Integer failedQuantity; // required if disposition = 'fail' or 'partial'
    private String notes;

    public QaInspectionRequest() {
    }

    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getDisposition() { return disposition; }
    public void setDisposition(String disposition) { this.disposition = disposition; }
    public Integer getFailedQuantity() { return failedQuantity; }
    public void setFailedQuantity(Integer failedQuantity) { this.failedQuantity = failedQuantity; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

