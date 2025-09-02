package co.com.sti.api;

import co.com.sti.api.dto.ApplyDTO;
import co.com.sti.api.exceptions.InvalidUserDataException;
import co.com.sti.api.mapper.ApplyDTOMapper;
import co.com.sti.usecase.applyloan.IApplyLoanUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    private final IApplyLoanUseCase applyLoanUseCase;
    private final ApplyDTOMapper applyDTOMapper;
    private final Validator validator;

    public Mono<ServerResponse> applyLoanEntryPoint(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ApplyDTO.class)
                .flatMap(dto -> {
                    Set<ConstraintViolation<ApplyDTO>> violations = validator.validate(dto);
                    if(!violations.isEmpty()) {
                        String errorMessage = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                        return Mono.error(new InvalidUserDataException(errorMessage));
                    }
                    return Mono.just(dto);
                })
                .map(applyDTOMapper::toModel)
                .flatMap(model -> {
                    // Validamos que el mapper no haya retornado null
                    if (model == null) {
                        return Mono.error(new InvalidUserDataException("Error en el mapeo de datos: El modelo de dominio es nulo."));
                    }
                    return applyLoanUseCase.saveApply(model);
                })
                .flatMap(savedApply -> {
                    log.info("solicitud de prestamo guardada correctamente");
                    Map<String, String> successMessage = new HashMap<>();
                    successMessage.put("message", "Solicitud guardada exitosamente");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(successMessage);
                });
    }
}
