package co.com.sti.r2dbc;

import co.com.sti.model.apply.Apply;
import co.com.sti.r2dbc.entity.ApplyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyReactiveRepositoryAdapterTest {
    // TODO: change four you own tests

    @InjectMocks
    private MyReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    private MyReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    private Apply apply;
    private ApplyEntity applyEntity;
    private ApplyEntity savedApplyEntity;

    @BeforeEach
    public void setup() {
        apply = Apply.builder()
                .numberIdentity("123456789")
                .amount(BigDecimal.valueOf(5000))
                .timeLimit(12)
                .build();

        applyEntity = ApplyEntity.builder()
                .numberIdentity("123456789")
                .amount(BigDecimal.valueOf(5000))
                .timeLimit(12)
                .build();

        savedApplyEntity = ApplyEntity.builder()
                .id(1L)
                .numberIdentity("123456789")
                .amount(BigDecimal.valueOf(5000))
                .timeLimit(12)
                .dateApply(LocalDate.now())
                .idState(1)
                .idLoanType(1)
                .build();
    }

    @Test
    void shouldSaveApplySuccessfully() {
        when(mapper.map(apply, ApplyEntity.class)).thenReturn(applyEntity);

        when(repository.save(any(ApplyEntity.class))).thenReturn(Mono.just(savedApplyEntity));

        when(mapper.map(savedApplyEntity, Apply.class)).thenReturn(
                Apply.builder()
                        .numberIdentity("123456789")
                        .amount(BigDecimal.valueOf(5000))
                        .timeLimit(12)
                        .dateApply(LocalDate.now())
                        .build()
        );

        Mono<Apply> result = repositoryAdapter.saveApply(apply);

        StepVerifier.create(result)
                .expectNextMatches(savedApply ->
                        savedApply.getNumberIdentity().equals("123456789") &&
                        savedApply.getAmount().equals(BigDecimal.valueOf(5000))
                )
                .verifyComplete();
    }
}
