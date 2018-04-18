package com.bca.boot.service;

import com.bca.boot.model.AccessToken;
import com.bca.boot.prop.BcaProperties;
import com.bca.boot.util.TransactionInterceptor;
import com.google.gson.Gson;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author opaw
 */

@Service
public class BcaService {
    @Autowired public BcaProperties properties;
    @Autowired private RestTemplate restTemplate;
    private AccessToken accessToken;
    
    public static final DateFormat TIMESTAMPT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());

    public String currentTime(){
        return TIMESTAMPT_FORMATTER.format(new Date());
    }
    
    private void requestToken(){
        accessToken = new AccessToken();
        String plainCreds = properties.getClientId() + ":" + properties.getClientSecret();
        String base64Creds = Base64.getEncoder().encodeToString(plainCreds.getBytes());

        String authHeader = "Basic " + base64Creds;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        String url = properties.getHost() + "/api/oauth/token";
        long createdTime = System.currentTimeMillis();
        ResponseEntity<AccessToken> resp = restTemplate.exchange(url, HttpMethod.POST, request, AccessToken.class);
        accessToken = resp.getBody();
        accessToken.setCreatedTime(createdTime);
    }
    
    public AccessToken getToken(){
        if(accessToken == null)
            requestToken();
        else {
            Long expiresIn = Long.valueOf(accessToken.getExpiresIn()) * 1000;
            Long expiredTime = accessToken.getCreatedTime() + expiresIn;
            Long currentTime = System.currentTimeMillis();
            if(currentTime > expiredTime) requestToken();            
        }
        return accessToken;
    }
    
    public Map<String, Object> request(HttpMethod method, String relativeUrl, Map<String, Object> payload) {
        AccessToken token = getToken();
        restTemplate.setInterceptors(Collections.singletonList(new TransactionInterceptor(token.getAccessToken(), properties.getApiKey(), properties.getApiSecret())));
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<Map<String, Object>>() {};
        HttpEntity entity = new HttpEntity(new LinkedMultiValueMap<>(), new HttpHeaders());
        if(payload != null){
            entity = createEntity(payload);
        }
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(relativeUrl, method, entity, responseType);
            return resp.getBody();
        } catch (HttpClientErrorException ex){
            return new Gson().fromJson(ex.getResponseBodyAsString(), HashMap.class);
        }
    }
    
    public HttpEntity createEntity(Map<String, Object> payload){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }
    
    public String buildUrl(String path, List<String> pathVariables, Map<String, Object> requestParam) {
        if (pathVariables != null) {
            for (String pathVariable : pathVariables) {
                path = path.replaceFirst("\\{}", pathVariable);
            }
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(properties.getHost()).path(path);
        if (requestParam != null) {
            requestParam.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(x -> builder.queryParam(x.getKey(), x.getValue()));
        }
        return builder.build().toUriString();
    }
}
