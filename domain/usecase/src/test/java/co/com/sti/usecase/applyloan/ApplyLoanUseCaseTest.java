package co.com.sti.usecase.applyloan;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.drivenports.IUserExtras;
import co.com.sti.usecase.exceptios.UserNotExistsException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplyLoanUseCaseTest {

    // @Mock crea un "mock" de la dependencia.
    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private IUserExtras userExistenceChecker;

    @Mock
    private TransactionExecutor transactionExecutor;

    // @InjectMocks crea una instancia real de la clase que queremos probar
    // e inyecta los mocks en ella.
    @InjectMocks
    private ApplyLoanUseCase applyLoanUseCase;

    private Apply testApply;
    private static final String TEST_IDENTITY = "123456789";

    @BeforeEach
    public void setup() {
        testApply = Apply.builder()
                .numberIdentity(TEST_IDENTITY)
                .amount(new BigDecimal(100000))
                .build();
    }

    @Test
    void shouldSaveApplyWhenUserExists() {
        when(userExistenceChecker.verifyUser(TEST_IDENTITY)).thenReturn(Mono.just(true));

        when(applyRepository.saveApply(any(Apply.class))).thenReturn(Mono.just(testApply));

        when(transactionExecutor.executeInTransaction(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Mono<Apply> mono = (Mono<Apply>) invocation.getArgument(0, java.util.function.Supplier.class).get();
            return mono;
        });

        StepVerifier.create(applyLoanUseCase.saveApply(testApply))
                .expectNextMatches(apply -> apply.getNumberIdentity().equals(TEST_IDENTITY))
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userExistenceChecker.verifyUser(TEST_IDENTITY)).thenReturn(Mono.just(false));

        when(transactionExecutor.executeInTransaction(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Mono<Apply> mono = (Mono<Apply>) invocation.getArgument(0, java.util.function.Supplier.class).get();
            return mono;
        });

        StepVerifier.create(applyLoanUseCase.saveApply(testApply))
                .expectErrorMatches(throwable -> throwable instanceof UserNotExistsException &&
                        throwable.getMessage().equals("Usuario no encontrado en base de datos"))
                .verify();
    }
}
