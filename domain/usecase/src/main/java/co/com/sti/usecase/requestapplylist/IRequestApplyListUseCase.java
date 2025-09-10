package co.com.sti.usecase.requestapplylist;

import co.com.sti.model.request.Request;
import co.com.sti.model.request.paginator.Pagination;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IRequestApplyListUseCase {
    Mono<List<Request>> applyList(Pagination pagination);
}
