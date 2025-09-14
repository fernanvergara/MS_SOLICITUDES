package co.com.sti.api;

import co.com.sti.api.dto.ApplyDTO;
import co.com.sti.api.dto.RequestDTO;
import co.com.sti.api.exceptions.GlobalExceptionHandler;
import co.com.sti.api.mapper.ApplyDTOMapper;
import co.com.sti.api.mapper.RequestDTOMapper;
import co.com.sti.api.security.JwtValidator;
import co.com.sti.model.apply.Apply;
import co.com.sti.model.paginator.PagedResponse;
import co.com.sti.model.request.Request;
import co.com.sti.usecase.applyloan.IApplyLoanUseCase;
import co.com.sti.usecase.exception.UserNotExistsException;
import co.com.sti.usecase.requestapplylist.IRequestApplyListUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest(excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
@Import({Handler.class, RouterRest.class, GlobalExceptionHandler.class, RouterRestTest.TestConfig.class})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private IApplyLoanUseCase applyLoanUseCase;

    @Autowired
    private IRequestApplyListUseCase requestApplyListUseCase;

    @Autowired
    private ApplyDTOMapper applyDTOMapper;

    @Autowired
    private RequestDTOMapper requestDTOMapper;

    @Autowired
    private Validator validator;

    @Configuration
    static class TestConfig {
        @Bean
        public IApplyLoanUseCase applyLoanUseCase() {
            return mock(IApplyLoanUseCase.class);
        }

        @Bean
        public IRequestApplyListUseCase requestApplyListUseCase() {
            return mock(IRequestApplyListUseCase.class);
        }

        @Bean
        public ApplyDTOMapper applyDTOMapper() {
            return mock(ApplyDTOMapper.class);
        }

        @Bean
        public RequestDTOMapper requestDTOMapper() {
            return mock(RequestDTOMapper.class);
        }

        @Bean
        public Validator validator() {
            Validator mockValidator = mock(Validator.class);
            when(mockValidator.validate(any(ApplyDTO.class))).thenReturn(Collections.emptySet());
            return mockValidator;
        }

        @Bean
        ReactiveAuthenticationManager authenticationManager() {
            ReactiveAuthenticationManager manager = mock(ReactiveAuthenticationManager.class);
            when(manager.authenticate(any())).thenReturn(
                    Mono.just(new UsernamePasswordAuthenticationToken(
                            "mockUser", null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    ))
            );
            return manager;
        }

        @Bean
        ServerSecurityContextRepository securityContextRepository() {
            ServerSecurityContextRepository repository = mock(ServerSecurityContextRepository.class);
            when(repository.load(any())).thenReturn(
                    Mono.just(new org.springframework.security.core.context.SecurityContextImpl(
                            new UsernamePasswordAuthenticationToken("mockUser", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    ))
            );
            return repository;
        }

        @Bean
        JwtValidator jwtValidator() {
            return mock(JwtValidator.class);
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
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
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
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
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
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(applyDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testListLoanAppliesEntryPointSuccess() {
        // Arrange
        Request mockRequest = new Request();
        RequestDTO mockRequestDTO = new RequestDTO();
        List<Request> requestList = List.of(mockRequest);
        PagedResponse<Request> pagedResponse = PagedResponse.<Request>builder()
                .content(requestList)
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(requestApplyListUseCase.applyList(any())).thenReturn(Mono.just(pagedResponse));
        when(requestDTOMapper.toDTO(any(Request.class))).thenReturn(mockRequestDTO);

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/solicitud")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .queryParam("sortBy", "dateApply")
                        .queryParam("sortOrder", "asc")
                        .build())
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(RequestDTO.class)
                .hasSize(1);
    }

    @Test
    void testListLoanAppliesEntryPointNoContent() {
        PagedResponse<Request> pagedResponse = PagedResponse.<Request>builder()
                .content(Collections.emptyList())
                .page(0)
                .size(10)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();
        when(requestApplyListUseCase.applyList(any())).thenReturn(Mono.just(pagedResponse));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/solicitud")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .queryParam("sortBy", "dateApply")
                        .queryParam("sortOrder", "asc")
                        .build())
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .exchange()
                .expectStatus().isNoContent();
    }
}
