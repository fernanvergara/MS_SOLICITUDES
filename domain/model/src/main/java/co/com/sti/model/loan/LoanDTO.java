package co.com.sti.model.loan;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LoanDTO {
    private Long id;
    private BigDecimal amount;
    private Integer monthlyFee;
    private BigDecimal interestRate;
}
