package com.Health;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main Application - Health Check Service
 * Combines health monitoring with HTTP server for metrics exposure
 */
public class MainApplication {
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting Health Check Service...");
        System.out.println("=====================================");
        
        try {
            // Start HTTP server for metrics
            HealthCheckServer.startServer();
            
            // Start health monitoring
            startHealthMonitoring();
            
            // Keep the application running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Shutting down Health Check Service...");
                HealthCheckServer.stopServer();
            }));
            
            // Keep main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start Health Check Service: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Start the health monitoring service
     */
    private static void startHealthMonitoring() {
        System.out.println("🏥 Starting health monitoring...");
        
        // Perform initial health check
        HealthCheckService.performHealthCheck();
        
        // Schedule periodic health checks every 30 seconds
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            HealthCheckService::performHealthCheck,
            30, // initial delay
            30, // period
            TimeUnit.SECONDS
        );
        
        System.out.println("✅ Health monitoring started (30-second intervals)");
    }
}
