package ke.co.skyworld.internship.domain.beans.packages;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

    public record PackageResponse(long packageId, long orderId, BigDecimal weight, String status, List<Line> lines, Date dateCreated){
    public record Line(long packageLineId,long orderLineId,int quantity) { }
}
