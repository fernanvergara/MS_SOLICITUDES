package co.com.sti.model.sqsservices;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataForReports {
    private Long id;
    private String numberIdentity;
    private BigDecimal amount;
}
