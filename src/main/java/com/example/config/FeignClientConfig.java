package com.example.config;

import org.springframework.context.annotation.Configuration;
import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class FeignClientConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        String token = AuthContext.getToken();

        if (token != null && !token.isBlank()) {
            template.header("Authorization", token);
        }
    }
}
