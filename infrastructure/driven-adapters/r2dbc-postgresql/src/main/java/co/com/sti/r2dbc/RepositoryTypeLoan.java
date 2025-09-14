package co.com.sti.r2dbc;

import co.com.sti.r2dbc.entity.TypeLoan;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RepositoryTypeLoan extends ReactiveCrudRepository<TypeLoan, Integer>, ReactiveQueryByExampleExecutor<TypeLoan> {

}
