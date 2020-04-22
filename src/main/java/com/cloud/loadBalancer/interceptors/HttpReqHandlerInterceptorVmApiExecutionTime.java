package com.cloud.loadBalancer.interceptors;

import com.cloud.loadBalancer.beans.ApiToVmExecTime;
import com.cloud.loadBalancer.beans.HttpRequestAllParamaters;
import com.cloud.loadBalancer.beans.VMTasksMap;
import com.cloud.loadBalancer.beans.VmExecTimeToTaskEncounteredCount;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpReqHandlerInterceptorVmApiExecutionTime implements HandlerInterceptor {
    private static List<String> servers;
    private AtomicInteger curServerIndex = new AtomicInteger(1);
    private VMTasksMap vmTasksMap1;
    private VMTasksMap vmTasksMap2;
    private List<VMTasksMap> vmTasksMaps;
    private Map<String, String> vm_to_cpu_map = new ConcurrentHashMap<>();
    private Map<String, String> vm_to_ram_map = new ConcurrentHashMap<>();
    private ApiToVmExecTime apiToVmExecTime;


    public HttpReqHandlerInterceptorVmApiExecutionTime() {
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
        apiToVmExecTime = new ApiToVmExecTime();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        int vmIdWithMinimumExecutionTime = getVmIdWithMinimumExecutionTime(request.getRequestURI());
        //TODO Add logic to check if above vmId has less load on it to do the task
        String serverPath = servers.get(vmIdWithMinimumExecutionTime);
        long executionTimeTakenForCurrentTask = 0L;


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
            try {
                Long executionStartTime = System.currentTimeMillis();
                ResponseEntity<Object> responseEntity = restTemplate.exchange(createUri(params, serverPath + requestURI), HttpMethod.GET, entity, Object.class);
                executionTimeTakenForCurrentTask = System.currentTimeMillis() - executionStartTime;
                convertResponseEntityToHttpServletResponse(responseEntity, response);
            } catch (Exception e) {
                System.out.println("error is : " + e.getMessage());
                ExceptionQueue.addToExceptionHandleQueue(new HttpRequestAllParamaters(params, serverPath, requestURI, HttpMethod.GET, entity));
            }
        } else if (httpMethod.equals(HttpMethod.POST.name())) {
            HttpEntity<String> entity = new HttpEntity<>(body, getHttpHeaders(request));
            try {
                Long executionStartTime = System.currentTimeMillis();

                ResponseEntity<Object> responseEntity = restTemplate.exchange(createUri(params, serverPath + requestURI), HttpMethod.POST, entity, Object.class);
                executionTimeTakenForCurrentTask = System.currentTimeMillis() - executionStartTime;

                convertResponseEntityToHttpServletResponse(responseEntity, response);
            } catch (Exception e) {
                System.out.println("error is : " + e.getMessage());
                ExceptionQueue.addToExceptionHandleQueue(new HttpRequestAllParamaters(params, serverPath, requestURI, HttpMethod.GET, entity));
            }

        }
        apiToVmExecTime.setExecTimeForApiVmPair(requestURI, vmIdWithMinimumExecutionTime, executionTimeTakenForCurrentTask);
        return false;
    }

    private int getVmIdWithMinimumExecutionTime(String api) {
        List<VmExecTimeToTaskEncounteredCount> vmExecutionTimesForApi = apiToVmExecTime.getVmExecutionTimesForApi(api);
        //Comparator<VmExecTimeToTaskEncounteredCount> compareById = (VmExecTimeToTaskEncounteredCount o1, VmExecTimeToTaskEncounteredCount o2) -> o1.getExecutionTime().compareTo( o2.getExecutionTime() );
        //vmExecutionTimesForApi.sort(compareById);
        //Collections.sort(vmExecutionTimesForApi, compareById);
        //int vmIdWithMinimumExecutionTime = vmExecutionTimesForApi.get(0).getVmId();
        long minExecTime = Long.MAX_VALUE;
        int vmIdWithMinimumExecutionTime = -1;
        //System.out.println("print the execution time");
        for (int i = 0; i < vmExecutionTimesForApi.size(); i++) {
            //System.out.println("Vm id "+i);
            //System.out.println(vmExecutionTimesForApi.get(i).getExecutionTime());
            if (vmExecutionTimesForApi.get(i).getExecutionTime() <= minExecTime) {
                minExecTime = vmExecutionTimesForApi.get(i).getExecutionTime();
                vmIdWithMinimumExecutionTime = i;
            }
        }
        return vmIdWithMinimumExecutionTime;
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
