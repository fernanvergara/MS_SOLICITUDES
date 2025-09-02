package co.com.sti.r2dbc.auth;

import co.com.sti.model.drivenports.IUserExistenceChecker;
import co.com.sti.model.drivenports.exceptions.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class UserExistenceCheckerImpl implements IUserExistenceChecker {

    private final WebClient verifiyUserWebClient;

    public UserExistenceCheckerImpl(WebClient verifiyUserWebClient) {
        this.verifiyUserWebClient = verifiyUserWebClient;
    }

    @Override
    public Mono<Boolean> verifyUser(String identificaction) {
        return verifiyUserWebClient.get()
                .uri("/api/v1/usuarios/{identification}", identificaction)
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
}
