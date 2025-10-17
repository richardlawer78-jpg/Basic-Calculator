package com.Health;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class HealthCheckController {
    
    private final EndPoints healthChecker = new EndPoints();
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        // Check all endpoints asynchronously
        CompletableFuture<Boolean> endpoint1 = CompletableFuture.supplyAsync(() -> 
            healthChecker.checkStatus(EndPoints.ENDPOINT_ONE));
        CompletableFuture<Boolean> endpoint2 = CompletableFuture.supplyAsync(() -> 
            healthChecker.checkStatus(EndPoints.ENDPOINT_TWO));
        CompletableFuture<Boolean> endpoint3 = CompletableFuture.supplyAsync(() -> 
            healthChecker.checkStatus(EndPoints.ENDPOINT_THREE));
        
        try {
            // Wait for all checks to complete with timeout
            Boolean status1 = endpoint1.get(10, TimeUnit.SECONDS);
            Boolean status2 = endpoint2.get(10, TimeUnit.SECONDS);
            Boolean status3 = endpoint3.get(10, TimeUnit.SECONDS);
            
            healthStatus.put("timestamp", java.time.LocalDateTime.now().toString());
            healthStatus.put("ENDPOINT_ONE", status1);
            healthStatus.put("ENDPOINT_TWO", status2);
            healthStatus.put("ENDPOINT_THREE", status3);
            healthStatus.put("overall_status", (status1 && status2 && status3) ? "ALL_HEALTHY" : "SOME_UNHEALTHY");
            
            // Return appropriate HTTP status
            HttpStatus httpStatus = (status1 && status2 && status3) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            
            return ResponseEntity.status(httpStatus).body(healthStatus);
            
        } catch (Exception e) {
            healthStatus.put("error", "Health check failed: " + e.getMessage());
            healthStatus.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(healthStatus);
        }
    }
    
    @GetMapping("/health/{endpoint}")
    public ResponseEntity<Map<String, Object>> getEndpointHealth(@PathVariable String endpoint) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean status = false;
            String endpointUrl = "";
            
            switch (endpoint.toLowerCase()) {
                case "1":
                case "one":
                    endpointUrl = EndPoints.ENDPOINT_ONE;
                    status = healthChecker.checkStatus(EndPoints.ENDPOINT_ONE);
                    break;
                case "2":
                case "two":
                    endpointUrl = EndPoints.ENDPOINT_TWO;
                    status = healthChecker.checkStatus(EndPoints.ENDPOINT_TWO);
                    break;
                case "3":
                case "three":
                    endpointUrl = EndPoints.ENDPOINT_THREE;
                    status = healthChecker.checkStatus(EndPoints.ENDPOINT_THREE);
                    break;
                default:
                    response.put("error", "Invalid endpoint. Use 1, 2, or 3");
                    return ResponseEntity.badRequest().body(response);
            }
            
            response.put("endpoint", endpoint);
            response.put("url", endpointUrl);
            response.put("status", status ? "HEALTHY" : "UNHEALTHY");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(status ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
                    
        } catch (Exception e) {
            response.put("error", "Health check failed: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/health/summary")
    public ResponseEntity<Map<String, Object>> getHealthSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Get current status from static variables
        summary.put("ENDPOINT_ONE", EndPoints.endpointOneStatus);
        summary.put("ENDPOINT_TWO", EndPoints.endpointTwoStatus);
        summary.put("ENDPOINT_THREE", EndPoints.endpointThreeStatus);
        summary.put("all_healthy", EndPoints.allEndpointsHealthy());
        summary.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(summary);
    }
}
