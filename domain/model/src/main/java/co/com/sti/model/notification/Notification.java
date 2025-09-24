package co.com.sti.model.notification;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class Notification {
    private Long applyId;
    private String applicantNames;
    private String applicantEmail;
    private BigDecimal amount;
    private LocalDate dateApply;
    private String newState;
    private String message;
}
