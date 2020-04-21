package com.cloud.loadBalancer.configuration;

import com.cloud.loadBalancer.interceptors.HttpReqHandlerInterceptorOverload;
import com.cloud.loadBalancer.interceptors.HttpReqHandlerInterceptorRoundRobin;
import com.cloud.loadBalancer.interceptors.HttpReqHandlerInterceptorVMTasksMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public HttpReqHandlerInterceptorOverload httpReqHandlerInterceptor() {
        return new HttpReqHandlerInterceptorOverload();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpReqHandlerInterceptor());
    }
}
