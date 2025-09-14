package co.com.sti.usecase.requestapplylist;

import co.com.sti.model.paginator.PagedResponse;
import co.com.sti.model.request.Request;
import co.com.sti.model.paginator.Pagination;
import reactor.core.publisher.Mono;

public interface IRequestApplyListUseCase {
    Mono<PagedResponse<Request>> applyList(Pagination pagination);
}
