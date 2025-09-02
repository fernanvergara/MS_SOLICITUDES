package co.com.sti.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApplyDTO(
        @NotBlank(message = "El número de identidad no puede estar vacío")
        @Pattern(regexp = "\\d+")
        String numberIdentity,

        @NotNull(message = "El salario no puede ser nulo")
        @DecimalMin(value = "0.0", message = "El monto debe ser mayor a cero")
        BigDecimal amount,

        @NotNull(message = "El plazo de pago no puede ser nulo")
        Integer timeLimit,

        @NotNull(message = "La fecha de solicitud no puede estar vacía")
//        @Past(message = "La fecha de solicitud debe ser la actual o una fecha pasada")
        LocalDate dateApply,

        @NotNull(message = "El ID de estado no puede ser nulo")
        Integer idState,

        @NotNull(message = "El ID de tipo de prestamo no puede ser nulo")
        Integer idLoanType
        ) {

}
