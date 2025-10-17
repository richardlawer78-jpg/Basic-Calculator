import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class test_health {
    public static void main(String[] args) {
        System.out.println("Testing Health Check Service...");
        System.out.println("================================");
        
        // Test the endpoints directly
        String[] endpoints = {
            "https://prod.npontu.io/endpoint_1/health_check",
            "https://prod.npontu.io/endpoint_2/health_check", 
            "https://prod.npontu.io/endpoint_3/health_check"
        };
        
        for (int i = 0; i < endpoints.length; i++) {
            boolean status = checkEndpoint(endpoints[i]);
            System.out.println("Endpoint " + (i+1) + ": " + (status ? "✅ HEALTHY" : "❌ UNHEALTHY"));
        }
    }
    
    private static boolean checkEndpoint(String url) {
        try {
            URL endpointUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) endpointUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 300;
            
        } catch (IOException e) {
            return false;
        }
    }
}
