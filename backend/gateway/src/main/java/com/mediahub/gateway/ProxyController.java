package com.mediahub.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import jakarta.annotation.PostConstruct;
import java.util.Set;

/**
 * MediaHub API Gateway � ProxyController
 *
 * Routes all incoming requests to their respective microservices.
 * All 9 modules are accessible through this single gateway on port 8094.
 *
 * Gateway Path Formula:
 *   http://localhost:8094/{prefix}/**  -->  strips /{prefix}  -->  forwards to service
 *
 * Module Routes:
 *   /iam/**           --> IAM + Audit Log      (port 8091)
 *   /content/**       --> Content Catalog       (port 8093)
 *   /subscription/**  --> Subscription Plan     (port 8086)
 *   /licensing/**     --> Licensing             (port 8083)
 *   /editorial/**     --> Editorial             (port 9097)
 *   /royalty/**       --> Royalty               (port 8045)
 *   /notification/**  --> Notification          (port 8085)
 *   /analytics/**     --> Analytics             (port 8098)
 */
@RestController
public class ProxyController {

    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);

    // Hop-by-hop headers that must NOT be forwarded to upstream services.
    // "Origin" is included here too: downstream services should see the gateway as the caller,
    // not the browser. Forwarding the browser's real Origin (e.g. http://localhost:4200) makes
    // each microservice's own narrower CORS allow-list (typically just other backend ports)
    // reject the request outright with 403, even though the gateway itself already handles CORS
    // for the actual browser-facing hop.
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            HttpHeaders.HOST,
            HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.TRANSFER_ENCODING,
            HttpHeaders.CONNECTION,
            "Keep-Alive",
            HttpHeaders.PROXY_AUTHORIZATION,
            HttpHeaders.TE,
            HttpHeaders.TRAILER,
            HttpHeaders.UPGRADE,
            HttpHeaders.EXPECT,
            "Proxy-Connection",
            HttpHeaders.ORIGIN
    );

    // -- Downstream Service URLs -----------------------------------------------

    @Value("${iam.url:http://localhost:8091}")
    private String iamUrl;

    @Value("${contentcatalog.url:http://localhost:8093}")
    private String contentUrl;

    @Value("${subscription.url:http://localhost:8086}")
    private String subscriptionUrl;

    @Value("${licensing.url:http://localhost:8083}")
    private String licensingUrl;

    @Value("${editorial.url:http://localhost:9097}")
    private String editorialUrl;

    @Value("${royalty.url:http://localhost:8045}")
    private String royaltyUrl;

    @Value("${notification.url:http://localhost:8085}")
    private String notificationUrl;

    @Value("${analytics.url:http://localhost:8098}")
    private String analyticsUrl;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        HttpClient client = HttpClient.create().followRedirect(true);
        ClientHttpConnector connector = new ReactorClientHttpConnector(client);
        this.webClient = WebClient.builder()
                .clientConnector(connector)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    // -- Module 1 : IAM + Audit Log (port 8091) -------------------------------
    // Primary Access: http://localhost:8094/mediaHub/iam/auth/login/v1.0
    // Legacy Access: http://localhost:8094/iam/mediaHub/iam/... (backward compatibility)
    @RequestMapping(
        value  = "/mediaHub/iam/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyIamDirect(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, iamUrl, "", body);
    }

    // -- Module 1 : IAM + Audit Log (port 8091) - AuditLog route ---------------
    // Access: http://localhost:8094/mediaHub/auditlog/...
    @RequestMapping(
        value  = "/mediaHub/auditlog/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyAuditLog(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, iamUrl, "", body);
    }

    // -- Module 1 : IAM + Audit Log (port 8091) - Legacy Route ----------------
    // Backward compatibility: http://localhost:8094/iam/mediaHub/iam/...
    @RequestMapping(
        value  = "/iam/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyIam(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, iamUrl, "/iam", body);
    }

    // -- Module 2 : Content Catalog (port 8093) -------------------------------
    // Access: http://localhost:8094/content/mediahub/contentCatalog/...
    @RequestMapping(
        value  = "/content/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyContent(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, contentUrl, "/content", body);
    }

    // -- Module 3 : Subscription Plan (port 8086) -----------------------------
    // Access: http://localhost:8094/subscription/mediaHub/subscriptionPlan/...
    @RequestMapping(
        value  = "/subscription/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxySubscription(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, subscriptionUrl, "/subscription", body);
    }

    // -- Module 4 : Licensing (port 8083) -------------------------------------
    // Access: http://localhost:8094/licensing/mediaHub/contentLicensing/...
    @RequestMapping(
        value  = "/licensing/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyLicensing(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, licensingUrl, "/licensing", body);
    }

    // -- Module 5 : Editorial (port 9097) -------------------------------------
    // Access: http://localhost:8094/editorial/MediaHub/editorial/...
    @RequestMapping(
        value  = "/editorial/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyEditorial(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, editorialUrl, "/editorial", body);
    }

    // -- Module 6 : Royalty (port 8045) ---------------------------------------
    // Access: http://localhost:8094/royalty/api/royalty-statements/...
    @RequestMapping(
        value  = "/royalty/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyRoyalty(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, royaltyUrl, "/royalty", body);
    }

    // -- Module 7 : Notification (port 8085) ----------------------------------
    // Access: http://localhost:8094/notification/mediaHub/notifications/...
    @RequestMapping(
        value  = "/notification/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyNotification(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, notificationUrl, "/notification", body);
    }

    // -- Module 8 : Analytics (port 8098) -------------------------------------
    // Access: http://localhost:8094/analytics/mediaHub/analytics/...
    @RequestMapping(
        value  = "/analytics/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyAnalytics(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, analyticsUrl, "/analytics", body);
    }

    // -- Backward compatibility: /media/** still works for IAM ----------------
    @RequestMapping(
        value  = "/media/**",
        method = {RequestMethod.GET, RequestMethod.POST,
                  RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> proxyMediaLegacy(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<String> body) {
        return forward(exchange, iamUrl, "/media", body);
    }

    // -- Core proxy logic -----------------------------------------------------
    private Mono<Void> forward(
            ServerWebExchange exchange,
            String targetBase,
            String prefixToStrip,
            Mono<String> bodyMono) {

        String path = exchange.getRequest().getURI().getRawPath();
        String forwardPath = path.substring(prefixToStrip.length());
        if (forwardPath.isEmpty()) forwardPath = "/";

        String query = exchange.getRequest().getURI().getRawQuery();
        String uri   = targetBase + forwardPath + (query != null ? "?" + query : "");

        HttpMethod method = exchange.getRequest().getMethod();

        log.info("Gateway: {} {} --> {}", method, path, uri);

        WebClient.RequestBodySpec spec = webClient
                .method(method)
                .uri(uri)
                .headers(h -> {
                    exchange.getRequest().getHeaders().forEach((k, v) -> {
                        if (!HOP_BY_HOP_HEADERS.contains(k)) {
                            h.put(k, v);
                        }
                    });
                });

        if (method == HttpMethod.GET) {
            return spec.exchangeToMono(response -> writeResponse(exchange, response));
        }

        // NOTE: exchangeToMono(...) returns Mono<Void> (writeResponse's return type), and a
        // Mono<Void> can never emit a value — it only ever completes empty or errors. That means
        // switchIfEmpty(...) chained after a bodyMono.flatMap(...) here would ALWAYS fire its
        // fallback (since the flatMap's inner Mono<Void> always looks "empty" to switchIfEmpty),
        // sending every POST/PUT/PATCH/DELETE-with-body request to the upstream service TWICE —
        // the second call then tries to write a second response onto an already-committed
        // exchange and blows up with UnsupportedOperationException on ReadOnlyHttpHeaders. Using
        // defaultIfEmpty("") on bodyMono itself (so the flatMap always runs exactly once, with an
        // empty string when there truly was no body) avoids the double-send entirely.
        if (bodyMono != null) {
            return bodyMono
                    .defaultIfEmpty("")
                    .flatMap(b -> {
                        log.debug("Forwarding {} body: {}", method, b);
                        WebClient.RequestBodySpec withType = spec.contentType(MediaType.APPLICATION_JSON);
                        return (b.isEmpty() ? withType : withType.bodyValue(b))
                                .exchangeToMono(response -> writeResponse(exchange, response));
                    });
        }

        return spec.exchangeToMono(response -> writeResponse(exchange, response));
    }

    private Mono<Void> writeResponse(
            ServerWebExchange exchange,
            ClientResponse response) {

        log.info("Gateway upstream response: {}", response.statusCode());
        exchange.getResponse().setStatusCode(response.statusCode());
        response.headers().asHttpHeaders().forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name)) {
                exchange.getResponse().getHeaders().put(name, values);
            }
        });

        return exchange.getResponse()
                .writeWith(response.bodyToFlux(DataBuffer.class))
                .doOnError(error -> log.error("Error writing proxied response", error));
    }
}
