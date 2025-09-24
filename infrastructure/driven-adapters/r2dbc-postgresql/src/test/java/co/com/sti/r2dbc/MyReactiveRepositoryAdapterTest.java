package co.com.sti.r2dbc;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.paginator.PagedResponse;
import co.com.sti.model.request.Request;
import co.com.sti.model.paginator.Pagination;
import co.com.sti.model.paginator.SortBy;
import co.com.sti.model.state.State;
import co.com.sti.model.drivenports.dto.UserDTO;
import co.com.sti.r2dbc.entity.ApplyEntity;
import co.com.sti.r2dbc.entity.TypeLoan;
import co.com.sti.r2dbc.extras.UserExtrasImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyReactiveRepositoryAdapterTest {

    @InjectMocks
    private MyReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    private RepositoryApply repository;

    @Mock
    private RepositoryTypeLoan repositoryTypeLoan;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private UserExtrasImpl userExtras;

    private Apply apply;
    private ApplyEntity applyEntity;
    private ApplyEntity savedApplyEntity;
    private UserDTO userDTO;
    private TypeLoan typeLoan;

    @BeforeEach
    public void setup() {
        apply = Apply.builder()
                .numberIdentity("123456789")
                .amount(BigDecimal.valueOf(5000))
                .timeLimit(12)
                .idLoanType(1)
                .build();

        applyEntity = ApplyEntity.builder()
                .numberIdentity("123456789")
                .amount(BigDecimal.valueOf(5000))
                .timeLimit(12)
                .idLoanType(1)
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

        userDTO = new UserDTO("John",
                "Doe",
                "john.doe@example.com",
                "pass",
                "123456789",
                LocalDate.now(),
                "1234567890",
                "123 Main St",
                1,
                BigDecimal.valueOf(10000));

        typeLoan = new TypeLoan(
                1,
                "TestLoan",
                BigDecimal.ZERO,
                new BigDecimal(100000),
                new BigDecimal(12),
                true);
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

    @Test
    void shouldFindAllForReviewSuccessfully() {
        Pagination pagination = Pagination.builder()
                        .page(0)
                        .size(10)
                        .sortBy(Collections.singletonList(SortBy.builder()
                                .property("dateApply")
                                .direction("desc")
                                .build()))
                        .build();

        when(repository.findAllByIdStateIn(any(List.class)))
                .thenReturn(Flux.just(applyEntity));

        when(mapper.map(any(ApplyEntity.class), any())).thenReturn(apply);
        when(userExtras.dataUser(any())).thenReturn(Mono.just(userDTO));
        when(repositoryTypeLoan.findById(any(Integer.class))).thenReturn(Mono.just(typeLoan));

        Mono<PagedResponse<Request>> result = repositoryAdapter.findAllForReview(pagination);

        StepVerifier.create(result)
                .expectNextMatches(pagedResponse ->
                        !pagedResponse.getContent().isEmpty() &&
                                pagedResponse.getContent().getFirst().getNames().equals("John Doe") &&
                                pagedResponse.getContent().getFirst().getEmail().equals("john.doe@example.com") &&
                                pagedResponse.getTotalElements() == 1 &&
                                pagedResponse.getTotalPages() == 1
                )
                .verifyComplete();
    }

    @Test
    void shouldFindAllForReviewWhenUserExtrasReturnsEmpty() {
        Pagination pagination = Pagination.builder()
                .page(0)
                .size(10)
                .sortBy(Collections.singletonList(SortBy.builder()
                        .property("dateApply")
                        .direction("desc")
                        .build()))
                .build();

        when(repository.findAllByIdStateIn(any(List.class)))
                .thenReturn(Flux.just(applyEntity));

        when(mapper.map(any(ApplyEntity.class), any())).thenReturn(apply);
        when(userExtras.dataUser(any())).thenReturn(Mono.empty());
        when(repositoryTypeLoan.findById(any(Integer.class))).thenReturn(Mono.just(typeLoan));

        Mono<PagedResponse<Request>> result = repositoryAdapter.findAllForReview(pagination);

        StepVerifier.create(result)
                .expectNextMatches(pagedResponse -> pagedResponse.getContent().isEmpty() && pagedResponse.getTotalElements() == 1)
                .verifyComplete();
    }

    @Test
    void shouldFindAllForReviewWhenUserExtrasReturnsError() {
        Pagination pagination = Pagination.builder()
                .page(0)
                .size(10)
                .sortBy(Collections.singletonList(SortBy.builder()
                        .property("dateApply")
                        .direction("desc")
                        .build()))
                .build();

        when(repository.findAllByIdStateIn(any(List.class)))
                .thenReturn(Flux.just(applyEntity));

        when(mapper.map(any(ApplyEntity.class), any())).thenReturn(apply);
        when(userExtras.dataUser(any())).thenReturn(Mono.error(new RuntimeException("Simulated error")));
        when(repositoryTypeLoan.findById(any(Integer.class))).thenReturn(Mono.just(typeLoan));

        Mono<PagedResponse<Request>> result = repositoryAdapter.findAllForReview(pagination);

        StepVerifier.create(result)
                .expectNextMatches(pagedResponse ->
                        pagedResponse.getContent().isEmpty() &&
                                pagedResponse.getTotalElements() == 1
                )
                .verifyComplete();
    }

    @Test
    void shouldFindAllForReviewWithMultipleApplies() {
        ApplyEntity anotherApplyEntity = ApplyEntity.builder()
                .id(2L)
                .numberIdentity("987654321")
                .amount(BigDecimal.valueOf(10000))
                .timeLimit(24)
                .dateApply(LocalDate.now())
                .idState(State.PENDING.getIdState())
                .idLoanType(2)
                .build();

        Apply anotherApply = Apply.builder()
                .numberIdentity("987654321")
                .amount(BigDecimal.valueOf(10000))
                .timeLimit(24)
                .idState(State.PENDING.getIdState())
                .idLoanType(2)
                .build();

        UserDTO anotherUserDTO = new UserDTO(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "pass",
                "987654321",
                LocalDate.now(),
                "0987654321",
                "456 Oak St",
                1,
                BigDecimal.valueOf(20000));

        TypeLoan typeLoan2 = new TypeLoan(
                2,
                "TestLoan",
                BigDecimal.ZERO,
                new BigDecimal(100000),
                new BigDecimal(15),
                true);

        Pagination pagination = Pagination.builder()
                .page(0)
                .size(10)
                .sortBy(Collections.singletonList(SortBy.builder()
                        .property("dateApply")
                        .direction("desc")
                        .build()))
                .build();

        List<ApplyEntity> entities = List.of(applyEntity, anotherApplyEntity);
        when(repository.findAllByIdStateIn(any(List.class)))
                .thenReturn(Flux.fromIterable(entities));

        when(mapper.map(applyEntity, Apply.class)).thenReturn(apply);
        when(mapper.map(anotherApplyEntity, Apply.class)).thenReturn(anotherApply);
        when(userExtras.dataUser("123456789")).thenReturn(Mono.just(userDTO));
        when(userExtras.dataUser("987654321")).thenReturn(Mono.just(anotherUserDTO));
        when(repositoryTypeLoan.findById(1)).thenReturn(Mono.just(typeLoan));
        when(repositoryTypeLoan.findById(2)).thenReturn(Mono.just(typeLoan2));

        Mono<PagedResponse<Request>> result = repositoryAdapter.findAllForReview(pagination);

        StepVerifier.create(result)
                .expectNextMatches(pagedResponse ->
                        pagedResponse.getContent().size() == 2 &&
                                pagedResponse.getContent().getFirst().getNames().equals("John Doe") &&
                                pagedResponse.getContent().get(1).getNames().equals("Jane Smith") &&
                                pagedResponse.getTotalElements() == 2 &&
                                pagedResponse.getTotalPages() == 1
                )
                .verifyComplete();
    }

    @Test
    void shouldFindAllForReviewWhenTypeLoanReturnsEmpty() {
        Pagination pagination = Pagination.builder()
                .page(0)
                .size(10)
                .sortBy(Collections.singletonList(SortBy.builder()
                        .property("dateApply")
                        .direction("desc")
                        .build()))
                .build();

        when(repository.findAllByIdStateIn(any(List.class)))
                .thenReturn(Flux.just(applyEntity));

        when(mapper.map(any(ApplyEntity.class), any())).thenReturn(apply);
        when(userExtras.dataUser(any())).thenReturn(Mono.just(userDTO));
        when(repositoryTypeLoan.findById(any(Integer.class))).thenReturn(Mono.empty());

        Mono<PagedResponse<Request>> result = repositoryAdapter.findAllForReview(pagination);

        StepVerifier.create(result)
                .expectNextMatches(pagedResponse ->
                        pagedResponse.getContent().isEmpty() &&
                                pagedResponse.getTotalElements() == 1
                )
                .verifyComplete();
    }

    @Test
    void shouldFindAllForReviewWhenTypeLoanReturnsError() {
        Pagination pagination = Pagination.builder()
                .page(0)
                .size(10)
                .sortBy(Collections.singletonList(SortBy.builder()
                        .property("dateApply")
                        .direction("desc")
                        .build()))
                .build();

        when(repository.findAllByIdStateIn(any(List.class)))
                .thenReturn(Flux.just(applyEntity));

        when(mapper.map(any(ApplyEntity.class), any())).thenReturn(apply);
        when(userExtras.dataUser(any())).thenReturn(Mono.just(userDTO));
        when(repositoryTypeLoan.findById(any(Integer.class))).thenReturn(Mono.error(new RuntimeException("Simulated TypeLoan error")));

        Mono<PagedResponse<Request>> result = repositoryAdapter.findAllForReview(pagination);

        StepVerifier.create(result)
                .expectNextMatches(pagedResponse ->
                        pagedResponse.getContent().isEmpty() &&
                                pagedResponse.getTotalElements() == 1
                )
                .verifyComplete();
    }

}