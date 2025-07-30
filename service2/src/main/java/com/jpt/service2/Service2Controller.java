package com.jpt.service2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class Service2Controller {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${SERVICE_1_URL:http://localhost:3501}")
    private String service1Url;

    @Value("${PORT:3502}")
    private String port;

    @Value("${ATATUS_APP_NAME:Service 2}")
    private String appName;

    @GetMapping("/health")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", appName);
        response.put("message", "Hello from " + appName + "!");
        response.put("timestamp", LocalDateTime.now());
        response.put("port", port);
        response.put("service1Url", service1Url);
        return response;
    }

    @GetMapping("/data")
    public Map<String, Object> getData() {
        Map<String, Object> response = new HashMap<>();
        Random random = new Random();
        
        response.put("service", appName);
        response.put("randomNumber", random.nextInt(1000));
        response.put("randomString", "data-" + random.nextInt(100));
        response.put("timestamp", LocalDateTime.now());
        response.put("dataType", "service2-random-data");
        response.put("uuid", java.util.UUID.randomUUID().toString());
        response.put("port", port);
        
        return response;
    }

    @GetMapping("/call")
    public Map<String, Object> callService1() {
        try {
            String url = service1Url + "/data";
            Map<String, Object> service1Data = restTemplate.getForObject(url, Map.class);
            
            Map<String, Object> response = new HashMap<>();
            response.put("service", appName);
            response.put("message", "Successfully called Service 1");
            response.put("service1Response", service1Data);
            response.put("timestamp", LocalDateTime.now());
            response.put("calledUrl", url);
            response.put("port", port);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("service", appName);
            errorResponse.put("error", "Failed to call Service 1");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("attemptedUrl", service1Url + "/data");
            errorResponse.put("port", port);
            
            return errorResponse;
        }
    }
}
