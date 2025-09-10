package co.com.sti.api.config;

import co.com.sti.api.Handler;
import co.com.sti.api.RouterRest;
import co.com.sti.api.mapper.ApplyDTOMapper;
import co.com.sti.api.mapper.RequestDTOMapper;
import co.com.sti.usecase.applyloan.IApplyLoanUseCase;
import co.com.sti.usecase.requestapplylist.IRequestApplyListUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@Import({RouterRest.class, Handler.class, CorsConfig.class, SecurityHeadersConfig.class, ConfigTest.TestConfig.class, ConfigTest.SecurityTestConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Configuration
    static class SecurityTestConfig {
        @Bean
        public SecurityWebFilterChain securityTestFilterChain(ServerHttpSecurity http) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                    .build();
        }
    }

    @Configuration
    static class TestConfig {
        @Bean
        IApplyLoanUseCase registerUserUseCase() {
            return Mockito.mock(IApplyLoanUseCase.class);
        }

        @Bean
        IRequestApplyListUseCase requestApplyListUseCase() {
            return Mockito.mock(IRequestApplyListUseCase.class);
        }

        @Bean
        ApplyDTOMapper userDTOMapper() {
            return Mockito.mock(ApplyDTOMapper.class);
        }

        @Bean
        RequestDTOMapper requestDTOMapper() {
            return Mockito.mock(RequestDTOMapper.class);
        }

        @Bean
        Validator validator() {
            return Mockito.mock(Validator.class);
        }

    }

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.post()
                .uri("/api/v1/solicitud")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}