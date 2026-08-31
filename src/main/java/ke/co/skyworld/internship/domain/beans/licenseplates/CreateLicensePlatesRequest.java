package ke.co.skyworld.internship.domain.beans.licenseplates;

import java.util.List;

public class CreateLicensePlatesRequest {
    private List<Plate> plates;

    public CreateLicensePlatesRequest() {
    }

    public List<Plate> getPlates() {
        return plates;
    }

    public void setPlates(List<Plate> plates) {
        this.plates = plates;
    }

    public static class Plate {
        private String code;
        private Integer quantity;
        private String lotNumber;

        public Plate() {
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getLotNumber() {
            return lotNumber;
        }

        public void setLotNumber(String lotNumber) {
            this.lotNumber = lotNumber;
        }
    }
}
