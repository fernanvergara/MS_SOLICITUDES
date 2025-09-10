package co.com.sti.usecase.requestapplylist;

import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.request.Request;
import co.com.sti.model.request.paginator.Pagination;
import reactor.core.publisher.Mono;

import java.util.List;

public class RequestApplyListUseCase implements IRequestApplyListUseCase{

    private final ApplyRepository applyRepository;

    public RequestApplyListUseCase(ApplyRepository applyRepository) {
        this.applyRepository = applyRepository;
    }

    @Override
    public Mono<List<Request>> applyList(Pagination pagination) {
        return applyRepository.findAllForReview(pagination);
    }


}
