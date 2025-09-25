package co.com.sti.model.sqsservices.gateways;

import co.com.sti.model.sqsservices.ApplyLoanMessage;
import co.com.sti.model.sqsservices.DataForReports;
import co.com.sti.model.sqsservices.Notification;
import reactor.core.publisher.Mono;

public interface SQSGateway {
    Mono<Void> sendNotification(Notification notification);
    Mono<Void> sendToValidationQueue(ApplyLoanMessage message);
    Mono<Void> sendToAprovedCount(DataForReports dataForReports);
}
