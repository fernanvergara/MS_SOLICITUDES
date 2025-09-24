package co.com.sti.model.drivenports.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO{
        private String name;
        private String lastName;
        private String email;
        private String password;
        private String numberIdentity;
        private LocalDate birthDate;
        private String phoneNumber;
        private String address;
        private Integer idRole;
        private BigDecimal salary;
}
