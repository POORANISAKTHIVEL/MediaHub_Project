package com.mediahub.subscriptionPlan.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .filter(propagateAuthorizationHeader())
                .build();
    }

    // Forwards the caller's incoming "Authorization" header onto every outbound
    // request so downstream secured services (audit log, notification, …)
    // can authenticate the service-to-service call.
    private ExchangeFilterFunction propagateAuthorizationHeader() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String authHeader = currentAuthorizationHeader();
            if (authHeader != null && !authHeader.isBlank()) {
                ClientRequest authorized = ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .build();
                return Mono.just(authorized);
            }
            return Mono.just(request);
        });
    }

    private String currentAuthorizationHeader() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            }
        } catch (IllegalStateException ignored) {
            // No request bound to the current thread (e.g. async/background call).
        }
        return null;
    }
}
