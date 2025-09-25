package co.com.sti.usecase.updateapply;

import co.com.sti.model.apply.Apply;
import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.drivenports.IUserExtras;
import co.com.sti.model.sqsservices.Notification;
import co.com.sti.model.sqsservices.gateways.SQSGateway;
import co.com.sti.model.state.State;
import co.com.sti.usecase.exception.ApplyNotExistsException;
import co.com.sti.usecase.exception.InvalidStatusUpdateException;
import co.com.sti.usecase.transaction.TransactionExecutor;
import reactor.core.publisher.Mono;

public class UpdateApplyUseCase implements IUpdateApplyUseCase{
    private final ApplyRepository applyRepository;
    private final SQSGateway sqsGateway;
    private final IUserExtras userExtras;
    private final TransactionExecutor transactionExecutor;

    public UpdateApplyUseCase(ApplyRepository applyRepository, SQSGateway sqsGateway, IUserExtras userExtras, TransactionExecutor transactionExecutor) {
        this.applyRepository = applyRepository;
        this.sqsGateway = sqsGateway;
        this.userExtras = userExtras;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public Mono<Apply> update(Long idApply, Integer idState, Boolean notify) {
        if (!State.isFinalState(idState)) {
            return Mono.error(new InvalidStatusUpdateException("El estado debe ser Aprobado o Rechazado."));
        }

        // 1. Validar que la solicitud exista y actualizar el estado
        return transactionExecutor.executeInTransaction(() ->

            applyRepository.updateStateOfApply(idApply, idState)
                    .switchIfEmpty(Mono.error(new ApplyNotExistsException("Solicitud con ID " + idApply + " no encontrada.")))
                    // 2. Enviar el mensaje a SQS
                    .flatMap(updatedApply ->
                            userExtras.dataUser(updatedApply.getNumberIdentity())
                                    .flatMap(user -> {
                                        if(updatedApply.getIdState().equals(State.APPROVED.getIdState())){
                                            sqsGateway.sendToAprovedCount(updatedApply.getIdApply());
                                        }
                                        if(notify){
                                            String customMessage;
                                            if (State.getById(idState) == State.APPROVED) {
                                                customMessage = "¡Felicidades! 🎉 Su solicitud de crédito ha sido aprobada. En breve, uno de nuestros asesores se pondrá en contacto con usted para continuar con el proceso.";
                                            } else {
                                                customMessage = "Lamentamos informarle que su solicitud de crédito ha sido rechazada. Hemos revisado cuidadosamente su perfil y por el momento no cumple con los criterios. Le invitamos a intentarlo nuevamente en el futuro.";
                                            }
                                            Notification notification = Notification.builder()
                                                    .applyId(idApply)
                                                    .applicantNames(user.getName() + " " + user.getLastName())
                                                    .applicantEmail(user.getEmail())
                                                    .amount(updatedApply.getAmount())
                                                    .dateApply(updatedApply.getDateApply())
                                                    .newState(State.getById(idState).name())
                                                    .message(customMessage)
                                                    .build();
                                            return sqsGateway.sendNotification(notification)
                                                    .thenReturn(updatedApply);
                                        }else{
                                            return Mono.just(updatedApply);
                                        }
                                    })
                                    .onErrorResume(e -> {
                                        // Manejo del error de SQS para no detener el flujo principal
                                        return Mono.error(new RuntimeException("Ocurrió un error al enviar la notificación.", e));
                                    })
                    )
        );
    }
}
