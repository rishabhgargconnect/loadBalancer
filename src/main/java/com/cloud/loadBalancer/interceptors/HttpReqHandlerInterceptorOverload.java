package com.cloud.loadBalancer.interceptors;

import com.cloud.loadBalancer.beans.ApiToVmExecTime;
import com.cloud.loadBalancer.beans.ControllerStats;
import com.cloud.loadBalancer.beans.VmExecTimeToTaskEncounteredCount;
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
import java.lang.reflect.Array;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class HttpReqHandlerInterceptorOverload implements HandlerInterceptor {
    private static List<String> servers;
    public static  ControllerStats controllerStats;
    private ApiToVmExecTime apiToVmExecTime;

    public HttpReqHandlerInterceptorOverload() {
        List<String> serverNames = new ArrayList<>();
        serverNames.add("http://localhost:9090");
        serverNames.add("http://localhost:9091");
        servers = Collections.unmodifiableList(serverNames);
        controllerStats = new ControllerStats();
        apiToVmExecTime = new ApiToVmExecTime();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        int vmIdWithMinimumExecutionTime = getNonOverloadedVmIdWithMinimumExecutionTime(request.getRequestURI());
        //TODO Add logic to check if above vmId has less load on it to do the task
        String serverPath = servers.get(vmIdWithMinimumExecutionTime);
        long executionTimeTakenForCurrentTask = 0L;


        System.out.println("Overload server path = " + serverPath);
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
            }

        }
        apiToVmExecTime.setExecTimeForApiVmPair(requestURI, vmIdWithMinimumExecutionTime, executionTimeTakenForCurrentTask);
        return false;
    }

    private int getNonOverloadedVmIdWithMinimumExecutionTime(String api) {
        List<VmExecTimeToTaskEncounteredCount> vmExecutionTimesForApi = apiToVmExecTime.getVmExecutionTimesForApi(api);
        //Comparator<VmExecTimeToTaskEncounteredCount> compareById = (VmExecTimeToTaskEncounteredCount o1, VmExecTimeToTaskEncounteredCount o2) -> o1.getExecutionTime().compareTo( o2.getExecutionTime() );
        //Collections.sort(vmExecutionTimesForApi, compareById);
        //int vmIdWithMinimumExecutionTime = vmExecutionTimesForApi.get(0).getVmId();
        /*for(int i =0; i< vmExecutionTimesForApi.size();i++){

            if(controllerStats.getControllerStats(vmExecutionTimesForApi.get(i).getVmId()).getCpu_utilisation() < 5.00 && controllerStats.getControllerStats(vmExecutionTimesForApi.get(i).getVmId()).getMem_utilisation() < 65.00 ){
                return vmExecutionTimesForApi.get(i).getVmId();
            }
        }*/
        long minExecTime_withOverload = Long.MAX_VALUE;
        int vmIdWithMinimumExecutionTimeWithOverload = -1;
        long minExecTime = Long.MAX_VALUE;
        int vmIdWithMinimumExecutionTime= -1;
        for (int i = 0; i < vmExecutionTimesForApi.size(); i++) {
            if (vmExecutionTimesForApi.get(i).getExecutionTime() <= minExecTime_withOverload &&( controllerStats.getControllerStats(i).getCpu_utilisation() < 5.00 && controllerStats.getControllerStats(i).getMem_utilisation() <65.00)) {
                minExecTime_withOverload = vmExecutionTimesForApi.get(i).getExecutionTime();
                vmIdWithMinimumExecutionTimeWithOverload = i;
            }
            if(vmExecutionTimesForApi.get(i).getExecutionTime() <= minExecTime){
                minExecTime = vmExecutionTimesForApi.get(i).getExecutionTime();
                vmIdWithMinimumExecutionTime =i;
            }
        }

        // case when all the containers are overloaded. return the one with minimum api time.
        if(vmIdWithMinimumExecutionTimeWithOverload == -1){
            return vmIdWithMinimumExecutionTime;
        }
        else{
            return vmIdWithMinimumExecutionTimeWithOverload;
        }
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
