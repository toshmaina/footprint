package ke.co.skyworld.internship.domain.beans.qainspection;

public record QaInspectionRequest (
     Integer sampleSize,
     String method, // 'visual' (default), 'measured', 'tested'
     String disposition, // 'pass', 'fail', 'partial' - required
     Integer failedQuantity, // required if disposition = 'fail' or 'partial'
     String notes){}

