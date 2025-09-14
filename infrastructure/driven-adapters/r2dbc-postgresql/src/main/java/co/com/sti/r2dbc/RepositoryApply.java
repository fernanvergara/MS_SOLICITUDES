package co.com.sti.r2dbc;

import co.com.sti.r2dbc.entity.ApplyEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface RepositoryApply extends ReactiveCrudRepository<ApplyEntity, Long>, ReactiveQueryByExampleExecutor<ApplyEntity> {

    Flux<ApplyEntity> findAllByIdStateIn(List<Integer> states);
}
