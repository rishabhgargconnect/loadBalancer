package com.cloud.loadBalancer.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpReqHandlerInterceptor implements HandlerInterceptor {
    private static List<String> servers;
    private AtomicInteger curServerIndex = new AtomicInteger(1);
    private Map<String, ConcurrentHashMap<String, Long>> vm_api_to_time_map = new ConcurrentHashMap<>();
    private Map<String, String> vm_to_cpu_map = new ConcurrentHashMap<>();
    private Map<String, String> vm_to_ram_map = new ConcurrentHashMap<>();


    public HttpReqHandlerInterceptor() {
        List<String> serverNames = new ArrayList<>();
        serverNames.add("http://localhost:9090");
        serverNames.add("http://localhost:9091");
        servers = Collections.unmodifiableList(serverNames);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String serverPath = servers.get(curServerIndex.getAndUpdate(prev -> {
            if (prev == servers.size() - 1) {
                return 0;
            }
            return prev + 1;
        }) % servers.size());
        System.out.println("server path = " + serverPath);
        System.out.println("req URL = ");
        StringBuffer requestURL = request.getRequestURL();
        System.out.println("req URL = " + requestURL);
        String requestURI = request.getRequestURI();
        System.out.println("req URI = " + requestURI);
        Enumeration<String> parameterNames = request.getParameterNames();
        Map<String, String> params = new HashMap<>();
        while (parameterNames.hasMoreElements()) {
            String paramKey = parameterNames.nextElement();
            String paramVal = request.getParameter(paramKey);
            params.put(paramKey, paramVal);
        }

        String body = IOUtils.toString(request.getReader());

        RestTemplate restTemplate = new RestTemplate();
        String httpMethod = request.getMethod();
        if (httpMethod.equals(HttpMethod.GET.name())) {
            HttpEntity<String> entity = new HttpEntity<>(getHttpHeaders(request));
            ResponseEntity<Object> responseEntity = restTemplate.exchange(createUri(params, serverPath + requestURI), HttpMethod.GET, entity, Object.class);
            convertResponseEntityToHttpServletResponse(responseEntity, response);
        } else if (httpMethod.equals(HttpMethod.POST.name())) {
            HttpEntity<String> entity = new HttpEntity<>(body, getHttpHeaders(request));
            try {
                ResponseEntity<Object> responseEntity = restTemplate.exchange(createUri(params, serverPath + requestURI), HttpMethod.POST, entity, Object.class);

                convertResponseEntityToHttpServletResponse(responseEntity, response);


            } catch (Exception e) {
                System.out.println("error is : " + e.getMessage());
            }

        }
        return false;
    }

    private URI createUri(Map<String, String> params, String requestPath) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestPath);

        if (params != null) {
            params.forEach(builder::queryParam);
        }
        URI uri = builder.build().encode().toUri();
        return uri;
    }

    private HttpHeaders getHttpHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.add(headerName, headerValue);

        }
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.set(HttpHeaders.ACCEPT, "application/json");
        return headers;
    }


    private void convertResponseEntityToHttpServletResponse(ResponseEntity<Object> responseEntity, HttpServletResponse response) {
        try {
            for (Map.Entry<String, List<String>> header : responseEntity.getHeaders().entrySet()) {
                String headerKey = header.getKey();
                for (String headerValue : header.getValue()) {
                    if (!"chunked".equals(headerValue))
                        response.addHeader(headerKey, headerValue);
                }
            }
            response.setStatus(responseEntity.getStatusCodeValue());
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
        } catch (IOException e) {
            response.setStatus(500);
            e.printStackTrace();
        }
    }
}
