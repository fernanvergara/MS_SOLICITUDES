package co.com.sti.usecase.requestapplylist;

import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.paginator.PagedResponse;
import co.com.sti.model.request.Request;
import co.com.sti.model.paginator.Pagination;
import reactor.core.publisher.Mono;

public class RequestApplyListUseCase implements IRequestApplyListUseCase{

    private final ApplyRepository applyRepository;

    public RequestApplyListUseCase(ApplyRepository applyRepository) {
        this.applyRepository = applyRepository;
    }

    @Override
    public Mono<PagedResponse<Request>> applyList(Pagination pagination) {
        return applyRepository.findAllForReview(pagination);
    }


}
