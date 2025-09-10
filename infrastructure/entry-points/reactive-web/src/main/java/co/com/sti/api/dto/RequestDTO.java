package co.com.sti.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {

    private BigDecimal amount;
    private Integer timeLimit;
    private String email;
    private String names;
    private Integer idLoanType;
    private BigDecimal rateInterest;
    private Integer idStateApply;
    private BigDecimal salary;
    private BigDecimal totalMonthlyFee;
}
