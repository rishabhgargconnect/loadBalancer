package com.cloud.loadBalancer.configuration;

import com.cloud.loadBalancer.interceptors.HttpReqHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public HttpReqHandlerInterceptor httpReqHandlerInterceptor() {
        return new HttpReqHandlerInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpReqHandlerInterceptor());
    }
}
