package co.com.sti.api;

import co.com.sti.api.dto.ApplyDTO;
import co.com.sti.api.exceptions.GlobalExceptionHandler;
import co.com.sti.api.exceptions.InvalidUserDataException;
import co.com.sti.api.mapper.ApplyDTOMapper;
import co.com.sti.model.apply.Apply;
import co.com.sti.usecase.applyloan.IApplyLoanUseCase;
import co.com.sti.usecase.exceptios.UserNotExistsException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class, RouterRestTest.TestConfig.class, GlobalExceptionHandler.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private IApplyLoanUseCase applyLoanUseCase;

    @Autowired
    private ApplyDTOMapper applyDTOMapper;

    @Autowired
    private Validator validator;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public IApplyLoanUseCase applyLoanUseCase() {
            return mock(IApplyLoanUseCase.class);
        }

        @Bean
        public ApplyDTOMapper applyDTOMapper() {
            return mock(ApplyDTOMapper.class);
        }

        @Bean
        public Validator validator() {
            Validator mockValidator = mock(Validator.class);
            when(mockValidator.validate(any(ApplyDTO.class))).thenReturn(Collections.emptySet());
            return mockValidator;
        }
    }

    @Test
    void testApplyLoanEntryPointSuccess() {
        ApplyDTO applyDTO = new ApplyDTO("123456", new BigDecimal(100000), 180, LocalDate.now(), 1, 1);
        Apply applyModel = Apply.builder()
                .numberIdentity("123456")
                .amount(new BigDecimal(100000))
                .timeLimit(180)
                .dateApply(LocalDate.now())
                .idState(1)
                .idLoanType(1)
                .build();

        when(validator.validate(any(ApplyDTO.class))).thenReturn(Collections.emptySet());
        when(applyDTOMapper.toModel(any(ApplyDTO.class))).thenReturn(applyModel);
        when(applyLoanUseCase.saveApply(any(Apply.class))).thenReturn(Mono.just(applyModel));

        webTestClient.post()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(applyDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Solicitud guardada exitosamente");
    }

    @Test
    void testApplyLoanEntryPointWithValidationErrors() {
        ConstraintViolation<ApplyDTO> mockViolation = mock(ConstraintViolation.class);
        when(mockViolation.getMessage()).thenReturn("El campo 'value' no puede ser nulo");
        when(mockViolation.getPropertyPath()).thenReturn(mock(Path.class));
        Set<ConstraintViolation<ApplyDTO>> violations = Collections.singleton(mockViolation);

        when(validator.validate(any(ApplyDTO.class))).thenReturn(violations);

        ApplyDTO applyDTO = new ApplyDTO(null, null, null, null, null, null);

        webTestClient.post()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(applyDTO)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testApplyLoanEntryPointWithBusinessRuleError() {
        ApplyDTO applyDTO = new ApplyDTO("123456", new BigDecimal(100000), 180, LocalDate.now(), 1, 1);
        Apply applyModel = Apply.builder()
                .numberIdentity("123456")
                .amount(new BigDecimal(100000))
                .timeLimit(180)
                .dateApply(LocalDate.now())
                .idState(1)
                .idLoanType(1)
                .build();

        when(validator.validate(any(ApplyDTO.class))).thenReturn(Collections.emptySet());
        when(applyDTOMapper.toModel(any(ApplyDTO.class))).thenReturn(applyModel);

        String errorMessage = "El usuario con la identificación 9876543210 no existe.";
        when(applyLoanUseCase.saveApply(any(Apply.class))).thenReturn(Mono.error(new UserNotExistsException(errorMessage)));

        webTestClient.post()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(applyDTO)
                .exchange()
                .expectStatus().isNotFound();
    }
}
