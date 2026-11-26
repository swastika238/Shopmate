package com.shopmate.gateway.proxy;

import com.shopmate.gateway.config.GatewayRouteProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

/**
 * A simple reverse proxy that routes /api/** requests to the
 * matching downstream microservice based on the second path segment.
 *
 * Example: POST /api/auth/register → http://localhost:8081/api/auth/register
 */
@RestController
public class GatewayProxyController {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyController.class);

    // Headers that should NOT be forwarded (hop-by-hop or causes problems)
    private static final Set<String> HOP_BY_HOP = Set.of(
            "host", "connection", "content-length", "transfer-encoding"
    );

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, GatewayRouteProperties.ServiceRoute> routes;

    public GatewayProxyController(GatewayRouteProperties routeProperties) {
        this.routes = routeProperties.getRoutes();
    }

    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();          // e.g. /api/orders/5
        String queryString = request.getQueryString();   // e.g. quantity=3

        String serviceKey = resolveService(path);
        if (serviceKey == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"error\":\"No route for path: " + path + "\"}").getBytes());
        }

        GatewayRouteProperties.ServiceRoute route = routes.get(serviceKey);
        if (route == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"error\":\"Service not configured: " + serviceKey + "\"}").getBytes());
        }

        String targetUrl = route.getUrl() + path;
        if (queryString != null && !queryString.isEmpty()) {
            targetUrl += "?" + queryString;
        }

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!HOP_BY_HOP.contains(name.toLowerCase())) {
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    headers.add(name, values.nextElement());
                }
            }
        }

        // Read body
        byte[] body = request.getInputStream().readAllBytes();

        HttpEntity<byte[]> entity = new HttpEntity<>(body.length > 0 ? body : null, headers);

        try {
            ResponseEntity<byte[]> resp = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.valueOf(request.getMethod()),
                    entity,
                    byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            resp.getHeaders().forEach((k, v) -> {
                if (!HOP_BY_HOP.contains(k.toLowerCase())) {
                    responseHeaders.addAll(k, v);
                }
            });

            return new ResponseEntity<>(resp.getBody(), responseHeaders, resp.getStatusCode());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(e.getResponseBodyAsByteArray(), respHeaders, e.getStatusCode());
        } catch (ResourceAccessException e) {
            log.warn("Service {} is unreachable at {}: {}", serviceKey, route.getUrl(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"error\":\"Service " + serviceKey + " is currently unavailable\"}").getBytes());
        }
    }

    /**
     * Map the second segment of the path to a service key:
     *   /api/auth/**         → auth-service
     *   /api/products/**     → product-service
     *   /api/categories/**   → product-service
     *   /api/orders/**       → order-service
     *   /api/internal/orders/** → order-service
     *   /api/payments/**     → payment-service
     *   /api/notifications/**→ notification-service
     *   /api/chatbot/**      → chatbot-service
     */
    private String resolveService(String path) {
        if (path.startsWith("/api/auth"))            return "auth-service";
        if (path.startsWith("/api/products"))        return "product-service";
        if (path.startsWith("/api/categories"))      return "product-service";
        if (path.startsWith("/api/internal/orders")) return "order-service";
        if (path.startsWith("/api/orders"))           return "order-service";
        if (path.startsWith("/api/payments"))         return "payment-service";
        if (path.startsWith("/api/notifications"))    return "notification-service";
        if (path.startsWith("/api/chatbot"))           return "chatbot-service";
        return null;
    }
}
