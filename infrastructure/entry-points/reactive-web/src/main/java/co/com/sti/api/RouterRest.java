package co.com.sti.api;

import co.com.sti.api.dto.ApplyDTO;
import co.com.sti.api.dto.RequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    private static final String PATH_APPLY = "/api/v1/solicitud";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = PATH_APPLY,
                    beanClass = Handler.class,
                    beanMethod = "applyLoanEntryPoint",
                    method = RequestMethod.POST,
                    operation = @Operation(
                            operationId = "saveApply",
                            summary = "Guardar una nueva solicitud de prestamo",
                            description = "Recibe un objeto ApplyDTO y la guarda en el sistema",
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos de la solicitud a registrar",
                                    content = @Content(schema = @Schema(implementation = ApplyDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Solicitud guardada correctamente",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class))),
                                    @ApiResponse(responseCode = "400", description = "Error de validación de datos",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class))),
                                    @ApiResponse(responseCode = "409", description = "Error de identificación no existente",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = PATH_APPLY,
                    beanClass = Handler.class,
                    beanMethod = "listLoanAppliesEntryPoint",
                    method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "listLoanApplies",
                            summary = "Listar solicitudes de prestamo para revisión manual",
                            description = "Obtiene una lista paginada y ordenada de solicitudes que requieren revisión. Por defecto, ordena por 'email' de forma ascendente y muestra 10 elementos por página.",
                            parameters = {
                                    @Parameter(in = ParameterIn.QUERY, name = "page", description = "Número de página (comienza en 0)", schema = @Schema(type = "integer", defaultValue = "0")),
                                    @Parameter(in = ParameterIn.QUERY, name = "size", description = "Cantidad de elementos por página", schema = @Schema(type = "integer", defaultValue = "10")),
                                    @Parameter(in = ParameterIn.QUERY, name = "sortBy", description = "Campo por el cual ordenar", schema = @Schema(type = "string", defaultValue = "dateApply")),
                                    @Parameter(in = ParameterIn.QUERY, name = "sortOrder", description = "Orden del listado (asc o desc)", schema = @Schema(type = "string", defaultValue = "asc"))
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Lista de solicitudes obtenida correctamente",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(type = "array", implementation = RequestDTO.class))),
                                    @ApiResponse(responseCode = "204", description = "No se encontraron solicitudes para revisar",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class)))
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(PATH_APPLY), handler::applyLoanEntryPoint)
                .andRoute(GET(PATH_APPLY), handler::listLoanAppliesEntryPoint);
    }
}
