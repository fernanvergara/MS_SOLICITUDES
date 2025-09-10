package co.com.sti.r2dbc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {

    @Bean
    public WebClient authMSWebClient() {
        // La URL base del microservicio de autenticacion.
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .filter(addAuthorizationHeader())
                .build();
    }

    public ExchangeFilterFunction addAuthorizationHeader() {
        return (request, next) -> ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication != null && authentication.getCredentials() instanceof String) {
                        String token = (String) authentication.getCredentials();
                        log.info("Token extraído: {}", token);
                        return ClientRequest.from(request)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .build();
                    }
                    return request;
                })
                .defaultIfEmpty(request)
                .flatMap(next::exchange);
    }
}
