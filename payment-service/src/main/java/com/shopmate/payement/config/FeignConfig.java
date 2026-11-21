package com.shopmate.payement.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Adds the shared internal API key to every call made to order-service. */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor internalKeyInterceptor(@Value("${internal.api-key}") String apiKey) {
        return template -> template.header("X-Internal-Key", apiKey);
    }
}
