package com.Health;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Comprehensive Health Check Service
 * Monitors 3 endpoints and exposes metrics via /health endpoint
 */
public class HealthCheckService {
    
    // Endpoints to monitor
    public static final String ENDPOINT_ONE = "https://prod.npontu.io/endpoint_1/health_check";
    public static final String ENDPOINT_TWO = "https://prod.npontu.io/endpoint_2/health_check";
    public static final String ENDPOINT_THREE = "https://prod.npontu.io/endpoint_3/health_check";
    
    // Health status tracking
    private static volatile boolean endpointOneStatus = false;
    private static volatile boolean endpointTwoStatus = false;
    private static volatile boolean endpointThreeStatus = false;
    
    // Metrics tracking
    private static final AtomicInteger totalChecks = new AtomicInteger(0);
    private static final AtomicInteger failedChecks = new AtomicInteger(0);
    private static final AtomicLong lastCheckTime = new AtomicLong(0);
    private static final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    
    // Service down detection
    private static final int MAX_CONSECUTIVE_FAILURES = 3;
    private static final long ALERT_COOLDOWN_MS = 300000; // 5 minutes
    private static volatile long lastAlertTime = 0;
    
    // Thread pool for concurrent health checks
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    
    // Health check configuration
    private static final int CHECK_INTERVAL_SECONDS = 30;
    private static final int REQUEST_TIMEOUT_MS = 10000;
    
