package co.com.sti.r2dbc.extras;

import co.com.sti.model.drivenports.IUserExtras;
import co.com.sti.model.drivenports.exceptions.ServiceUnavailableException;
import co.com.sti.r2dbc.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class UserExtrasImpl implements IUserExtras {

    private final WebClient getUserWebClient;

    public UserExtrasImpl(WebClient authMSWebClient) {
        this.getUserWebClient = authMSWebClient;
    }

    @Override
    public Mono<Boolean> verifyUser(String identification) {
        return getUserWebClient.get()
                .uri("/api/v1/usuarios/{identification}", identification)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .bodyToMono(Object.class)
                .map(user -> true)
                .defaultIfEmpty(false)
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("Error HTTP al validar el usuario: " + e.getRawStatusCode());
                    return Mono.error(new ServiceUnavailableException("El servicio de autenticación no está disponible."));
                })
                .onErrorResume(e -> {
                    System.err.println("Error de conexión con el servicio de autenticación: " + e.getMessage());
                    return Mono.error(new ServiceUnavailableException("El servicio de autenticación no está disponible."));
                });
    }

    public Mono<UserDTO> dataUser(String identification) {
        return getUserWebClient.get()
                .uri("/api/v1/usuarios/{identification}", identification)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .bodyToMono(UserDTO.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("Error HTTP al obtener datos del usuario: " + e.getRawStatusCode());
                    return Mono.error(new ServiceUnavailableException("El servicio de autenticación no está disponible."));
                })
                .onErrorResume(e -> {
                    System.err.println("Error de conexión con el servicio de autenticación: " + e.getMessage());
                    return Mono.error(new ServiceUnavailableException("El servicio de autenticación no está disponible."));
                });
    }
}
