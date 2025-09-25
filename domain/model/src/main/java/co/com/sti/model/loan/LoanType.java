package co.com.sti.model.loan;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoanType {
    private Integer idType;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal rateInterest;
    private Boolean automaticValidation;
}
