package co.com.sti.r2dbc;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.request.Request;
import co.com.sti.model.request.paginator.Pagination;
import co.com.sti.model.state.State;
import co.com.sti.r2dbc.entity.ApplyEntity;
import co.com.sti.r2dbc.extras.UserExtrasImpl;
import co.com.sti.r2dbc.helper.ReactiveAdapterOperations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
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
    public Mono<List<Request>> findAllForReview(Pagination pagination) {
        List<Sort.Order> orders = pagination.getSortBy().stream()
                .map(sortBy -> sortBy.getDirection().equalsIgnoreCase("asc") ?
                        Sort.Order.asc(sortBy.getProperty()) : Sort.Order.desc(sortBy.getProperty()))
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(pagination.getPage(), pagination.getSize(), Sort.by(orders));

        return repository.findAllByIdStateIn(Arrays.asList(State.PENDING.getIdState(), State.REVIEW.getIdState(), State.REJECTED.getIdState()), pageable)
                .map(entity -> mapper.map(entity, Apply.class))
                .flatMap(apply -> {
                    return userExtras.dataUser(apply.getNumberIdentity())
                            .filter(Objects::nonNull)
                            .flatMap(user -> repositoryTypeLoan.findById(apply.getIdLoanType())
                                    .map(typeLoan -> {
                                        Request request = new Request();
                                        request.setAmount(apply.getAmount());
                                        request.setTimeLimit(apply.getTimeLimit());
                                        request.setIdLoanType(apply.getIdLoanType());
                                        request.setIdStateApply(apply.getIdState());
                                        request.setRateInterest(typeLoan.getRateInterest());
                                        request.setTotalMonthlyFee(BigDecimal.ZERO);

                                        request.setNames(user.getName() + " " + user.getLastName());
                                        request.setEmail(user.getEmail());
                                        request.setSalary(user.getSalary());
                                        return request;
                                    })
                                    .onErrorResume(err2 -> {
                                        log.error("Error al obtener datos del tipo de préstamo para el ID {}: {}", apply.getIdLoanType(), err2.getMessage());
                                        return Mono.empty();
                                    })
                            )
                            .onErrorResume(err1 ->{
                                log.error("Error al obtener datos del usuario para el número de identificación {}: {}", apply.getNumberIdentity(), err1.getMessage());
                                return Mono.empty();
                            });
                })
                .collectList();
    }


}
