package co.com.sti.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table("solicitud")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApplyEntity {

    @Id
    @Column("id_solicitud")
    private Long id;

    @Column("documento_identidad")
    private String numberIdentity;

    @Column("monto")
    private BigDecimal amount;

    @Column("plazo")
    private Integer timeLimit;

    @Column("fecha_solicitud")
    private LocalDate dateApply;

    @Column("id_estado")
    private Integer idState;

    @Column("id_tipo_prestamo")
    private Integer idLoanType;

    @Column("intereses")
    private BigDecimal rateInterest;

}
