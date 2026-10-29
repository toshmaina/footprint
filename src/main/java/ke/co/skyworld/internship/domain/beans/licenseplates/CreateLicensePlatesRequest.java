package ke.co.skyworld.internship.domain.beans.licenseplates;

import java.util.List;

public record CreateLicensePlatesRequest(List<Plate> plates) {
    public record Plate(String code, Integer quantity, String lotNumber) {
    }

}
