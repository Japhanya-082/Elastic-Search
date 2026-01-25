package com.example.component;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.config.AuthContext;
import com.example.controller.IndexController;
import com.example.service.IndexLogService;

@Component
public class ScheduledFullIndexer {

    @Autowired
    private IndexController indexController;

    @Autowired
    private IndexLogService indexLogService;

    /**
     * Incremental indexing
     * Runs every day at 2 PM
     */
    @Scheduled(cron = "0 0 14 * * ?")
    public void incrementalIndex() {

        System.out.println("========== INCREMENTAL INDEX STARTED ==========");
        AuthContext.setToken("SYSTEM_SERVICE_TOKEN");

        try {
            // VMS incremental
            String lastIndexedVms =
                    indexLogService.getLastIndexedTime("VMS-SERVICE");
            indexController.indexVmsIncremental(lastIndexedVms);
            indexLogService.updateLastIndexedTime(
                    "VMS-SERVICE",
                    LocalDateTime.now()
            );

            // OPENREQS incremental
            String lastIndexedOpenReqs =
                    indexLogService.getLastIndexedTime("OPENREQS-SERVICE");
            indexController.indexOpenReqsIncremental(lastIndexedOpenReqs);
            indexLogService.updateLastIndexedTime(
                    "OPENREQS-SERVICE",
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            System.err.println("Incremental indexing failed");
            e.printStackTrace();
        } finally {
            AuthContext.clear();
        }

        System.out.println("========== INCREMENTAL INDEX COMPLETED ==========");
    }

    /**
     * Full indexing
     * Runs once per day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void fullDailyIndex() {

        System.out.println("========== DAILY FULL INDEX STARTED ==========");
        AuthContext.setToken("SYSTEM_SERVICE_TOKEN");

        try {
            indexController.indexAllServices(null);

            // Reset incremental timestamps after full reindex
            indexLogService.resetLastIndexedTime("VMS-SERVICE");
            indexLogService.resetLastIndexedTime("OPENREQS-SERVICE");

        } catch (Exception e) {
            System.err.println("Full daily indexing failed");
            e.printStackTrace();
        } finally {
            AuthContext.clear();
        }

        System.out.println("========== DAILY FULL INDEX COMPLETED ==========");
    }
    
}
