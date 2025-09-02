package co.com.sti.r2dbc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient hu1AuthWebClient() {
        // La URL base del microservicio de autenticacion.
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }
}
