package co.com.sti.r2dbc;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.paginator.PagedResponse;
import co.com.sti.model.request.Request;
import co.com.sti.model.paginator.Pagination;
import co.com.sti.model.state.State;
import co.com.sti.r2dbc.entity.ApplyEntity;
import co.com.sti.r2dbc.extras.UserExtrasImpl;
import co.com.sti.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
    Apply,
    ApplyEntity,
    Long,
    RepositoryApply
> implements ApplyRepository {

    private final UserExtrasImpl userExtras;
    private final RepositoryTypeLoan repositoryTypeLoan;

    public MyReactiveRepositoryAdapter(RepositoryApply repository, ObjectMapper mapper, UserExtrasImpl userExtras, RepositoryTypeLoan repositoryTypeLoan) {

        super(repository, mapper, d -> mapper.map(d, Apply.class));
        this.userExtras = userExtras;
        this.repositoryTypeLoan = repositoryTypeLoan;
    }

    @Override
    public Mono<Apply> saveApply(Apply apply) {
        ApplyEntity entity = mapper.map(apply, ApplyEntity.class);
        return repository.save(entity)
                .map(savedEntity -> {
                    log.info("Solicitud registrada correctamente con ID: {}", savedEntity.getId());
                    return mapper.map(savedEntity, Apply.class);
                });
    }

    @Override
    public Mono<PagedResponse<Request>> findAllForReview(Pagination pagination) {
        Flux<ApplyEntity> applyEntitiesFlux = repository.findAllByIdStateIn(
                Arrays.asList(State.PENDING.getIdState(), State.REVIEW.getIdState(), State.REJECTED.getIdState())
        );

        Comparator<ApplyEntity> comparator = ordering(pagination);

        Mono<Long> totalElementsMono = applyEntitiesFlux.count();

        return applyEntitiesFlux
                .sort(comparator)
                .skip((long) pagination.getPage() * pagination.getSize())
                .take(pagination.getSize())
                .flatMap(entity -> {
//                    Apply apply = mapper.map(entity, Apply.class);
                    return Mono.zip(
                                    userExtras.dataUser(entity.getNumberIdentity())
                                            .filter(Objects::nonNull)
                                            .onErrorResume(err1 -> {
                                                log.error("Error fetching user data for {}: {}", entity.getNumberIdentity(), err1.getMessage());
                                                return Mono.empty();
                                            }),
                                    repositoryTypeLoan.findById(entity.getIdLoanType())
                                            .onErrorResume(err2 -> {
                                                log.error("Error fetching loan type data for ID {}: {}", entity.getIdLoanType(), err2.getMessage());
                                                return Mono.empty();
                                            })
                            )
                            .map(tuple -> {
                                Request request = new Request();
                                request.setId(entity.getId());
                                request.setAmount(entity.getAmount());
                                request.setTimeLimit(entity.getTimeLimit());
                                request.setIdLoanType(entity.getIdLoanType());
                                request.setIdStateApply(entity.getIdState());
                                request.setNames(tuple.getT1().getName() + " " + tuple.getT1().getLastName());
                                request.setEmail(tuple.getT1().getEmail());
                                request.setSalary(tuple.getT1().getSalary());
                                request.setRateInterest(tuple.getT2().getRateInterest());
                                request.setTotalMonthlyFee(BigDecimal.ZERO);
                                return request;
                            })
                            .onErrorResume(e -> Mono.empty());
                })
                .collectList()
                .zipWith(totalElementsMono)
                .map(tuple -> {
                    List<Request> requests = tuple.getT1();
                    Long totalElements = tuple.getT2();
                    long totalPages = (long) Math.ceil((double) totalElements / pagination.getSize());

                    return PagedResponse.<Request>builder()
                            .content(requests)
                            .page(pagination.getPage())
                            .size(pagination.getSize())
                            .totalElements(totalElements)
                            .totalPages((int) totalPages)
                            .last(pagination.getPage() >= totalPages - 1)
                            .build();
                });
    }

    private Comparator<ApplyEntity> ordering(Pagination pagination) {
        List<Sort.Order> orders = pagination.getSortBy().stream()
                .map(sortBy -> sortBy.getDirection().equalsIgnoreCase("asc") ?
                        Sort.Order.asc(sortBy.getProperty()) : Sort.Order.desc(sortBy.getProperty()))
                .collect(Collectors.toList());

        return orders.stream()
                .map(order -> (Comparator<ApplyEntity>) (a1, a2) -> {
                    Object val1 = getPropertyValue(a1, order.getProperty());
                    Object val2 = getPropertyValue(a2, order.getProperty());
                    if (val1 instanceof Comparable && val2 instanceof Comparable) {
                        int compareResult = ((Comparable) val1).compareTo(val2);
                        return order.getDirection().isAscending() ? compareResult : -compareResult;
                    }
                    return 0;
                })
                .reduce(Comparator::thenComparing)
                .orElse((a1, a2) -> 0);
    }

    private Object getPropertyValue(ApplyEntity entity, String propertyName) {
        try {
            return ApplyEntity.class.getDeclaredField(propertyName).get(entity);
        } catch (Exception e) {
            log.warn("Property '{}' not found in ApplyEntity. Sorting will be skipped for this property.", propertyName);
            return null;
        }
    }
}
