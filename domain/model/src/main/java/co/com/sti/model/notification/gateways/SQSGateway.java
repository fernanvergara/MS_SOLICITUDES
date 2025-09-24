package co.com.sti.model.notification.gateways;

import co.com.sti.model.notification.Notification;
import reactor.core.publisher.Mono;

public interface SQSGateway {
    Mono<Void> sendNotification(Notification notification);
}
