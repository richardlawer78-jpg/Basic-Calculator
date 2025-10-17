package com.Health;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HealthCheckApp {
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting Health Check Application...");
        
        try {
            // Start HTTP server for /health endpoint
            HealthServer.startHealthEndpoint();
            
            // Start health monitoring in background
            startHealthMonitoring();
            
            // Keep application running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Shutting down Health Check Application...");
            }));
            
            // Keep main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void startHealthMonitoring() {
        System.out.println("🏥 Starting health monitoring...");
        
        // Perform initial health check
        Main.main(new String[]{});
        
        // Schedule periodic health checks every 30 seconds
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n=== Scheduled Health Check ===");
            System.out.println("Time: " + java.time.LocalDateTime.now());
            
            Main.endpointOneStatus = Main.checkStatus(Main.ENDPOINT_ONE);
            Main.endpointTwoStatus = Main.checkStatus(Main.ENDPOINT_TWO);
            Main.endpointThreeStatus = Main.checkStatus(Main.ENDPOINT_THREE);
            
            System.out.println("ENDPOINT_ONE: " + (Main.endpointOneStatus ? "✅" : "❌"));
            System.out.println("ENDPOINT_TWO: " + (Main.endpointTwoStatus ? "✅" : "❌"));
            System.out.println("ENDPOINT_THREE: " + (Main.endpointThreeStatus ? "✅" : "❌"));
            
        }, 30, 30, TimeUnit.SECONDS);
        
        System.out.println("✅ Health monitoring started (30-second intervals)");
    }
}
