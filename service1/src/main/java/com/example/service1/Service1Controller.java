package com.example.service1;

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
public class Service1Controller {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${SERVICE2_URL:http://localhost:3502}")
    private String service2Url;

    @Value("${PORT:3501}")
    private String port;

    @Value("${ATATUS_APP_NAME:Service 1}")
    private String appName;

    @GetMapping("/health")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", appName);
        response.put("message", "Hello from " + appName + "!");
        response.put("timestamp", LocalDateTime.now());
        response.put("port", port);
        response.put("service2Url", service2Url);
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
        response.put("dataType", "service1-random-data");
        response.put("port", port);
        
        return response;
    }

    @GetMapping("/call")
    public Map<String, Object> callService2() {
        try {
            String url = service2Url + "/data";
            Map<String, Object> service2Data = restTemplate.getForObject(url, Map.class);
            
            Map<String, Object> response = new HashMap<>();
            response.put("service", appName);
            response.put("message", "Successfully called Service 2");
            response.put("service2Response", service2Data);
            response.put("timestamp", LocalDateTime.now());
            response.put("calledUrl", url);
            response.put("port", port);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("service", appName);
            errorResponse.put("error", "Failed to call Service 2");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("attemptedUrl", service2Url + "/data");
            errorResponse.put("port", port);
            
            return errorResponse;
        }
    }
}
