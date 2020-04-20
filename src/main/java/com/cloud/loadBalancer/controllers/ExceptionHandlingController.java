package com.cloud.loadBalancer.controllers;

import com.cloud.loadBalancer.beans.HttpRequestAllParamaters;
import com.cloud.loadBalancer.exceptionHandler.ExceptionQueue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/exception")
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExceptionHandlingController {
    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/handleException")
    public void handleException() {
        HttpRequestAllParamaters httpRequestAllParamaters = ExceptionQueue.getFromExceptionHandleQueue();
        if (httpRequestAllParamaters != null) {
            Map<String, String> params = httpRequestAllParamaters.getParams();
            String serverPath = httpRequestAllParamaters.getServerPath();
            System.out.println(serverPath);
            restTemplate.exchange(createUri(params, serverPath, "/api/student/setValue"), httpRequestAllParamaters.getMethoodType(), httpRequestAllParamaters.getEntity(), Object.class);
        }

    }

    @GetMapping("/exceptionQueueLength")
    public int getExceptionQueueLength() {
        return ExceptionQueue.exceptionQueueLength();
    }


    private URI createUri(Map<String, String> params, String serverPath, String requestUri) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverPath + requestUri);

        if (params != null) {
            params.forEach(builder::queryParam);
        }
        URI uri = builder.build().encode().toUri();
        return uri;
    }
}