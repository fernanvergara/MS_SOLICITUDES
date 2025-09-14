package co.com.sti.model.apply.gateways;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.paginator.PagedResponse;
import co.com.sti.model.request.Request;
import co.com.sti.model.paginator.Pagination;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplyRepository {
    Mono<Apply> saveApply(Apply apply);

    Mono<PagedResponse<Request>> findAllForReview(Pagination pagination);
}
