package com.Health;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Main {
    
    public static final String ENDPOINT_ONE = "https://prod.npontu.io/endpoint_1/health_check";
    public static final String ENDPOINT_TWO = "https://prod.npontu.io/endpoint_2/health_check";
    public static final String ENDPOINT_THREE = "https://prod.npontu.io/endpoint_3/health_check";
    
    public static boolean endpointOneStatus = false;
    public static boolean endpointTwoStatus = false;
    public static boolean endpointThreeStatus = false;
    
    private static Boolean checkStatus(String url) {
        try {
            // Make HTTP request to check status
            URL endpointUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) endpointUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds timeout
            connection.setReadTimeout(10000); // 10 seconds read timeout
            
            // Wait for response (up to 10 seconds)
            int responseCode = connection.getResponseCode();
            
            // Check if response is healthy (200-299 status codes)
            if (responseCode >= 200 && responseCode < 300) {
                return true;
            } else {
                return false;
            }
            
        } catch (IOException e) {
            return false;
        }
    }
    
    public static void main(String[] args) {
        while (true) {
            endpointOneStatus = checkStatus(ENDPOINT_ONE);
            endpointTwoStatus = checkStatus(ENDPOINT_TWO);
            endpointThreeStatus = checkStatus(ENDPOINT_THREE);
            
            // Wait 10 seconds
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    // /health endpoint method
    public static String getHealth() {
        return "{\n" +
               "  \"ENDPOINT_ONE\": " + endpointOneStatus + ",\n" +
               "  \"ENDPOINT_TWO\": " + endpointTwoStatus + ",\n" +
               "  \"ENDPOINT_THREE\": " + endpointThreeStatus + "\n" +
               "}";
    }
}
