package com.Health;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Simple HTTP Server to expose health check metrics
 * Provides /health endpoint with JSON metrics
 */
public class HealthCheckServer {
    
    private static final int PORT = 8080;
    private static HttpServer server;
    
    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Health endpoint
        server.createContext("/health", new HealthHandler());
        
        // Root endpoint with basic info
        server.createContext("/", new RootHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🌐 Health Check Server started on port " + PORT);
        System.out.println("📊 Health metrics available at: http://localhost:" + PORT + "/health");
        System.out.println("ℹ️  Server info available at: http://localhost:" + PORT + "/");
    }
    
    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Health Check Server stopped");
        }
    }
    
    /**
     * Handler for /health endpoint
     */
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = HealthCheckService.getHealthMetrics();
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
    
    /**
     * Handler for root endpoint
     */
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "<!DOCTYPE html>" +
                "<html><head><title>Health Check Service</title></head>" +
                "<body>" +
                "<h1>🏥 Health Check Service</h1>" +
                "<p>This service monitors 3 endpoints and provides health metrics.</p>" +
                "<h2>Available Endpoints:</h2>" +
                "<ul>" +
                "<li><a href='/health'>GET /health</a> - Health metrics in JSON format</li>" +
                "</ul>" +
                "<h2>Monitored Endpoints:</h2>" +
                "<ul>" +
                "<li>Endpoint 1: " + HealthCheckService.ENDPOINT_ONE + "</li>" +
                "<li>Endpoint 2: " + HealthCheckService.ENDPOINT_TWO + "</li>" +
                "<li>Endpoint 3: " + HealthCheckService.ENDPOINT_THREE + "</li>" +
                "</ul>" +
                "<p><em>Service started at: " + java.time.LocalDateTime.now() + "</em></p>" +
                "</body></html>";
            
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
