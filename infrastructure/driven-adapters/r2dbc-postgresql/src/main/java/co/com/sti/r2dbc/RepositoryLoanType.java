package co.com.sti.r2dbc;

import co.com.sti.r2dbc.entity.LoanTypeEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RepositoryLoanType extends ReactiveCrudRepository<LoanTypeEntity, Integer>, ReactiveQueryByExampleExecutor<LoanTypeEntity> {

}
