package co.com.sti.r2dbc.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(WebClientConfigTest.TestConfig.class)
class WebClientConfigTest {

    @Autowired
    private WebClient authMSWebClient;

    @Autowired
    private ExchangeFunction exchangeFunction;

    @Configuration
    static class TestConfig {
        @Bean
        public ExchangeFunction exchangeFunction() {
            return mock(ExchangeFunction.class);
        }

        @Bean
        @Primary
        public WebClient authMSWebClient(ExchangeFunction exchangeFunction) {
            return WebClient.builder()
                    .baseUrl("http://localhost:8080")
                    .filter(new WebClientConfig().addAuthorizationHeader())
                    .exchangeFunction(exchangeFunction)
                    .build();
        }
    }

    @Test
    void authMSWebClient_withValidToken_shouldAddAuthorizationHeader() {
        String mockToken = "mock-jwt-token";
        String expectedAuthHeader = "Bearer " + mockToken;

        ClientResponse mockResponse = mock(ClientResponse.class);
        when(mockResponse.bodyToMono(any(Class.class))).thenReturn(Mono.empty());
        when(mockResponse.statusCode()).thenReturn(HttpStatus.OK);

        when(exchangeFunction.exchange(any(ClientRequest.class))).thenAnswer(invocation -> {
            ClientRequest request = invocation.getArgument(0);
            assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo(expectedAuthHeader);
            return Mono.just(mockResponse);
        });

        var securityContext = new org.springframework.security.core.context.SecurityContextImpl(
                new UsernamePasswordAuthenticationToken("user", mockToken)
        );

        Mono<Void> response = authMSWebClient.get()
                .uri("/test")
                .retrieve()
                .bodyToMono(Void.class)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(response)
                .verifyComplete();
    }

    @Test
    void authMSWebClient_withoutToken_shouldNotAddAuthorizationHeader() {
        ClientResponse mockResponse = mock(ClientResponse.class);
        when(mockResponse.bodyToMono(any(Class.class))).thenReturn(Mono.empty());
        when(mockResponse.statusCode()).thenReturn(HttpStatus.OK);

        when(exchangeFunction.exchange(any(ClientRequest.class))).thenAnswer(invocation -> {
            ClientRequest request = invocation.getArgument(0);
            assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isNull();
            return Mono.just(mockResponse);
        });

        Mono<Void> response = authMSWebClient.get()
                .uri("/test")
                .retrieve()
                .bodyToMono(Void.class);

        StepVerifier.create(response)
                .verifyComplete();
    }
}