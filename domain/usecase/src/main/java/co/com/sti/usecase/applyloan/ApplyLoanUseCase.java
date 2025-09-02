package co.com.sti.usecase.applyloan;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.drivenports.IUserExistenceChecker;
import co.com.sti.usecase.exceptios.UserNotExistsException;
import co.com.sti.usecase.transaction.TransactionExecutor;
import reactor.core.publisher.Mono;

public class ApplyLoanUseCase implements IApplyLoanUseCase {

    private final ApplyRepository applyRepository;
    private final IUserExistenceChecker userExistenceChecker;
    private final TransactionExecutor transactionExecutor;

    public ApplyLoanUseCase(ApplyRepository applyRepository, IUserExistenceChecker userExistenceChecker, TransactionExecutor transactionExecutor) {
        this.applyRepository = applyRepository;
        this.userExistenceChecker = userExistenceChecker;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public Mono<Apply> saveApply(Apply apply) {
        return transactionExecutor.executeInTransaction(() ->
                userExistenceChecker.verifyUser(apply.getNumberIdentity())
                        .flatMap(exists -> {
                            if(Boolean.FALSE.equals(exists)) {
                                return Mono.error(new UserNotExistsException("Usuario no encontrado en base de datos"));
                            }
                            System.out.println("Usuario encontrado en el sistema");
                            return applyRepository.saveApply(apply);
                        })
        );
    }
}
