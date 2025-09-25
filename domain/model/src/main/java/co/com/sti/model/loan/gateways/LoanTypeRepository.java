package co.com.sti.model.loan.gateways;

import co.com.sti.model.loan.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
//    Mono<Boolean> isAutomaticValidationEnabled(Integer idLoanType);
    Mono<LoanType> findById(Integer idLoanType);
}
