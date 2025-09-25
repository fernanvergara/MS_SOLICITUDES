package co.com.sti.usecase.updateapply;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.drivenports.IUserExtras;
import co.com.sti.model.drivenports.dto.UserDTO;
import co.com.sti.model.sqsservices.gateways.SQSGateway;
import co.com.sti.model.state.State;
import co.com.sti.usecase.exception.ApplyNotExistsException;
import co.com.sti.usecase.exception.InvalidStatusUpdateException;
import co.com.sti.usecase.transaction.TransactionExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateApplyUseCaseTest {

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private SQSGateway sqsGateway;

    @Mock
    private IUserExtras userExtras;

    @Mock
    private TransactionExecutor transactionExecutor;

    @InjectMocks
    private UpdateApplyUseCase updateApplyUseCase;

    private Apply testApply;
    private UserDTO testUserDTO;
    private static final Long TEST_APPLY_ID = 1L;
    private static final String TEST_IDENTITY = "123456789";

    @BeforeEach
    void setUp() {
        testApply = Apply.builder()
                .numberIdentity(TEST_IDENTITY)
                .amount(new BigDecimal(5000000))
                .dateApply(LocalDate.now())
                .idState(State.REVIEW.getIdState())
                .build();

        testUserDTO = new UserDTO(
                "Pedro",
                "Perez",
                "pedro.perez@example.com",
                "password",
                TEST_IDENTITY,
                LocalDate.of(1990, 1, 1),
                "3101234567",
                "Calle 100 # 20-30",
                1,
                new BigDecimal(2000000)
        );
    }

    @Test
    void shouldUpdateApplyToApprovedSuccessfully() {
        // Mover la configuracion de TransactionExecutor al test que lo usa
        when(transactionExecutor.executeInTransaction(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Mono<Apply> mono = (Mono<Apply>) invocation.getArgument(0, java.util.function.Supplier.class).get();
            return mono;
        });

        // Given
        int approvedStateId = State.APPROVED.getIdState();
        when(applyRepository.updateStateOfApply(anyLong(), anyInt())).thenReturn(Mono.just(testApply.toBuilder().idState(approvedStateId).build()));
        when(userExtras.dataUser(anyString())).thenReturn(Mono.just(testUserDTO));
        when(sqsGateway.sendNotification(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(updateApplyUseCase.update(TEST_APPLY_ID, approvedStateId))
                .expectNextMatches(updatedApply -> updatedApply.getIdState().equals(approvedStateId))
                .verifyComplete();
    }

    @Test
    void shouldUpdateApplyToRejectedSuccessfully() {
        // Mover la configuracion de TransactionExecutor al test que lo usa
        when(transactionExecutor.executeInTransaction(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Mono<Apply> mono = (Mono<Apply>) invocation.getArgument(0, java.util.function.Supplier.class).get();
            return mono;
        });

        // Given
        int rejectedStateId = State.REJECTED.getIdState();
        when(applyRepository.updateStateOfApply(anyLong(), anyInt())).thenReturn(Mono.just(testApply.toBuilder().idState(rejectedStateId).build()));
        when(userExtras.dataUser(anyString())).thenReturn(Mono.just(testUserDTO));
        when(sqsGateway.sendNotification(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(updateApplyUseCase.update(TEST_APPLY_ID, rejectedStateId))
                .expectNextMatches(updatedApply -> updatedApply.getIdState().equals(rejectedStateId))
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenStatusIsInvalid() {
        // Given
        int invalidStateId = State.REVIEW.getIdState(); // Estado de revisión no es final

        // When & Then
        StepVerifier.create(updateApplyUseCase.update(TEST_APPLY_ID, invalidStateId))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidStatusUpdateException &&
                                throwable.getMessage().equals("El estado debe ser Aprobado o Rechazado."))
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenApplyNotExists() {
        // Mover la configuracion de TransactionExecutor al test que lo usa
        when(transactionExecutor.executeInTransaction(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Mono<Apply> mono = (Mono<Apply>) invocation.getArgument(0, java.util.function.Supplier.class).get();
            return mono;
        });

        // Given
        int approvedStateId = State.APPROVED.getIdState();
        when(applyRepository.updateStateOfApply(anyLong(), anyInt())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(updateApplyUseCase.update(TEST_APPLY_ID, approvedStateId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ApplyNotExistsException &&
                                throwable.getMessage().equals("Solicitud con ID " + TEST_APPLY_ID + " no encontrada."))
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenSQSGatewayFails() {
        // Mover la configuracion de TransactionExecutor al test que lo usa
        when(transactionExecutor.executeInTransaction(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Mono<Apply> mono = (Mono<Apply>) invocation.getArgument(0, java.util.function.Supplier.class).get();
            return mono;
        });

        // Given
        int approvedStateId = State.APPROVED.getIdState();
        when(applyRepository.updateStateOfApply(anyLong(), anyInt())).thenReturn(Mono.just(testApply));
        when(userExtras.dataUser(anyString())).thenReturn(Mono.just(testUserDTO));
        when(sqsGateway.sendNotification(any())).thenReturn(Mono.error(new RuntimeException("SQS error")));

        // When & Then
        StepVerifier.create(updateApplyUseCase.update(TEST_APPLY_ID, approvedStateId))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Ocurrió un error al enviar la notificación."))
                .verify();
    }
}