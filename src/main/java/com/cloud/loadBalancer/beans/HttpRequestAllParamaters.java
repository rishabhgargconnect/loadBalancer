package com.cloud.loadBalancer.beans;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;

public class HttpRequestAllParamaters {

    private Map<String, String> params;
    private String serverPath;
    private String requestUri;
    private HttpMethod methoodType;
    private HttpEntity<String> entity;

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public HttpMethod getMethoodType() {
        return methoodType;
    }

    public void setMethoodType(HttpMethod methoodType) {
        this.methoodType = methoodType;
    }

    public HttpEntity<String> getEntity() {
        return entity;
    }

    public void setEntity(HttpEntity<String> entity) {
        this.entity = entity;
    }

    public HttpRequestAllParamaters(Map<String, String> params, String serverPath, String requestUri, HttpMethod methodType, HttpEntity<String> entity) {
        this.params = params;
        this.serverPath = serverPath;
        this.requestUri = requestUri;
        this.methoodType = methodType;
        this.entity = entity;
    }


}
