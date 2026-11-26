package com.shopmate.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayRouteProperties {

    private Map<String, ServiceRoute> routes = new HashMap<>();

    public Map<String, ServiceRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, ServiceRoute> routes) {
        this.routes = routes;
    }

    public static class ServiceRoute {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
