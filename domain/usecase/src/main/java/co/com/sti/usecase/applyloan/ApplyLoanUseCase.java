package co.com.sti.usecase.applyloan;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.drivenports.IUserExtras;
import co.com.sti.model.loan.gateways.LoanTypeRepository;
import co.com.sti.model.sqsservices.ApplyLoanMessage;
import co.com.sti.model.sqsservices.gateways.SQSGateway;
import co.com.sti.model.state.State;
import co.com.sti.usecase.exception.UserNotExistsException;
import co.com.sti.usecase.transaction.TransactionExecutor;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public class ApplyLoanUseCase implements IApplyLoanUseCase {

    private final ApplyRepository applyRepository;
    private final IUserExtras userExtras;
    private final LoanTypeRepository loanTypeRepository;
    private final TransactionExecutor transactionExecutor;
    private final SQSGateway sqsGateway;

    @Value("${aws.sqs.callBack}")
    private String urlCallBack;

    public ApplyLoanUseCase(ApplyRepository applyRepository, IUserExtras userExtras, LoanTypeRepository loanTypeRepository, TransactionExecutor transactionExecutor, SQSGateway sqsGateway) {
        this.applyRepository = applyRepository;
        this.userExtras = userExtras;
        this.loanTypeRepository = loanTypeRepository;
        this.transactionExecutor = transactionExecutor;
        this.sqsGateway = sqsGateway;
    }

    @Override
    public Mono<Apply> saveApply(Apply apply) {
        return transactionExecutor.executeInTransaction(() ->
                userExtras.dataUser(apply.getNumberIdentity())
                        .switchIfEmpty(Mono.error(new UserNotExistsException("Usuario no encontrado en base de datos")))
                        .flatMap(userDTO -> {
                            System.out.println("Usuario encontrado en el sistema");
                            return loanTypeRepository.findById(apply.getIdLoanType())
                                    .flatMap(loanType -> {
                                        if (Boolean.TRUE.equals(loanType.getAutomaticValidation())) {
                                            return applyRepository.getActiveLoansByUserId(apply.getNumberIdentity())
                                                .collectList()
                                                .flatMap(activeLoans -> {
                                                    System.out.println("Validación automática habilitada. Enviando a cola.");
                                                    apply.setIdState(State.REVIEW.getIdState());
                                                    ApplyLoanMessage message = ApplyLoanMessage.builder()
                                                            .id(apply.getNumberIdentity())
                                                            .numberIdentity(apply.getNumberIdentity())
                                                            .email(userDTO.getEmail())
                                                            .salary(userDTO.getSalary())
                                                            .activeLoans(activeLoans)
                                                            .date(LocalDate.now())
                                                            .amount(apply.getAmount())
                                                            .timeLimit(apply.getTimeLimit())
                                                            .idLoanType(apply.getIdLoanType())
                                                            .rateInterest(loanType.getRateInterest())
                                                            .dateApply(apply.getDateApply())
                                                            .callBack(urlCallBack)
                                                            .build();

                                                    return applyRepository.saveApply(apply)
                                                            .flatMap(savedApply ->
                                                                    sqsGateway.sendToValidationQueue(message).thenReturn(savedApply));
                                                });
                                        } else {
                                            System.out.println("Validación automática deshabilitada. Estableciendo estado a PENDIENTE.");
                                            apply.setIdState(State.PENDING.getIdState());
                                            return applyRepository.saveApply(apply);
                                        }
                                    });
                        })
        );
    }
}
