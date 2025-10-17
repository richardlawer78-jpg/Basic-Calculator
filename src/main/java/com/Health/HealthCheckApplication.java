package com.Health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HealthCheckApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HealthCheckApplication.class, args);
        
        // Start background health monitoring
        startHealthMonitoring();
    }
    
    private static void startHealthMonitoring() {
        Thread monitoringThread = new Thread(() -> {
            while (true) {
                try {
                    // Update status every 30 seconds
                    EndPoints.endpointOneStatus = EndPoints.checkStatus(EndPoints.ENDPOINT_ONE);
                    EndPoints.endpointTwoStatus = EndPoints.checkStatus(EndPoints.ENDPOINT_TWO);
                    EndPoints.endpointThreeStatus = EndPoints.checkStatus(EndPoints.ENDPOINT_THREE);
                    
                    Thread.sleep(30000); // 30 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        monitoringThread.setDaemon(true);
        monitoringThread.start();
    }
}
