package co.com.sti.r2dbc;

import co.com.sti.model.loan.LoanType;
import co.com.sti.model.loan.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TypeLoanAdapter implements LoanTypeRepository {

    private final RepositoryLoanType repositoryLoanType;
    private final ObjectMapper mapper;

    @Override
    public Mono<LoanType> findById(Integer idLoanType) {
        return repositoryLoanType.findById(idLoanType)
                .map(entity -> mapper.map(entity, LoanType.class));
    }
}