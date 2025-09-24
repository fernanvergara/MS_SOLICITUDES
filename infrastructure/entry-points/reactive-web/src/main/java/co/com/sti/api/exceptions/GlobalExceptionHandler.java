package co.com.sti.api.exceptions;

import co.com.sti.model.drivenports.exceptions.ServiceUnavailableException;
import co.com.sti.usecase.exception.ApplyNotExistsException;
import co.com.sti.usecase.exception.InvalidStatusUpdateException;
import co.com.sti.usecase.exception.UserAlreadyExistsException;
import co.com.sti.usecase.exception.UserNotExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@Order(-2)
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Conflicto");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
    }

    @ExceptionHandler(UserNotExistsException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleUserNotExists(UserNotExistsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "No encontrado");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleInvalidUserData(InvalidUserDataException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Datos de usuario inválidos");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleServiceUnavailableException(ServiceUnavailableException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Servicio no disponible");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse));
    }

    @ExceptionHandler(NotContentException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleNotContentException(NotContentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Sin contenido");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body(errorResponse));
    }

    @ExceptionHandler(ApplyNotExistsException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleApplyNotExistsException(ApplyNotExistsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Solicitud no encontrada");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(InvalidStatusUpdateException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleInvalidStatusUpdateException(InvalidStatusUpdateException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Actualización de estado inválida");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handleAllException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Error interno del servidor");
        errorResponse.put("message", "Ocurrió un error inesperado: " + ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

}