    /**
     * Check the health status of a single endpoint
     */
    private static boolean checkEndpointHealth(String url) {
        try {
            URL endpointUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) endpointUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(REQUEST_TIMEOUT_MS);
            connection.setReadTimeout(REQUEST_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "HealthCheckService/1.0");
            
            int responseCode = connection.getResponseCode();
            boolean isHealthy = responseCode >= 200 && responseCode < 300;
            
            System.out.println(String.format("[%s] %s - Status: %d - %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                url, responseCode, isHealthy ? "HEALTHY" : "UNHEALTHY"));
            
            return isHealthy;
            
        } catch (IOException e) {
            System.err.println(String.format("[%s] ERROR checking %s: %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                url, e.getMessage()));
            return false;
        }
    }
    
    /**
     * Perform health check on all endpoints concurrently
     */
    private static void performHealthCheck() {
        System.out.println("\n=== Performing Health Check ===");
        
        // Create futures for concurrent execution
        Future<Boolean> future1 = executor.submit(() -> checkEndpointHealth(ENDPOINT_ONE));
        Future<Boolean> future2 = executor.submit(() -> checkEndpointHealth(ENDPOINT_TWO));
        Future<Boolean> future3 = executor.submit(() -> checkEndpointHealth(ENDPOINT_THREE));
        
        try {
            // Wait for all checks to complete with timeout
            boolean status1 = future1.get(REQUEST_TIMEOUT_MS + 5000, TimeUnit.MILLISECONDS);
            boolean status2 = future2.get(REQUEST_TIMEOUT_MS + 5000, TimeUnit.MILLISECONDS);
            boolean status3 = future3.get(REQUEST_TIMEOUT_MS + 5000, TimeUnit.MILLISECONDS);
            
            // Update status
            endpointOneStatus = status1;
            endpointTwoStatus = status2;
            endpointThreeStatus = status3;
            
            // Update metrics
            totalChecks.incrementAndGet();
            lastCheckTime.set(System.currentTimeMillis());
            
            // Check for failures
            int currentFailures = 0;
            if (!status1) currentFailures++;
            if (!status2) currentFailures++;
            if (!status3) currentFailures++;
            
            if (currentFailures > 0) {
                failedChecks.addAndGet(currentFailures);
                consecutiveFailures.set(currentFailures);
                
                // Check if we need to send alert
                if (currentFailures >= MAX_CONSECUTIVE_FAILURES) {
                    sendServiceDownAlert(currentFailures);
                }
            } else {
                consecutiveFailures.set(0);
            }
            
            // Print summary
            printHealthSummary();
            
        } catch (Exception e) {
            System.err.println("Error during health check: " + e.getMessage());
            failedChecks.incrementAndGet();
            consecutiveFailures.incrementAndGet();
        }
    }
    
    /**
     * Send alert when service goes down
     */
    private static void sendServiceDownAlert(int failedServices) {
        long currentTime = System.currentTimeMillis();
        
        // Implement cooldown to avoid spam
        if (currentTime - lastAlertTime < ALERT_COOLDOWN_MS) {
            return;
        }
        
        lastAlertTime = currentTime;
        
        System.out.println("\n🚨 ALERT: SERVICE DOWN DETECTED! 🚨");
        System.out.println("=====================================");
        System.out.println("Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Failed Services: " + failedServices + "/3");
        System.out.println("Endpoint 1: " + (endpointOneStatus ? "✅" : "❌"));
        System.out.println("Endpoint 2: " + (endpointTwoStatus ? "✅" : "❌"));
        System.out.println("Endpoint 3: " + (endpointThreeStatus ? "✅" : "❌"));
        System.out.println("=====================================\n");
        
        // In production, this would send to monitoring systems like:
        // - PagerDuty
        // - Slack/Teams
        // - Email notifications
        // - Log aggregation systems (ELK, Splunk)
    }
    
    /**
     * Print health summary
     */
    private static void printHealthSummary() {
        System.out.println("\n--- Health Summary ---");
        System.out.println("Endpoint 1: " + (endpointOneStatus ? "✅ HEALTHY" : "❌ UNHEALTHY"));
        System.out.println("Endpoint 2: " + (endpointTwoStatus ? "✅ HEALTHY" : "❌ UNHEALTHY"));
        System.out.println("Endpoint 3: " + (endpointThreeStatus ? "✅ HEALTHY" : "❌ UNHEALTHY"));
        
        boolean allHealthy = endpointOneStatus && endpointTwoStatus && endpointThreeStatus;
        System.out.println("Overall Status: " + (allHealthy ? "✅ ALL HEALTHY" : "❌ DEGRADED"));
        System.out.println("Consecutive Failures: " + consecutiveFailures.get());
        System.out.println("Total Checks: " + totalChecks.get());
        System.out.println("Failed Checks: " + failedChecks.get());
        System.out.println("Success Rate: " + String.format("%.2f%%", 
            totalChecks.get() > 0 ? (1.0 - (double)failedChecks.get() / totalChecks.get()) * 100 : 100));
    }
    
    /**
     * Get health metrics in JSON format (for /health endpoint)
     */
    public static String getHealthMetrics() {
        boolean allHealthy = endpointOneStatus && endpointTwoStatus && endpointThreeStatus;
        double successRate = totalChecks.get() > 0 ? 
            (1.0 - (double)failedChecks.get() / totalChecks.get()) * 100 : 100;
        
        return String.format(
            "{\n" +
            "  \"timestamp\": \"%s\",\n" +
            "  \"status\": \"%s\",\n" +
            "  \"endpoints\": {\n" +
            "    \"endpoint_1\": {\n" +
            "      \"url\": \"%s\",\n" +
            "      \"status\": \"%s\",\n" +
            "      \"healthy\": %s\n" +
            "    },\n" +
            "    \"endpoint_2\": {\n" +
            "      \"url\": \"%s\",\n" +
            "      \"status\": \"%s\",\n" +
            "      \"healthy\": %s\n" +
            "    },\n" +
            "    \"endpoint_3\": {\n" +
            "      \"url\": \"%s\",\n" +
            "      \"status\": \"%s\",\n" +
            "      \"healthy\": %s\n" +
            "    }\n" +
            "  },\n" +
            "  \"metrics\": {\n" +
            "    \"total_checks\": %d,\n" +
            "    \"failed_checks\": %d,\n" +
            "    \"success_rate\": %.2f,\n" +
            "    \"consecutive_failures\": %d,\n" +
            "    \"last_check_time\": \"%s\"\n" +
            "  },\n" +
            "  \"alerts\": {\n" +
            "    \"service_down_detected\": %s,\n" +
            "    \"last_alert_time\": \"%s\"\n" +
            "  }\n" +
            "}",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
            allHealthy ? "healthy" : "degraded",
            ENDPOINT_ONE,
            endpointOneStatus ? "healthy" : "unhealthy",
            endpointOneStatus,
            ENDPOINT_TWO,
            endpointTwoStatus ? "healthy" : "unhealthy",
            endpointTwoStatus,
            ENDPOINT_THREE,
            endpointThreeStatus ? "healthy" : "unhealthy",
            endpointThreeStatus,
            totalChecks.get(),
            failedChecks.get(),
            successRate,
            consecutiveFailures.get(),
            lastCheckTime.get() > 0 ? 
                LocalDateTime.ofEpochSecond(lastCheckTime.get() / 1000, 0, 
                    java.time.ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) : "never",
            consecutiveFailures.get() >= MAX_CONSECUTIVE_FAILURES,
            lastAlertTime > 0 ? 
                LocalDateTime.ofEpochSecond(lastAlertTime / 1000, 0, 
                    java.time.ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) : "never"
        );
    }
    
    /**
     * Main method to start the health check service
     */
    public static void main(String[] args) {
        System.out.println("🏥 Health Check Service Starting...");
        System.out.println("Monitoring 3 endpoints every " + CHECK_INTERVAL_SECONDS + " seconds");
        System.out.println("Health metrics available at: GET /health");
        System.out.println("Press Ctrl+C to stop monitoring\n");
        
        // Perform initial health check
        performHealthCheck();
        
        // Schedule periodic health checks
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            HealthCheckService::performHealthCheck,
            CHECK_INTERVAL_SECONDS,
            CHECK_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
        
        // Keep the main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Health check service stopped.");
            scheduler.shutdown();
            executor.shutdown();
        }
    }
}
