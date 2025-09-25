package co.com.sti.api;

import co.com.sti.api.dto.ApplyDTO;
import co.com.sti.api.dto.ApplyUpdateDTO;
import co.com.sti.api.dto.RequestDTO;
import co.com.sti.api.exceptions.InvalidUserDataException;
import co.com.sti.api.exceptions.NotContentException;
import co.com.sti.api.mapper.ApplyDTOMapper;
import co.com.sti.api.mapper.RequestDTOMapper;
import co.com.sti.model.paginator.Pagination;
import co.com.sti.model.paginator.SortBy;
import co.com.sti.model.state.State;
import co.com.sti.usecase.applyloan.IApplyLoanUseCase;
import co.com.sti.usecase.requestapplylist.IRequestApplyListUseCase;
import co.com.sti.usecase.updateapply.IUpdateApplyUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    private final IApplyLoanUseCase applyLoanUseCase;
    private final IRequestApplyListUseCase requestApplyListUseCase;
    private final IUpdateApplyUseCase updateApplyUseCase;
    private final ApplyDTOMapper applyDTOMapper;
    private final RequestDTOMapper requestDTOMapper;
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

    public Mono<ServerResponse> listLoanAppliesEntryPoint(ServerRequest serverRequest) {
        int page = Integer.parseInt(serverRequest.queryParam("page").orElse("0"));
        int size = Integer.parseInt(serverRequest.queryParam("size").orElse("10"));
        String sortByProperty = serverRequest.queryParam("sortBy").orElse("dateApply");
        String sortDirection = serverRequest.queryParam("sortOrder").orElse("asc");

        SortBy sortBy = SortBy.builder()
                .property(sortByProperty)
                .direction(sortDirection)
                .build();

        Pagination pagination = Pagination.builder()
                .page(page)
                .size(size)
                .sortBy(Collections.singletonList(sortBy))
                .build();

        return requestApplyListUseCase.applyList(pagination)
                .flatMap(pagedResponse  -> {
                    if (pagedResponse.getContent().isEmpty()) {
                        log.warn("No se encontraron solicitudes. Devolviendo 204 Not Content.");
                        return Mono.error(new NotContentException("No se encontraron solicitudes para revisar"));
                    } else {
                        log.info("Listado de solicitudes encontrado");

                        List<RequestDTO> requestDTOs = pagedResponse.getContent().stream()
                                .map(requestDTOMapper::toDTO)
                                .collect(Collectors.toList());

                        Map<String, Object> responseData = new HashMap<>();
                        responseData.put("content", requestDTOs);
                        responseData.put("page", pagedResponse.getPage());
                        responseData.put("size", pagedResponse.getSize());
                        responseData.put("totalElements", pagedResponse.getTotalElements());
                        responseData.put("totalPages", pagedResponse.getTotalPages());
                        responseData.put("last", pagedResponse.isLast());

                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(responseData);
                    }
                });
    }

    public Mono<ServerResponse> updateApplyEntryPoint(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ApplyUpdateDTO.class)
                .flatMap(dto -> {
                    if (dto.getIdApply() == null || dto.getIdState() == null) {
                        return Mono.error(new InvalidUserDataException("El ID de la solicitud y el estado son obligatorios."));
                    }
                    return updateApplyUseCase.update(dto.getIdApply(), dto.getIdState(), true);
                })
                .flatMap(updatedApply -> {
                    log.info("Solicitud actualizada con éxito");
                    Map<String, String> successMessage = new HashMap<>();
                    successMessage.put("message", "Solicitud actualizada con éxito.");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(successMessage);
                });
    }

    public Mono<ServerResponse> handleCallback(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ApplyUpdateDTO.class)
                .flatMap(dto -> {
                    log.info("Callback recibido para el préstamo con ID {} y nuevo estado {}", dto.getIdApply(), dto.getIdState());
                    return updateApplyUseCase.update(dto.getIdApply(), dto.getIdState(), false);
                })
                .flatMap(updatedApply -> {
                    log.info("Solicitud de préstamo actualizada con éxito a estado {}", State.getById(updatedApply.getIdState()).getName() );
                    Map<String, String> successMessage = new HashMap<>();
                    successMessage.put("message", "Solicitud actualizada con éxito.");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(successMessage);
                });
    }
}
