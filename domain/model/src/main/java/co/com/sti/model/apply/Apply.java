package co.com.sti.model.apply;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Apply {
    private Long idApply;
    private String numberIdentity;
    private BigDecimal amount;
    private Integer timeLimit;
    private LocalDate dateApply;
    private Integer idState;
    private Integer idLoanType;

}
