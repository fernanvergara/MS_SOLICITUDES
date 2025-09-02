package co.com.sti.model.apply.gateways;

import co.com.sti.model.apply.Apply;
import reactor.core.publisher.Mono;

public interface ApplyRepository {
    Mono<Apply> saveApply(Apply apply);
}
