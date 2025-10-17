package com.Health;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HealthServer {
    
    public static void startHealthEndpoint() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // /health endpoint
        server.createContext("/health", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String response = Main.getHealth();
                    
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            }
        });
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🌐 Health endpoint available at: http://localhost:8080/health");
    }
}
