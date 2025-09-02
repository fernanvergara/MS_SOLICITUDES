package co.com.sti.r2dbc;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.r2dbc.entity.ApplyEntity;
import co.com.sti.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
    Apply,
    ApplyEntity,
    Long,
    MyReactiveRepository
> implements ApplyRepository {
    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper) {

        super(repository, mapper, d -> mapper.map(d, Apply.class));
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

}
