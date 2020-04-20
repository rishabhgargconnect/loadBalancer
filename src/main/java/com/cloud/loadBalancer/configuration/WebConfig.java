package com.cloud.loadBalancer.configuration;

import com.cloud.loadBalancer.interceptors.HttpReqHandlerInterceptorRoundRobin;
import com.cloud.loadBalancer.interceptors.HttpReqHandlerInterceptorVMTasksMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public HttpReqHandlerInterceptorRoundRobin httpReqHandlerInterceptor() {
        return new HttpReqHandlerInterceptorRoundRobin();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpReqHandlerInterceptor());
    }
}
