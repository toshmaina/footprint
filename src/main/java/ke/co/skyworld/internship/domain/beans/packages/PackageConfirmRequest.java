package ke.co.skyworld.internship.domain.beans.packages;


import java.math.BigDecimal;

public class PackageConfirmRequest {
    private BigDecimal weight;

    public PackageConfirmRequest() {
    }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
}

