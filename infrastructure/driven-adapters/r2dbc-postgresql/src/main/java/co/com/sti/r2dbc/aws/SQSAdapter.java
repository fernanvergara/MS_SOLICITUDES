package co.com.sti.r2dbc.aws;

import co.com.sti.model.sqsservices.ApplyLoanMessage;
import co.com.sti.model.sqsservices.DataForReports;
import co.com.sti.model.sqsservices.Notification;
import co.com.sti.model.sqsservices.gateways.SQSGateway;
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

    @Value("${aws.sqs.notificationsQueueName}")
    private String notificationsQueueName;

    @Value("${aws.sqs.loanValidationQueueName}")
    private String loanValidationQueueName;

    @Value("${aws.sqs.loanApprovedCountQueueName}")
    private String loanApprovedCountQueueName;

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
                        return Mono.error( new RuntimeException("Error de serialización de la notificación.", e) );
                    }
                })
                .flatMap(messageBody -> {
                    // Obtener la URL de la cola de forma no-bloqueante
                    GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(notificationsQueueName).build();
                    return Mono.fromFuture(sqsAsyncClient.getQueueUrl(getQueueUrlRequest))
                            .flatMap(getQueueUrlResponse -> {
                                String queueUrl = getQueueUrlResponse.queueUrl();
                                SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                                        .queueUrl(queueUrl)
                                        .messageBody((String) messageBody)
                                        .build();

                                // Enviar el mensaje de forma no-bloqueante
                                return Mono.fromFuture(sqsAsyncClient.sendMessage(sendMessageRequest))
                                        .doOnSuccess(response -> log.info("Mensaje enviado a SQS con éxito."))
                                        .then();
                            });
                });
    }

    @Override
    public Mono<Void> sendToValidationQueue(ApplyLoanMessage message) {
        log.info("Enviando mensaje a la cola de validación: {}", message);
        return Mono.fromCallable(() -> {
                    try {
                        return objectMapper.writeValueAsString(message);
                    } catch (JsonProcessingException e) {
                        log.error("Error al convertir el mensaje de validación a JSON: {}", e.getMessage());
                        return Mono.error( new RuntimeException("Error de serialización del mensaje de validación.", e) );
                    }
                })
                .flatMap(messageBody -> {
                    GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(loanValidationQueueName).build();
                    return Mono.fromFuture(sqsAsyncClient.getQueueUrl(getQueueUrlRequest))
                            .flatMap(getQueueUrlResponse -> {
                                String queueUrl = getQueueUrlResponse.queueUrl();
                                SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                                        .queueUrl(queueUrl)
                                        .messageBody((String) messageBody)
                                        .build();

                                return Mono.fromFuture(sqsAsyncClient.sendMessage(sendMessageRequest))
                                        .doOnSuccess(response -> log.info("Mensaje enviado a la cola de validación con éxito."))
                                        .then();
                            });
                });
    }

    @Override
    public Mono<Void> sendToAprovedCount(DataForReports dataForReports) {
        log.info("Enviando mensaje a la cola de reportes: {}", dataForReports);
        return Mono.fromCallable(() -> {
                    try {
                        return objectMapper.writeValueAsString(dataForReports);
                    } catch (JsonProcessingException e) {
                        log.error("Error al convertir los datos para reportes a JSON: {}", e.getMessage());
                        return Mono.error( new RuntimeException("Error de serialización de los datos para reportes.", e) );
                    }
                })
                .flatMap(messageBody -> {
                    GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(loanApprovedCountQueueName).build();
                    return Mono.fromFuture(sqsAsyncClient.getQueueUrl(getQueueUrlRequest))
                            .flatMap(getQueueUrlResponse -> {
                                String queueUrl = getQueueUrlResponse.queueUrl();
                                SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                                        .queueUrl(queueUrl)
                                        .messageBody((String) messageBody)
                                        .build();

                                return Mono.fromFuture(sqsAsyncClient.sendMessage(sendMessageRequest))
                                        .doOnSuccess(response -> log.info("Mensaje enviado a la cola de contador de prestamos aprobados con éxito."))
                                        .then();
                            });
                });
    }

}