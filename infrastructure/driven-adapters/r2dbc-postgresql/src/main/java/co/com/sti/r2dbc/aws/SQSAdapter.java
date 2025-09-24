package co.com.sti.r2dbc.aws;

import co.com.sti.model.notification.Notification;
import co.com.sti.model.notification.gateways.SQSGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Component
public class SQSAdapter implements SQSGateway {

    private final ObjectMapper objectMapper;
    private final SqsAsyncClient sqsAsyncClient;

    @Value("${aws.sqs.queueName}")
    private String queueName;

    public SQSAdapter(ObjectMapper objectMapper, SqsAsyncClient sqsAsyncClient) {
        this.objectMapper = objectMapper;
        this.sqsAsyncClient = sqsAsyncClient;
    }

    @Override
    public Mono<Void> sendNotification(Notification notification) {
        // Encadenado de las operaciones reactivamente
        return Mono.fromCallable(() -> {
                    try {
                        return objectMapper.writeValueAsString(notification);
                    } catch (JsonProcessingException e) {
                        log.error("Error al convertir la notificación a JSON: {}", e.getMessage());
                        throw new RuntimeException("Error de serialización de la notificación.", e);
                    }
                })
                .flatMap(messageBody -> {
                    // Obtener la URL de la cola de forma no-bloqueante
                    GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
                    return Mono.fromFuture(sqsAsyncClient.getQueueUrl(getQueueUrlRequest))
                            .flatMap(getQueueUrlResponse -> {
                                String queueUrl = getQueueUrlResponse.queueUrl();
                                SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                                        .queueUrl(queueUrl)
                                        .messageBody(messageBody)
                                        .build();

                                // Enviar el mensaje de forma no-bloqueante
                                return Mono.fromFuture(sqsAsyncClient.sendMessage(sendMessageRequest))
                                        .doOnSuccess(response -> log.info("Mensaje enviado a SQS con éxito."))
                                        .then();
                            });
                });
    }
}