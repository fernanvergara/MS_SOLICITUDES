package co.com.sti.r2dbc;

import co.com.sti.r2dbc.entity.ApplyEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

// TODO: This file is just an example, you should delete or modify it
public interface MyReactiveRepository extends ReactiveCrudRepository<ApplyEntity, Long>, ReactiveQueryByExampleExecutor<ApplyEntity> {

}
