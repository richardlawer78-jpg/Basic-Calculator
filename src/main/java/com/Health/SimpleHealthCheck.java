package com.Health;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class SimpleHealthCheck {
    
    public static final String ENDPOINT_ONE = "https://prod.npontu.io/endpoint_1/health_check";
    public static final String ENDPOINT_TWO = "https://prod.npontu.io/endpoint_2/health_check";
    public static final String ENDPOINT_THREE = "https://prod.npontu.io/endpoint_3/health_check";

    public static boolean endpointOneStatus = false;
    public static boolean endpointTwoStatus = false;
    public static boolean endpointThreeStatus = false;

    private static boolean checkStatus(String url) {
        try {
            URL endpointUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) endpointUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds timeout
            connection.setReadTimeout(10000); // 10 seconds read timeout
            
            int responseCode = connection.getResponseCode();
            
            // Consider 200-299 as healthy status
            boolean isHealthy = responseCode >= 200 && responseCode < 300;
            
            System.out.println("Checking " + url + " - Response Code: " + responseCode + " - Status: " + (isHealthy ? "HEALTHY" : "UNHEALTHY"));
            
            return isHealthy;
            
        } catch (IOException e) {
            System.err.println("Error checking endpoint " + url + ": " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Health Check Service Started ===");
        System.out.println("Monitoring endpoints every 30 seconds...");
        System.out.println("Press Ctrl+C to stop monitoring\n");
        
        while (true) {
            System.out.println("--- Health Check Status ---");
            System.out.println("Timestamp: " + java.time.LocalDateTime.now());
            
            endpointOneStatus = checkStatus(ENDPOINT_ONE);
            endpointTwoStatus = checkStatus(ENDPOINT_TWO);
            endpointThreeStatus = checkStatus(ENDPOINT_THREE);
            
            System.out.println("\nSummary:");
            System.out.println("ENDPOINT_ONE: " + (endpointOneStatus ? "✅ HEALTHY" : "❌ UNHEALTHY"));
            System.out.println("ENDPOINT_TWO: " + (endpointTwoStatus ? "✅ HEALTHY" : "❌ UNHEALTHY"));
            System.out.println("ENDPOINT_THREE: " + (endpointThreeStatus ? "✅ HEALTHY" : "❌ UNHEALTHY"));
            
            boolean allHealthy = endpointOneStatus && endpointTwoStatus && endpointThreeStatus;
            System.out.println("Overall Status: " + (allHealthy ? "✅ ALL HEALTHY" : "❌ SOME UNHEALTHY"));
            System.out.println("=====================================\n");
            
            // Wait 30 seconds before next check
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
                System.out.println("Health check monitoring stopped.");
                break;
            }
        }
    }
    
    // Method to get health status for API endpoint
    public static String getHealth() {
        return String.format(
            "{\n" +
            "  \"timestamp\": \"%s\",\n" +
            "  \"ENDPOINT_ONE\": %s,\n" +
            "  \"ENDPOINT_TWO\": %s,\n" +
            "  \"ENDPOINT_THREE\": %s,\n" +
            "  \"overall_status\": \"%s\"\n" +
            "}",
            java.time.LocalDateTime.now(),
            endpointOneStatus,
            endpointTwoStatus,
            endpointThreeStatus,
            (endpointOneStatus && endpointTwoStatus && endpointThreeStatus) ? "ALL_HEALTHY" : "SOME_UNHEALTHY"
        );
    }
}
