package co.com.sti.model.apply.gateways;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.request.Request;
import co.com.sti.model.request.paginator.Pagination;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplyRepository {
    Mono<Apply> saveApply(Apply apply);

    Mono<List<Request>> findAllForReview(Pagination pagination);
}
