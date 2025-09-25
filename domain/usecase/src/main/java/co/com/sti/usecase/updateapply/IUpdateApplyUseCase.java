package co.com.sti.usecase.updateapply;

import co.com.sti.model.apply.Apply;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public interface IUpdateApplyUseCase {
    Mono<Apply> update(Long idApply, Integer idState, Boolean notify);
}
