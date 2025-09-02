package co.com.sti.usecase.applyloan;

import co.com.sti.model.apply.Apply;
import reactor.core.publisher.Mono;

public interface IApplyLoanUseCase {
    Mono<Apply> saveApply(Apply  apply);
}
