package co.com.sti.model.sqsservices;

import co.com.sti.model.loan.LoanDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ApplyLoanMessage {
    private String id;
    private String numberIdentity;
    private String email;
    private BigDecimal salary;
    private List<LoanDTO> activeLoans;
    private LocalDate date;
    private BigDecimal amount;
    private Integer timeLimit;
    private Integer idLoanType;
    private BigDecimal rateInterest;
    private LocalDate dateApply;
    private String callBack;
}
