package co.com.sti.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("tipo_prestamo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TypeLoan {

    @Id
    @Column("id_tipo")
    private Integer idType;

    @Column("nombre")
    private String name;

    @Column("monto_minimo")
    private BigDecimal minAmount;

    @Column("monto_maximo")
    private BigDecimal maxAmount;

    @Column("tasa_interes")
    private BigDecimal rateInterest;

    @Column("validacion_automatica")
    private Boolean checkAutomatic;
}
