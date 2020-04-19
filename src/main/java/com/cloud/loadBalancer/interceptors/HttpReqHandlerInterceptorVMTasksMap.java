package com.cloud.loadBalancer.interceptors;

import com.cloud.loadBalancer.beans.HttpRequestAllParamaters;
import com.cloud.loadBalancer.beans.VMTasksMap;
import com.cloud.loadBalancer.exceptionHandler.ExceptionQueue;
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

public class HttpReqHandlerInterceptorVMTasksMap implements HandlerInterceptor {
    private static List<String> servers;
    private VMTasksMap vmTasksMap1;
    private VMTasksMap vmTasksMap2;
    private List<VMTasksMap> vmTasksMaps;
    private Map<String, ConcurrentHashMap<String, Long>> vm_api_to_time_map = new ConcurrentHashMap<>();
    private final Random random = new Random();


    public HttpReqHandlerInterceptorVMTasksMap() {
        List<String> serverNames = new ArrayList<>();
        serverNames.add("http://localhost:9090");
        serverNames.add("http://localhost:9091");
        servers = Collections.unmodifiableList(serverNames);

        vmTasksMap1 = new VMTasksMap();
        vmTasksMap2 = new VMTasksMap();
        List<VMTasksMap> vmTasksMapTempList = new ArrayList<>();
        vmTasksMapTempList.add(vmTasksMap1);
        vmTasksMapTempList.add(vmTasksMap2);
        vmTasksMaps = Collections.unmodifiableList(vmTasksMapTempList);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().contains("/exceptionQueueLength") || request.getRequestURI().contains("/handleException")) {
            return true;
        } else {
            int minTasksLeftVmIndex = getVmIndexWithMinimumTasks();
            System.out.println("minTasksLeftVmIndex = " + minTasksLeftVmIndex);
            //TODO Can insert a random task below, if just looking at length and nnot taking anything weighted(else take weighted)
            int randKey = random.nextInt();
            VMTasksMap selectedVmTasksMap = vmTasksMaps.get(minTasksLeftVmIndex);
            while (selectedVmTasksMap.containsKey(randKey)) {
                randKey = random.nextInt();
            }
            selectedVmTasksMap.addTask(randKey, new Object());
            String serverPath = servers.get(minTasksLeftVmIndex);

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
                System.out.println("######################REQUEST URI IS" + requestURI);
                HttpEntity<String> entity = new HttpEntity<>(getHttpHeaders(request));
                try {
                    ResponseEntity<Object> responseEntity = restTemplate.exchange(createUri(params, serverPath, requestURI), HttpMethod.GET, entity, Object.class);
                    convertResponseEntityToHttpServletResponse(responseEntity, response);
                } catch (Exception e) {
                    System.out.println("error is : " + e.getMessage());
                    ExceptionQueue.addToExceptionHandleQueue(new HttpRequestAllParamaters(params, serverPath, requestURI, HttpMethod.GET, entity));
                    System.out.println("Added to exception queue whos length is" + ExceptionQueue.exceptionQueueLength());


                }
            } else if (httpMethod.equals(HttpMethod.POST.name())) {
                HttpEntity<String> entity = new HttpEntity<>(body, getHttpHeaders(request));
                URI uri = null;
                try {
                    uri = createUri(params, serverPath, requestURI);
                    ResponseEntity<Object> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, Object.class);
                    convertResponseEntityToHttpServletResponse(responseEntity, response);
                } catch (Exception e) {
                    System.out.println("error is : " + e.getMessage());
                    ExceptionQueue.addToExceptionHandleQueue(new HttpRequestAllParamaters(params, serverPath, requestURI, HttpMethod.POST, entity));
                    System.out.println("Added to exception queue whos length is" + ExceptionQueue.exceptionQueueLength());
                }

            }
            selectedVmTasksMap.removeTaskWithKey(randKey);
            return false;
        }
    }

    private int getVmIndexWithMinimumTasks() {
        int minSize = Integer.MAX_VALUE;
        int minTasksLeftVmIndex = -1;
        for (int i = 0; i < vmTasksMaps.size(); i++) {
            int vmQueueSize = vmTasksMaps.get(i).getSize();
            if (vmQueueSize < minSize) {
                minSize = vmQueueSize;
                minTasksLeftVmIndex = i;
            }
        }
        return minTasksLeftVmIndex;
    }

    private URI createUri(Map<String, String> params, String serverPath, String requestUri) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverPath + requestUri);

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
